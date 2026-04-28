package DAO;
import DAO.LibraryBookDAO;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import javax.persistence.*;
import odb.Book;
import odb.Borrow;
import odb.Library_Book;
import odb.Person;

public class PersonDAO {

    private EntityManagerFactory emf =
    Persistence.createEntityManagerFactory("com.mycompany_dbconnection_jar_1.0-SNAPSHOTPU");//connction wit the database
    
    
    
    public void rollback(Person p,Book b1){
            EntityManager em = emf.createEntityManager();
            
            BorrowDAO dao=new BorrowDAO();
          LibraryBookDAO lib=new LibraryBookDAO();
          
          ;//this get me the nearset book in the database in the librarybook database i pass to it the bookid and then search for thr book id in the librarybook and first one found is being taken to be borrowed 
   Borrow borrow =dao.searchforpersonandbook(p, b1);
  try {
        Borrow b = new Borrow();
        if(borrow!=null){
        borrow.getLibrary_Book().setAvailable_Copies(borrow.getLibrary_Book().getAvailable_Copies()+1);
        lib.update(borrow.getLibrary_Book());//update the avaliable copies here
        borrow.setReturnDate(LocalDate.now());
        borrow.setStatus("RETURNED");
        dao.update(borrow);
    
        }
        else{
            System.out.println("the Book is not avialble right now");
            
        }    
    } finally {
        em.close();
    }
    }
    
    
    //this is used as the user is making a borrow form thr nearset book in the datatabase 
    public void borrowBook(Person p, Book b1) {
    EntityManager em = emf.createEntityManager();
  LibraryBookDAO dao=new LibraryBookDAO();
BorrowDAO borrow=new BorrowDAO();
  
Library_Book lb=dao.asigh_book(b1);//this get me the nearset book in the database in the librarybook database i pass to it the bookid and then search for thr book id in the librarybook and first one found is being taken to be borrowed 
    try {
        em.getTransaction().begin();
        Borrow b = new Borrow();
        if(lb!=null){
        b.setP(p);
        b.setBorrowDate(LocalDate.now());
        b.setStatus("BORROWED");
        lb.setAvailable_Copies(lb.getAvailable_Copies()-1);
        dao.update(lb);//update the avaliable copies here
        b.setLibrary_Book(lb);
        em.getTransaction().commit();
        borrow.insert(b);
        }
        else{
            System.out.println("the Book is not avialble right now");
            
        }    
    } finally {
        em.close();
    }

    }

    public void borrowLibraryBook(Person p, Long libraryBookId) {
        if (p == null || libraryBookId == null) {
            return;
        }
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Person managedPerson = em.find(Person.class, p.getId());
            Library_Book libraryBook = em.find(Library_Book.class, libraryBookId);
            if (managedPerson == null || libraryBook == null) {
                throw new IllegalStateException("Book or user not found");
            }
            if (libraryBook.getAvailable_Copies() <= 0) {
                throw new IllegalStateException("Book is not available");
            }

            boolean alreadyBorrowed = !em.createQuery(
                            "SELECT b FROM Borrow b WHERE b.p.id = :personId AND b.Library_Book.id = :libraryBookId AND b.status = :status",
                            Borrow.class
                    )
                    .setParameter("personId", managedPerson.getId())
                    .setParameter("libraryBookId", libraryBook.getId())
                    .setParameter("status", "BORROWED")
                    .getResultList()
                    .isEmpty();
            if (alreadyBorrowed) {
                throw new IllegalStateException("This book is already borrowed");
            }

            libraryBook.setAvailable_Copies(libraryBook.getAvailable_Copies() - 1);

            Borrow borrow = new Borrow();
            borrow.setP(managedPerson);
            borrow.setBorrowDate(LocalDate.now());
            borrow.setStatus("BORROWED");
            borrow.setLibrary_Book(libraryBook);
            em.persist(borrow);

            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
    } finally {
        em.close();
    }
}

public void insert(Person p) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(p);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public Person findById(Long id) {
        EntityManager em = emf.createEntityManager();

        try {
            return em.find(Person.class, id);
        } finally {
            em.close();
        }
    }

    public List<Person> findAll() {
        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery("SELECT p FROM Person p", Person.class)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    public void update(Person p) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            em.merge(p);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            Person p = em.find(Person.class, id);

            if (p != null) {
                em.remove(p);
            }

            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public Person findByEmail(String email) {
        EntityManager em = emf.createEntityManager();
        try {
            List<Person> result = em.createQuery(
                            "SELECT p FROM Person p WHERE p.Email = :email",
                            Person.class
                    )
                    .setParameter("email", email)
                    .getResultList();
            return result.isEmpty() ? null : result.get(0);
        } finally {
            em.close();
        }
    }

    public Person authenticate(String email, String password) {
        Person person = findByEmail(email);
        if (person == null) {
            return null;
        }
        String hashed = sha256Hex(password);
        if (hashed.equals(person.getPasswod())) {
            return person;
        }
        return null;
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
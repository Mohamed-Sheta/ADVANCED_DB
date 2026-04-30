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

    private static EntityManagerFactory emf;

    public PersonDAO() {
        synchronized (PersonDAO.class) {
            if (emf == null) {
                emf = Persistence.createEntityManagerFactory("dbconnectionPU");
            }
        }
    }

    public void rollback(Person p, Book b1) {

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        BorrowDAO dao = new BorrowDAO();
        LibraryBookDAO lib = new LibraryBookDAO();

        try {
            tx.begin();

            Borrow borrow = dao.searchforpersonandbook(p, b1);

            if (borrow == null) {
                System.out.println("User did not borrow this book");
                return;
            }

            if ("RETURNED".equals(borrow.getStatus())) {
                System.out.println("Book already returned");
                return;
            }

            Library_Book lb = borrow.getLibrary_Book();

            lb.setAvailable_Copies(lb.getAvailable_Copies() + 1);
            lib.update(lb);

            borrow.setReturnDate(LocalDate.now());
            borrow.setStatus("RETURNED");
            dao.update(borrow);

            tx.commit();

            System.out.println("Book returned successfully ✅");

        } catch (Exception e) {

            if (tx.isActive()) {
                tx.rollback();
            }

            e.printStackTrace();

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
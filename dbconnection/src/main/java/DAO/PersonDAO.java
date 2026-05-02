package DAO;
import DAO.LibraryBookDAO;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Collections;
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

            List<Borrow> existingBorrows = em.createQuery(
                            "SELECT b FROM Borrow b WHERE b.p.id = :personId AND b.Library_Book.id = :libraryBookId",
                            Borrow.class
                    )
                    .setParameter("personId", managedPerson.getId())
                    .setParameter("libraryBookId", libraryBook.getId())
                    .getResultList();
            Borrow existingBorrow = existingBorrows.isEmpty() ? null : existingBorrows.get(0);

            if (existingBorrow != null) {
                if ("BORROWED".equals(existingBorrow.getStatus())) {
                    throw new IllegalStateException("This book is already borrowed");
                }
                existingBorrow.setBorrowDate(LocalDate.now());
                existingBorrow.setReturnDate(null);
                existingBorrow.setStatus("BORROWED");
                libraryBook.setAvailable_Copies(libraryBook.getAvailable_Copies() - 1);
                em.merge(existingBorrow);
            } else {
                libraryBook.setAvailable_Copies(libraryBook.getAvailable_Copies() - 1);
                Borrow borrow = new Borrow();
                borrow.setP(managedPerson);
                borrow.setBorrowDate(LocalDate.now());
                borrow.setStatus("BORROWED");
                borrow.setLibrary_Book(libraryBook);
                em.persist(borrow);
            }

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

    public List<Book> searchBorrowedBooksByText(Long personId, String inputText) {
        if (personId == null) {
            return Collections.emptyList();
        }
        String trimmed = inputText == null ? "" : inputText.trim();
        if (trimmed.isEmpty()) {
            return Collections.emptyList();
        }

        EntityManager em = emf.createEntityManager();
        try {
            String pattern = "%" + trimmed.toLowerCase() + "%";
            return em.createQuery(
                            "SELECT DISTINCT b2 " +
                            "FROM Person p " +
                            "JOIN p.b br " +
                            "JOIN br.Library_Book lb " +
                            "JOIN lb.B b2 " +
                            "WHERE p.id = :personId " +
                            "AND (LOWER(b2.Title) LIKE :pattern OR LOWER(b2.Author) LIKE :pattern)",
                            Book.class
                    )
                    .setParameter("personId", personId)
                    .setParameter("pattern", pattern)
                    .getResultList();
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
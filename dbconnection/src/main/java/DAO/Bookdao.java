package DAO;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import odb.Book;

public class Bookdao {

    private EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("com.mycompany_dbconnection_jar_1.0-SNAPSHOTPU");

    public void insert(Book book) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            em.persist(book);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public Book findById(Long id) {
        EntityManager em = emf.createEntityManager();

        try {
            return em.find(Book.class, id);
        } finally {
            em.close();
        }
    }

    public List<Book> findAll() {
        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery(
                    "SELECT b FROM Book b",
                    Book.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public void update(Book book) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            em.merge(book);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            Book book = em.find(Book.class, id);

            if (book != null) {
                em.remove(book);
            }

            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<Book> findByTitle(String title) {
        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery(
                    "SELECT b FROM Book b WHERE b.Title = :title",
                    Book.class
            ).setParameter("title", title)
             .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Book> findByAuthor(String author) {
        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery(
                    "SELECT b FROM Book b WHERE b.Author = :author",
                    Book.class
            ).setParameter("author", author)
             .getResultList();
        } finally {
            em.close();
        }
    }
}

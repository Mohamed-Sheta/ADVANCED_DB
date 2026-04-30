package DAO;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import odb.Book;

public class Bookdao {

    private static EntityManagerFactory emf;

    public Bookdao() {
        synchronized (Bookdao.class) {
            if (emf == null) {
                emf = Persistence.createEntityManagerFactory("dbconnectionPU");
            }
        }
    }

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

    public boolean isEmpty() {
        return findAll().isEmpty();
    }

    public void seedDefaultBooks() {
        if (!isEmpty()) return;
        List<Book> books = new ArrayList<>();
        books.add(new Book("George Orwell", "1984"));
        books.add(new Book("Harper Lee", "To Kill a Mockingbird"));
        books.add(new Book("F. Scott Fitzgerald", "The Great Gatsby"));
        books.add(new Book("Jane Austen", "Pride and Prejudice"));
        books.add(new Book("J.R.R. Tolkien", "The Hobbit"));
        books.add(new Book("Mary Shelley", "Frankenstein"));
        books.add(new Book("Markus Zusak", "The Book Thief"));
        books.add(new Book("Gabriel García Márquez", "One Hundred Years of Solitude"));
        books.add(new Book("Leo Tolstoy", "War and Peace"));
        books.add(new Book("Homer", "The Odyssey"));

        for (Book b : books) {
            insert(b);
        }
    }
}

package DAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import odb.Book;
import odb.Library_Book;

public class LibraryBookDAO {

    private EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("com.mycompany_dbconnection_jar_1.0-SNAPSHOTPU");
    
        //get the first book that has copies to the user 
    public Library_Book asigh_book(Book b1)
    {
        Library_Book lb=new Library_Book();
        List<Library_Book> lb1=findByBookId(b1.getId());//get all books tha have this id
    
        if(lb1!=null)
        {
            for(Library_Book b:lb1)
            {
                //assign to the first one i found
                if(b.getAvailable_Copies()>0)
                {
                    lb=b;
                    break;
                }
            }
        }
        return lb;
    }
    public void insert(Library_Book libraryBook) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            em.persist(libraryBook);
            em.getTransaction().commit();


        }catch(Exception e){
            System.out.println("error you may insert in duplication");
            System.out.println(e);
        } 
        finally {
            em.close();
        }
    }

    public Library_Book findById(Long id) {
        EntityManager em = emf.createEntityManager();

        try {
            return em.find(Library_Book.class, id);
        } finally {
            em.close();
        }
    }

    public List<Library_Book> findAll() {
        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery(
                    "SELECT lb FROM Library_Book lb",
                    Library_Book.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public void update(Library_Book libraryBook) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            em.merge(libraryBook);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            Library_Book libraryBook = em.find(Library_Book.class, id);

            if (libraryBook != null) {
                em.remove(libraryBook);
            }

            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<Library_Book> findByLibraryId(long libraryId) {
        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery(
                    "SELECT lb FROM Library_Book lb WHERE lb.l.id = :libraryId",
                    Library_Book.class
            ).setParameter("libraryId", libraryId)
             .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Library_Book> findByBookId(long bookId) {
        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery(
                    "SELECT lb FROM Library_Book lb WHERE lb.B.id = :bookId",
                    Library_Book.class
            ).setParameter("bookId", bookId)
             .getResultList();
        } finally {
            em.close();
        }
    }
}

package DAO;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import odb.Library;
import odb.Library_Book;
import odb.Book;

public class LibraryDAO {
     private static EntityManagerFactory emf;

     public LibraryDAO() {
         synchronized (LibraryDAO.class) {
             if (emf == null) {
                 emf = Persistence.createEntityManagerFactory("dbconnectionPU");
             }
         }
     }

    public void insert(Library l){
        EntityManager em = emf.createEntityManager();
        try{
            em.getTransaction().begin();
            em.persist(l);
            em.getTransaction().commit();
        }finally{
            em.close();
        }
    }
    public Library findById(long id){
         EntityManager em = emf.createEntityManager();
         try{
            return em.find(Library.class, id);
         }finally{
             em.close();
         }
    }
    public void delete(long id){
         EntityManager em = emf.createEntityManager();
         try{
             Library l = em.find(Library.class, id);
             if(l!=null){
               em.getTransaction().begin();
               em.remove(l);
             }
               em.getTransaction().commit();
               
         }finally{
             em.close();
         }
         
    }
    public void update(Library l){
        EntityManager em = emf.createEntityManager();
        try{
            em.getTransaction().begin();

            em.merge(l);

            em.getTransaction().commit();
        } finally{
            em.close();
        }
    }
   public List<Library> finAll(){
        EntityManager em = emf.createEntityManager();
         try{
             return em.createQuery("SELECT l FROM Library l ", Library.class).getResultList();
    
    }finally{
             em.close();
         }
    }

    public boolean isEmpty() {
        return finAll().isEmpty();
    }

    public void seedDefaultLibrariesAndLinks() {
        if (!isEmpty()) return;
        Bookdao bookdao = new Bookdao();
        if (bookdao.isEmpty()) {
            bookdao.seedDefaultBooks();
        }
        List<Book> books = bookdao.findAll();
        if (books.isEmpty()) return;

        List<Library> libs = new ArrayList<>();
        libs.add(new Library("Central", "Central Library"));
        libs.add(new Library("East Wing", "East Branch"));
        libs.add(new Library("West Wing", "West Branch"));

        for (Library lib : libs) {
            insert(lib);
        }

        // Link books to libraries
        LibraryBookDAO lbdao = new LibraryBookDAO();
        List<Library> persisted = finAll();
        int bookIndex = 0;
        for (Library lib : persisted) {
            // assign 3 books per library with copies
            for (int i = 0; i < 3 && bookIndex < books.size(); i++, bookIndex++) {
                Book b = books.get(bookIndex);
                // avoid duplicates
                List<Library_Book> existing = lbdao.findByLibraryId(lib.getId());
                boolean found = false;
                for (Library_Book lb : existing) {
                    if (lb.getB().getId().equals(b.getId())) { found = true; break; }
                }
                if (found) continue;
                Library_Book lb = new Library_Book(5, 5, lib, b);
                lbdao.insert(lb);
            }
        }
    }
}

package DAO;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import odb.Library;

public class LibraryDAO {
     private static final EntityManagerFactory emf =
        Persistence.createEntityManagerFactory("com.mycompany_dbconnection_jar_1.0-SNAPSHOTPU");
     
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
             Library l=findById(id);
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
             Library l1=findById(l.getId());
             if(l1!=null){
                 em.getTransaction().begin();
                 em.merge(l1);
             }
             em.getTransaction().commit();
         }finally{
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
       
}

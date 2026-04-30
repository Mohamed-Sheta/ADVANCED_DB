package DAO;

import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import odb.Book;
import odb.Borrow;
import odb.Person;

public class BorrowDAO {

    private static EntityManagerFactory emf;

    public BorrowDAO() {
        synchronized (BorrowDAO.class) {
            if (emf == null) {
                emf = Persistence.createEntityManagerFactory("dbconnectionPU");
            }
        }
    }

    private boolean isnotinborrow(Borrow b) {
        List<Borrow> b1 = this.findByPersonId(b.getP().getId());
        for (Borrow v : b1) {
            if (Objects.equals(v.getLibrary_Book().getId(), b.getLibrary_Book().getId()))
                return false;
        }
        return true;
    }

    public void insert(Borrow borrow) {
        EntityManager em = emf.createEntityManager();

        try {
            //first handel if the user is inserting the same borrow twice as the borrow has it own id so i must check on the libid aand the person id is not duplicated
            if (isnotinborrow(borrow)) {
                em.getTransaction().begin();
                em.persist(borrow);
                em.getTransaction().commit();
            }
        } catch (Exception e) {
            System.out.println("you may have duplications");
        } finally {
            em.close();
        }
    }

    public Borrow findById(Long id) {
        EntityManager em = emf.createEntityManager();

        try {
            return em.find(Borrow.class, id);
        } finally {
            em.close();
        }
    }

    public List<Borrow> findAll() {
        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery(
                    "SELECT b FROM Borrow b",
                    Borrow.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public void update(Borrow borrow) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            em.merge(borrow);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            Borrow borrow = em.find(Borrow.class, id);

            if (borrow != null) {
                em.remove(borrow);
            }

            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<Borrow> findByPersonId(Long personId) {
        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery(
                            "SELECT b FROM Borrow b WHERE b.p.id = :personId",
                            Borrow.class
                    ).setParameter("personId", personId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Borrow> findByLibraryBookId(Long libraryBookId) {
        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery(
                            "SELECT b FROM Borrow b WHERE b.Library_Book.id = :libraryBookId",
                            Borrow.class
                    ).setParameter("libraryBookId", libraryBookId)
                    .getResultList();
        } finally {
            em.close();
        }
    }


    public Borrow searchforpersonandbook(Person p, Book b1) {
        List<Borrow> b = this.findByPersonId(p.getId());
        for (Borrow r : b) {
            if (Objects.equals(r.getLibrary_Book().getB().getId(), b1.getId())) {
                return r;
            }
        }
        return null;
    }
}

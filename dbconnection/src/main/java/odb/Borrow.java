package odb;

import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Table(
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"LibraryBook_id", "Person_id"}
    )
)
@Entity
public class Borrow implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
     @JoinColumn(name="Person_id", nullable = false)
    private Person p;
    
     @ManyToOne
     @JoinColumn(name="LibraryBook_id", nullable = false)
     Library_Book Library_Book;

    @Column(name="BorrowDate")
    private LocalDate borrowDate;

    @Column(name="ReturnDate")
    private LocalDate returnDate;

    @Column(name="Status")
    private String status;

    public Borrow() {
    }

    public Borrow(Person p) {
        this.p = p;
    }

    public Person getP() {
        return p;
    }

    public void setP(Person p) {
        this.p = p;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Borrow)) {
            return false;
        }
        Borrow other = (Borrow) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    public Library_Book getLibrary_Book() {
        return Library_Book;
    }

    public void setLibrary_Book(Library_Book Library_Book) {
        this.Library_Book = Library_Book;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "odb.Borrow[ id=" + id + " ]";
    }
    
}

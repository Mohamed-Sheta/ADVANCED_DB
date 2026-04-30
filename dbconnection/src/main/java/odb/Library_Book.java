package odb;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Table(
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"library_id", "book_id"}
    )
)
@Entity
public class Library_Book implements Serializable {


    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="Total_Copies")//for all copies we have not right now in general
    private int Total_Copies;
    
     @Column(name="Available_Copies")//for all copies we have right now
    private int Available_Copies;
     
    @ManyToOne
    @JoinColumn(name = "library_id", nullable = false)
    private Library l;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book B;
    
    @OneToMany(mappedBy="Library_Book")
    List<Borrow> Borrow;

    public Library_Book() {
    }

    public Library_Book(int Total_Copies, int Available_Copies, Library l, Book B) {
        if(Total_Copies<Available_Copies){
            System.out.println("error in the entries");
                this.Total_Copies=0;
        this.Available_Copies=0;
        }
        else{
        this.Total_Copies = Total_Copies;
        this.Available_Copies = Available_Copies;
        }
    
        this.l = l;
        this.B = B;
    }

    public Library getL() {
        return l;
    }

    public void setL(Library l) {
        this.l = l;
    }

    public Book getB() {
        return B;
    }

    public void setB(Book B) {
        this.B = B;
    }

    public int getTotal_Copies() {
        return Total_Copies;
    }

    public void setTotal_Copies(int Total_Copies) {
        this.Total_Copies = Total_Copies;
    }

    public int getAvailable_Copies() {
        return Available_Copies;
    }

    public void setAvailable_Copies(int Available_Copies) {
        this.Available_Copies = Available_Copies;
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
        if (!(object instanceof Library_Book)) {
            return false;
        }
        Library_Book other = (Library_Book) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "odb.Library_Book[ " + this.Available_Copies + " ]";
    }
    
}

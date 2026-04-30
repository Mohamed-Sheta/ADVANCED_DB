package odb;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Library implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
     @Column(name="Location")
    private String Loction;
      @Column(name="Name")
    private String Name;
      @OneToMany(mappedBy="l")
      List<Library_Book> Library_Book;

    public Library() {
    }

    public Library(String Loction, String Name) {
        this.Loction = Loction;
        this.Name = Name;
    }
    public String getLoction() {
        return Loction;
    }

    public void setLoction(String Loction) {
        this.Loction = Loction;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
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
        if (!(object instanceof Library)) {
            return false;
        }
        Library other = (Library) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "odb.Library[ "+"Location"+this.Loction+"Name"+this.Name+ " ]";
    }
    
}

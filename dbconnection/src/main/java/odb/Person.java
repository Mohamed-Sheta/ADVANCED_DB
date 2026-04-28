package odb;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@Entity
public class Person implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id; 
    
    @Column(name="Name")
    private String Name;
    
    @Column(name="Email")
    private String Email;
    
    @Column(name="Password")
    private String Passwod;

    @Column(name="SSN")
    private String Ssn;

    @Column(name="Phone")
    private String Phone;

    @Column(name="Address")
    private String Address;

    @Column(name="Gender")
    private String Gender;
    
    @OneToMany(mappedBy="p")
    List<Borrow> b;//this is for the relation between the borrow and the person and this not the relation owner this make the person id in the table borrow as an foreign key

    public Person(String Name, String Email, String Passwod, String Ssn, String Phone, String Address, String Gender) {
        this.Name = Name;
        this.Email = Email;
        this.Passwod = Passwod;
        this.Ssn = Ssn;
        this.Phone = Phone;
        this.Address = Address;
        this.Gender = Gender;
    }

    public List<Borrow> getB() {
        return b;
    }

    public void setB(List<Borrow> b) {
        this.b = b;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }

    public String getPasswod() {
        return Passwod;
    }

    public void setPasswod(String Passwod) {
        this.Passwod = Passwod;
    }

    public String getSsn() {
        return Ssn;
    }

    public void setSsn(String Ssn) {
        this.Ssn = Ssn;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String Phone) {
        this.Phone = Phone;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String Address) {
        this.Address = Address;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String Gender) {
        this.Gender = Gender;
    }

    @PrePersist
    @PreUpdate
    private void hashPasswordIfNeeded() {
        if (Passwod == null || Passwod.isEmpty()) {
            return;
        }
        if (isSha256Hex(Passwod)) {
            return;
        }
        Passwod = sha256Hex(Passwod);
    }

    private static boolean isSha256Hex(String value) {
        if (value.length() != 64) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            boolean isDigit = c >= '0' && c <= '9';
            boolean isLower = c >= 'a' && c <= 'f';
            boolean isUpper = c >= 'A' && c <= 'F';
            if (!(isDigit || isLower || isUpper)) {
                return false;
            }
        }
        return true;
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
    
    public Person() {
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
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Person)) {
            return false;
        }
        Person other = (Person) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "odb.Person[ id=" + id + " ]";
    }
    
}

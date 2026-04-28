package com.mycompany.dbconnection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import odb.Book;
import odb.Borrow;
import odb.Library;
import odb.Library_Book;
import odb.Person;

public class dbsetconnetion {
    public void setconn(){
        inserting_borrowsbooks c=new inserting_borrowsbooks();
        c.start();
        c.startL();
        c.startlb();
        c.startp();
    }
}

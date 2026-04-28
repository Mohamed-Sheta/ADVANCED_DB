package com.mycompany.dbconnection;

import DAO.Bookdao;
import DAO.LibraryBookDAO;
import DAO.LibraryDAO;
import DAO.PersonDAO;
import odb.Book;
import odb.Library;
import odb.Library_Book;
import odb.Person;

public class inserting_borrowsbooks {
Book b1 = new Book("Robert C. Martin", "Clean Code");
Book b2 = new Book("Joshua Bloch", "Effective Java");
Book b3 = new Book("Erich Gamma", "Design Patterns");
Book b4 = new Book("Abraham Silberschatz", "Database System Concepts");
Book b5 = new Book("Christian Bauer", "Java Persistence with Hibernate");
Book b6 = new Book("Martin Fowler", "Refactoring");
Book b7 = new Book("Brian Goetz", "Java Concurrency in Practice");
Book b8 = new Book("Herbert Schildt", "Java: The Complete Reference");
Book b9 = new Book("Andrew S. Tanenbaum", "Computer Networks");
Book b10 = new Book("Thomas H. Cormen", "Introduction to Algorithms");
Book b11 = new Book("Robert C. Martin", "Clean Architecture");
Book b12 = new Book("Martin Kleppmann", "Designing Data-Intensive Applications");
Bookdao dao = new Bookdao();
   void start(){

//insertion
dao.insert(b1);
dao.insert(b2);
dao.insert(b3);
dao.insert(b4);
dao.insert(b5);
dao.insert(b6);
dao.insert(b7);
dao.insert(b8);
dao.insert(b9);
dao.insert(b10);
dao.insert(b11);
dao.insert(b12);

   }
   void startp(){
 Person p1 = new Person("Mostafa", "mostafa@gmail.com", "123456", "302-11-5001", "+20 10 1000 2000", "Nasr City", "Male");
Person p2 = new Person("Ahmed", "ahmed@gmail.com", "abcdef", "302-11-5002", "+20 10 2000 3000", "Fifth Settlement", "Male");
Person p3 = new Person("Ali", "ali@gmail.com", "pass123", "302-11-5003", "+20 10 3000 4000", "Zamalek", "Male");
Person p4 = new Person("Sara", "sara@gmail.com", "qwerty", "302-11-5004", "+20 10 4000 5000", "Heliopolis", "Female");
Person p5 = new Person("Mona", "mona@gmail.com", "zxcvbn", "302-11-5005", "+20 10 5000 6000", "Maadi", "Female");
PersonDAO dao = new PersonDAO();
//iserting persons
dao.insert(p1);
dao.insert(p2);
dao.insert(p3);
dao.insert(p4);
dao.insert(p5);
//making some borrows:
//this is action the user can make 
dao.borrowBook(p1,b1);
dao.borrowBook(p1,b2);
dao.rollback(p1, b1);
   }
    Library l1=new Library("Nasr city","samir&aly");
    Library l2=new Library("fifthstatment","safty");
    Library l3=new Library("firthstatment","elbasha");
    Library l4=new Library("elshrouk","exotic");
    Library l5=new Library("downtown","heritage");
    Library l6=new Library("new cairo","knowledge hub");
void startL(){

    LibraryDAO dao=new LibraryDAO();
    dao.insert(l1);
    dao.insert(l2);
    dao.insert(l3);
    dao.insert(l4);
    dao.insert(l5);
    dao.insert(l6);
}
void startlb(){
    Library_Book lb1=new Library_Book(8,7,l1,b1);
    Library_Book lb2=new Library_Book(10,9,l2,b1);
    Library_Book lb3=new Library_Book(11,10,l3,b1);
    Library_Book lb4=new Library_Book(5,4,l3,b5);
    Library_Book lb5=new Library_Book(6,5,l3,b2);
    Library_Book lb6=new Library_Book(6,5,l3,b3);
    Library_Book lb7=new Library_Book(9,8,l4,b4);
    Library_Book lb8=new Library_Book(7,6,l5,b6);
    Library_Book lb9=new Library_Book(12,11,l5,b9);
    Library_Book lb10=new Library_Book(4,3,l6,b10);
    Library_Book lb11=new Library_Book(6,5,l6,b11);
    Library_Book lb12=new Library_Book(8,7,l2,b12);
    
    LibraryBookDAO dao=new LibraryBookDAO();
    dao.insert(lb1);
    dao.insert(lb2);
    dao.insert(lb3);
    dao.insert(lb4);
    dao.insert(lb5);
    dao.insert(lb6);
    dao.insert(lb7);
    dao.insert(lb8);
    dao.insert(lb9);
    dao.insert(lb10);
    dao.insert(lb11);
    dao.insert(lb12);


    
}  
}

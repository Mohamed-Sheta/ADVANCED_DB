package Services;

import DAO.LibraryBookDAO;
import DAO.PersonDAO;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import odb.Book;
import odb.Library_Book;
import odb.Person;

public class BorrowService {
    private final PersonDAO personDAO = new PersonDAO();
    private final LibraryBookDAO libraryBookDAO = new LibraryBookDAO();

    public void borrowBooks(Person person, List<Book> books) {
        if (person == null || books == null) {
            return;
        }
        for (Book book : books) {
            personDAO.borrowBook(person, book);
        }
    }

    public void borrowLibraryBooks(Person person, List<Long> libraryBookIds) {
        if (person == null || libraryBookIds == null || libraryBookIds.isEmpty()) {
            return;
        }

        List<Library_Book> libraryBooks = libraryBookIds.stream()
                .map(libraryBookDAO::findById)
                .filter(Objects::nonNull)
            .collect(Collectors.toList());

        boolean hasUnavailable = libraryBooks.stream()
                .anyMatch(lb -> lb.getAvailable_Copies() <= 0);
        if (hasUnavailable || libraryBooks.size() != libraryBookIds.size()) {
            throw new IllegalStateException("One or more selected books are no longer available.");
        }

        for (Library_Book libraryBook : libraryBooks) {
            personDAO.borrowLibraryBook(person, libraryBook.getId());
        }
    }
}

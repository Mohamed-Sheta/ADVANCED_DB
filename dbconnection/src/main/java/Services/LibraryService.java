package Services;

import DAO.LibraryBookDAO;
import DAO.LibraryDAO;
import DAO.Bookdao;
import java.util.List;
import odb.Library;
import odb.Library_Book;

public class LibraryService {
    private final LibraryDAO libraryDAO = new LibraryDAO();
    private final LibraryBookDAO libraryBookDAO = new LibraryBookDAO();

    public LibraryService() {
        // Seed data if empty
        Bookdao bookdao = new Bookdao();
        bookdao.seedDefaultBooks();
        libraryDAO.seedDefaultLibrariesAndLinks();
    }

    public List<Library> getLibraries() {
        return libraryDAO.finAll();
    }

    public List<Library_Book> getBooksForLibrary(long libraryId) {
        return libraryBookDAO.findByLibraryId(libraryId);
    }
}

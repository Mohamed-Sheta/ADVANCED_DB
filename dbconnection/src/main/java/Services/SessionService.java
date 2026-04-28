package Services;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import odb.Person;

public class SessionService {
    private Person currentUser;
    private final BooleanProperty showLibraryCovers = new SimpleBooleanProperty(true);
    private final BooleanProperty showLibraryLocations = new SimpleBooleanProperty(true);

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public Person getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(Person currentUser) {
        this.currentUser = currentUser;
    }

    public void clear() {
        currentUser = null;
    }

    public boolean isShowLibraryCovers() {
        return showLibraryCovers.get();
    }

    public void setShowLibraryCovers(boolean value) {
        showLibraryCovers.set(value);
    }

    public BooleanProperty showLibraryCoversProperty() {
        return showLibraryCovers;
    }

    public boolean isShowLibraryLocations() {
        return showLibraryLocations.get();
    }

    public void setShowLibraryLocations(boolean value) {
        showLibraryLocations.set(value);
    }

    public BooleanProperty showLibraryLocationsProperty() {
        return showLibraryLocations;
    }
}

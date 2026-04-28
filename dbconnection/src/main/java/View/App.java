package View;

import Services.BorrowService;
import Services.LibraryService;
import Services.PersonService;
import Services.SessionService;
import com.mycompany.dbconnection.dbsetconnetion;
import java.util.ArrayDeque;
import java.util.Deque;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import odb.Library;

public class App extends Application {
    private Stage stage;
    private final Deque<Runnable> history = new ArrayDeque<>();
    private Runnable currentScreen;
    private final SessionService sessionService = new SessionService();
    private final PersonService personService = new PersonService();
    private final LibraryService libraryService = new LibraryService();
    private final BorrowService borrowService = new BorrowService();

    public static void main(String[] args) {
        dbsetconnetion d=new dbsetconnetion();
        d.setconn();
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Library App");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
        applyWindowSize();
        displayHome();
        stage.show();
    }

    private void setScene(Parent root) {
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/application.css").toExternalForm());
        stage.setScene(scene);
        applyWindowSize();
    }

    private void applyWindowSize() {
        var bounds = Screen.getPrimary().getVisualBounds();
        stage.setResizable(false);
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
    }

    private void navigateTo(Runnable screen) {
        if (currentScreen != null) {
            history.push(currentScreen);
        }
        screen.run();
    }

    private void goBack() {
        if (history.isEmpty()) {
            return;
        }
        history.pop().run();
    }

    private boolean canGoBack() {
        return !history.isEmpty();
    }

    private void showHome() {
        navigateTo(this::displayHome);
    }

    private void displayHome() {
        currentScreen = this::displayHome;
        HomeView view = new HomeView(
                libraryService,
                sessionService,
                this::showProfile,
                this::showSettings,
                this::showLibraryDetail,
                this::showLogin
        );
        setScene(view.getRoot());
    }

    private void showSettings() {
        navigateTo(this::displaySettings);
    }

    private void displaySettings() {
        currentScreen = this::displaySettings;
        SettingsView view = new SettingsView(
                sessionService,
                canGoBack(),
                this::goBack
        );
        setScene(view.getRoot());
    }

    private void showProfile() {
        navigateTo(this::displayProfile);
    }

    private void displayProfile() {
        currentScreen = this::displayProfile;
        ProfileView view = new ProfileView(
                sessionService,
            personService,
                canGoBack(),
                this::goBack,
            this::showSettings,
            this::showHome,
                this::showLogin,
                this::showRegister
        );
        setScene(view.getRoot());
    }

    private void showRegister() {
        navigateTo(this::displayRegister);
    }

    private void displayRegister() {
        currentScreen = this::displayRegister;
        RegisterView view = new RegisterView(
                personService,
                sessionService,
                canGoBack(),
                this::goBack,
                this::showHome,
                this::showLogin
        );
        setScene(view.getRoot());
    }

    private void showLogin() {
        navigateTo(this::displayLogin);
    }

    private void displayLogin() {
        currentScreen = this::displayLogin;
        LoginView view = new LoginView(
                personService,
                sessionService,
                canGoBack(),
                this::goBack,
                this::showHome,
                this::showRegister
        );
        setScene(view.getRoot());
    }

    private void showLibraryDetail(Library library) {
        navigateTo(() -> displayLibraryDetail(library));
    }

    private void displayLibraryDetail(Library library) {
        currentScreen = () -> displayLibraryDetail(library);
        LibraryDetailView view = new LibraryDetailView(
                library,
                libraryService,
                borrowService,
                sessionService,
                canGoBack(),
                this::goBack,
            this::showSettings,
            this::showProfile,
            this::showLogin,
            this::showRegister
        );
        setScene(view.getRoot());
    }

}

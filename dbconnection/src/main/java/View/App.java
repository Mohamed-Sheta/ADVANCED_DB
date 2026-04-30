package View;

import Services.BorrowService;
import Services.LibraryService;
import Services.PersonService;
import Services.SessionService;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import java.net.URL;
import odb.Library;

public class App extends Application {
    private Stage stage;
    private final Deque<Runnable> history = new ArrayDeque<>();
    private Runnable currentScreen;
    private String currentScreenName = null;
    private SessionService sessionService;
    private PersonService personService;
    private LibraryService libraryService;
    private BorrowService borrowService;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.sessionService = new SessionService();
        this.personService = new PersonService();
        this.libraryService = new LibraryService();
        this.borrowService = new BorrowService();

        this.stage = stage;
        stage.setTitle("Library App");

        InputStream iconStream = getClass().getResourceAsStream("/images/app_icon.png");
        if (iconStream != null) {
            stage.getIcons().add(new Image(iconStream));
        }

        applyWindowSize();
        displayHome();
        stage.show();
    }

    private void setScene(Parent root) {
        if (root == null) {
            System.out.println("setScene called with null root - aborting to avoid blank screen");
            return;
        }
        System.out.println("setScene: incoming root class=" + root.getClass().getName() + " childrenCount=" + root.getChildrenUnmodifiable().size());
        Platform.runLater(() -> {
            try {
                Scene scene = stage.getScene();
                URL css = getClass().getResource("/application.css");
                if (scene == null) {
                    scene = new Scene(root);
                    if (css != null) scene.getStylesheets().add(css.toExternalForm());
                    stage.setScene(scene);
                    System.out.println("Created new Scene and set root. currentScreen=" + currentScreenName + " sceneRootChildren=" + (stage.getScene() != null ? stage.getScene().getRoot().getChildrenUnmodifiable().size() : -1));
                } else {
                    scene.setRoot(root);
                    if (css != null && !scene.getStylesheets().contains(css.toExternalForm())) {
                        scene.getStylesheets().add(css.toExternalForm());
                    }
                    System.out.println("Replaced existing Scene root. currentScreen=" + currentScreenName + " sceneRootChildren=" + (stage.getScene() != null ? stage.getScene().getRoot().getChildrenUnmodifiable().size() : -1));
                }
                applyWindowSize();
            } catch (Exception ex) {
                System.out.println("Error while setting scene root: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    private void applyWindowSize() {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setResizable(false);
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
    }

    private void navigateTo(Runnable screen) {
        System.out.println("navigateTo called. currentScreenName=" + currentScreenName + " target=" + screen);
        try {
            if (currentScreen != null) {
                history.push(currentScreen);
            }
            screen.run();
        } catch (Exception ex) {
            System.out.println("navigateTo failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void goBack() {
        if (history.isEmpty()) {
            System.out.println("goBack called but history is empty");
            return;
        }
        System.out.println("goBack: popping history and running");
        try {
            Runnable prev = history.pop();
            prev.run();
        } catch (Exception ex) {
            System.out.println("goBack failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private boolean canGoBack() {
        return !history.isEmpty();
    }

    private void showHome() {
        navigateTo(this::displayHome);
    }

    private void displayHome() {
        System.out.println("Creating HomeView");
        currentScreen = this::displayHome;
        currentScreenName = "home";
        HomeView view = new HomeView(
                libraryService,
                sessionService,
                this::showProfile,
                this::showSettings,
                this::showLibraryDetail,
                this::showLogin
        );
        if (view == null || view.getRoot() == null) {
            System.out.println("HomeView creation failed: root is null");
            return;
        }
        setScene(view.getRoot());
    }

    private void showSettings() {
        navigateTo(this::displaySettings);
    }

    private void displaySettings() {
        System.out.println("Creating SettingsView");
        currentScreen = this::displaySettings;
        currentScreenName = "settings";
        SettingsView view = new SettingsView(
                sessionService,
                canGoBack(),
                this::goBack
        );
        if (view == null || view.getRoot() == null) {
            System.out.println("SettingsView creation failed: root is null");
            return;
        }
        setScene(view.getRoot());
    }

    private void showProfile() {
        System.out.println("showProfile called; loggedIn=" + (sessionService != null && sessionService.isLoggedIn()));
        if (sessionService == null || !sessionService.isLoggedIn()) {
            System.out.println("User not logged in - navigating to login");
            navigateTo(this::displayLogin);
            return;
        }
        navigateTo(this::displayProfile);
    }

    private void displayProfile() {
        System.out.println("Creating ProfileView");
        currentScreen = this::displayProfile;
        currentScreenName = "profile";
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
        if (view == null || view.getRoot() == null) {
            System.out.println("ProfileView creation failed: root is null");
            return;
        }
        setScene(view.getRoot());
    }

    private void showRegister() {
        navigateTo(this::displayRegister);
    }

    private void displayRegister() {
        System.out.println("Creating RegisterView");
        currentScreen = this::displayRegister;
        currentScreenName = "register";
        RegisterView view = new RegisterView(
                personService,
                sessionService,
                canGoBack(),
                this::goBack,
                this::showHome,
                this::showLogin
        );
        if (view == null || view.getRoot() == null) {
            System.out.println("RegisterView creation failed: root is null");
            return;
        }
        setScene(view.getRoot());
    }

    private void showLogin() {
        navigateTo(this::displayLogin);
    }

    private void displayLogin() {
        System.out.println("Creating LoginView");
        currentScreen = this::displayLogin;
        currentScreenName = "login";
        LoginView view = new LoginView(
                personService,
                sessionService,
                canGoBack(),
                this::goBack,
                this::showHome,
                this::showRegister
        );
        if (view == null || view.getRoot() == null) {
            System.out.println("LoginView creation failed: root is null");
            return;
        }
        setScene(view.getRoot());
    }

    private void showLibraryDetail(Library library) {
        navigateTo(() -> displayLibraryDetail(library));
    }

    private void displayLibraryDetail(Library library) {
        System.out.println("Creating LibraryDetailView");
        currentScreen = () -> displayLibraryDetail(library);
        currentScreenName = "libraryDetail";
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
        if (view == null || view.getRoot() == null) {
            System.out.println("LibraryDetailView creation failed: root is null");
            return;
        }
        setScene(view.getRoot());
    }
}
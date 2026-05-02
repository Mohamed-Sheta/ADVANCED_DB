package View;

import Services.PersonService;
import Services.SessionService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import DAO.BorrowDAO;
import DAO.PersonDAO;
import odb.Person;
import odb.Borrow;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import odb.Book;

public class ProfileView {
    private final BorderPane root = new BorderPane();
    private final Runnable onSettings;
    private static final String BACK_ICON = "/images/arrow_back.png";
    private static final String SETTINGS_ICON = "/images/settings_icon.png";

    public ProfileView(
            SessionService sessionService,
            PersonService personService,
            boolean canGoBack,
            Runnable onBack,
            Runnable onSettings,
            Runnable onHome,
            Runnable onLogin,
            Runnable onRegister
    ) {
        System.out.println("Constructing ProfileView; loggedIn=" + (sessionService != null && sessionService.isLoggedIn()));
        root.getStyleClass().add("app-root");
        root.setPadding(Insets.EMPTY);
        this.onSettings = onSettings;

        root.setTop(buildTopBar(sessionService, canGoBack, onBack, onHome));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("main-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox page = new VBox(24);
        page.getStyleClass().add("profile-page");
        page.getChildren().add(buildProfileHero(sessionService));
        page.getChildren().add(buildMainPanel(sessionService, personService, onLogin, onRegister));
        page.getChildren().add(sessionService.isLoggedIn()
            ? buildLoggedInDetails(sessionService)
            : buildGuestBanner(onLogin, onRegister));

        scrollPane.setContent(page);
        root.setCenter(scrollPane);
    }

    private HBox buildTopBar(SessionService sessionService, boolean canGoBack, Runnable onBack, Runnable onHome) {
        HBox topBar = new HBox(16);
        topBar.getStyleClass().add("app-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button backButton = iconButton("arrow_back");
        java.io.InputStream bis = getClass().getResourceAsStream(BACK_ICON);
        if (bis != null) {
            try (java.io.InputStream is = bis) {
                Image backImg = new Image(is);
                ImageView biv = new ImageView(backImg);
                biv.setFitWidth(24);
                biv.setFitHeight(24);
                biv.setPreserveRatio(true);
                biv.setSmooth(true);
                biv.setCache(true);
                backButton.setGraphic(biv);
            } catch (Exception ex) {
                System.out.println("Failed to load back icon: " + BACK_ICON + " -> " + ex.getMessage());
                backButton.getStyleClass().add("icon-missing");
            }
        } else {
            System.out.println("Back icon not found: " + BACK_ICON);
            backButton.getStyleClass().add("icon-missing");
        }
        backButton.setDisable(!canGoBack);
        backButton.setOnAction(event -> onBack.run());

        Label title = new Label("Athenaeum Curator");
        title.getStyleClass().add("app-title");

        HBox nav = new HBox(28);
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.getChildren().addAll(
            createTopLink("Home", false, onHome),
            createTopLink("Profile", true, null)
        );

        Button settings = new Button();
        settings.getStyleClass().add("icon-button");
        ImageView settingsIv = createImageView(SETTINGS_ICON, 24, 24);
        if (settingsIv != null) settings.setGraphic(settingsIv);
        settings.setOnAction(event -> onSettings.run());

        Button signOut = new Button("Sign Out");
        signOut.getStyleClass().add("outline-button");
        signOut.setDisable(!sessionService.isLoggedIn());
        signOut.setOnAction(event -> {
            sessionService.clear();
            onHome.run();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(backButton, title, nav, spacer, settings, signOut);
        return topBar;
    }

    private VBox buildProfileHero(SessionService sessionService) {
        VBox hero = new VBox(6);
        hero.getStyleClass().add("profile-hero");

        Label title = new Label(sessionService.isLoggedIn() ? "Personal Information" : "Join the Curators");
        title.getStyleClass().add("hero-title");

        Label subtitle = new Label(sessionService.isLoggedIn()
                ? "Manage your library credentials and contact details."
                : "Access the world's most extensive digital archive.");
        subtitle.getStyleClass().add("hero-subtitle");

        hero.getChildren().addAll(title, subtitle);
        return hero;
    }

    private VBox buildMainPanel(SessionService sessionService, PersonService personService, Runnable onLogin, Runnable onRegister) {
        VBox panel = new VBox(18);
        panel.getStyleClass().add("profile-panel");

        HBox header = new HBox(12);
        header.setAlignment(Pos.TOP_LEFT);

        VBox titleBlock = new VBox(4);
        Label sectionTitle = new Label("Personal Information");
        sectionTitle.getStyleClass().add("section-title");
        Label sectionCaption = new Label(sessionService.isLoggedIn()
                ? "Your archivist account details are shown below."
                : "Sign in to view your archivist account details.");
        sectionCaption.getStyleClass().add("section-caption");
        titleBlock.getChildren().addAll(sectionTitle, sectionCaption);

        Button editButton = new Button(sessionService.isLoggedIn() ? "Edit Profile" : "Sign In");
        editButton.getStyleClass().add("primary-button");
        editButton.setGraphic(iconLabel(sessionService.isLoggedIn() ? "edit" : "login"));
        editButton.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        VBox detailsHolder = new VBox();
        detailsHolder.setFillWidth(true);

        Label message = new Label();
        message.getStyleClass().add("auth-message");

        Runnable showDetails = () -> {
            detailsHolder.getChildren().setAll(buildProfileDetailsGrid(sessionService.getCurrentUser()));
            message.setText("");
            editButton.setDisable(false);
        };

        Runnable showEdit = () -> {
            detailsHolder.getChildren().setAll(buildProfileEditForm(sessionService, personService, showDetails));
            message.setText("");
            editButton.setDisable(true);
        };

        editButton.setOnAction(event -> {
            if (sessionService.isLoggedIn()) {
                showEdit.run();
            } else {
                onLogin.run();
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(titleBlock, spacer, editButton);

        panel.getChildren().add(header);
        if (sessionService.isLoggedIn()) {
            showDetails.run();
            panel.getChildren().addAll(detailsHolder, message);
        } else {
            panel.getChildren().add(buildGuestActions(onLogin, onRegister));
        }
        return panel;
    }

    private VBox buildProfileEditForm(SessionService sessionService, PersonService personService, Runnable onDone) {
        Person person = sessionService.getCurrentUser();

        GridPane grid = new GridPane();
        grid.getStyleClass().add("profile-grid");
        grid.setHgap(24);
        grid.setVgap(18);

        TextField nameField = new TextField(person.getName());
        TextField emailField = new TextField(person.getEmail());
        TextField ssnField = new TextField(person.getSsn());
        TextField phoneField = new TextField(person.getPhone());
        TextField addressField = new TextField(person.getAddress());
        ComboBox<String> genderField = new ComboBox<>();
        genderField.getItems().addAll("Male", "Female");
        if (person.getGender() != null && !person.getGender().trim().isEmpty()) {
            genderField.setValue(person.getGender());
        }

        grid.addRow(0, inputBlock("FULL NAME", nameField));
        grid.addRow(1, inputBlock("EMAIL ADDRESS", emailField));
        grid.addRow(2, inputBlock("SSN", ssnField));
        grid.addRow(3, inputBlock("PHONE", phoneField));
        grid.addRow(4, inputBlock("ADDRESS", addressField));
        grid.addRow(5, inputBlock("GENDER", genderField));

        Label message = new Label();
        message.getStyleClass().add("auth-message");

        Button saveButton = new Button("Save Changes");
        saveButton.getStyleClass().add("primary-button");
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("outline-button");

        saveButton.setOnAction(event -> {
            try {
                personService.updateProfile(
                        person,
                        nameField.getText(),
                        emailField.getText(),
                        ssnField.getText(),
                        phoneField.getText(),
                        addressField.getText(),
                        genderField.getValue()
                );
                sessionService.setCurrentUser(person);
                onDone.run();
            } catch (RuntimeException ex) {
                message.setText(ex.getMessage());
            }
        });

        cancelButton.setOnAction(event -> onDone.run());

        HBox actions = new HBox(10, cancelButton, saveButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox form = new VBox(14, grid, actions, message);
        form.setFillWidth(true);
        return form;
    }

    private GridPane buildProfileDetailsGrid(Person person) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("profile-grid");
        grid.setHgap(24);
        grid.setVgap(18);

        grid.addRow(0, fieldBlock("FULL NAME", person.getName()));
        grid.addRow(1, fieldBlock("EMAIL ADDRESS", person.getEmail()));
        grid.addRow(2, fieldBlock("SSN", person.getSsn()));
        grid.addRow(3, fieldBlock("PHONE", person.getPhone()));
        grid.addRow(4, fieldBlock("ADDRESS", person.getAddress()));
        grid.addRow(5, fieldBlock("GENDER", person.getGender()));
        return grid;
    }

    private VBox buildGuestActions(Runnable onLogin, Runnable onRegister) {
        VBox guest = new VBox(14);
        guest.getStyleClass().add("guest-actions");

        Label note = new Label("You are not signed in.");
        note.getStyleClass().add("section-caption");

        Button loginButton = new Button("Sign In");
        loginButton.getStyleClass().add("primary-button");
        loginButton.setGraphic(iconLabel("login"));
        loginButton.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        loginButton.setOnAction(event -> onLogin.run());

        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("outline-button");
        registerButton.setGraphic(iconLabel("app_registration"));
        registerButton.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        registerButton.setOnAction(event -> onRegister.run());

        guest.getChildren().addAll(note, loginButton, registerButton);
        return guest;
    }

    private VBox buildLoggedInDetails(SessionService sessionService) {
        VBox details = new VBox(14);
        details.getStyleClass().add("profile-panel");

        Label title = new Label("Account Summary");
        title.getStyleClass().add("section-title");
        Label description = new Label("This account is active and ready for archive access.");
        description.getStyleClass().add("section-caption");

        Label feedbackLabel = new Label();
        feedbackLabel.getStyleClass().addAll("auth-message");

        TextField searchField = new TextField();
        searchField.setPromptText("Search borrowed books...");
        searchField.getStyleClass().add("auth-input");
        searchField.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "Borrowed", "Returned");
        statusFilter.setValue("All");
        statusFilter.getStyleClass().add("auth-input");
        statusFilter.setPrefWidth(160);

        HBox filterBar = new HBox(12, searchField, statusFilter);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        // Don't perform DAO calls on the JavaFX thread. Show a placeholder immediately
        VBox booksList = new VBox(12);
        booksList.getStyleClass().add("borrowed-books-list");
        Label loading = new Label("Loading borrowed books...");
        loading.getStyleClass().add("section-caption");
        booksList.getChildren().add(loading);

        // Populate the UI so the view renders immediately
        details.getChildren().addAll(title, description, filterBar, feedbackLabel, booksList);

        PersonDAO personDAO = new PersonDAO();
        BorrowDAO borrowDAO = new BorrowDAO();
        Long personId = sessionService.getCurrentUser() == null ? null : sessionService.getCurrentUser().getId();
        AtomicReference<List<Borrow>> allBorrowsRef = new AtomicReference<>(java.util.Collections.emptyList());
        AtomicReference<Runnable> refreshRef = new AtomicReference<>(() -> {
        });

        Consumer<List<Borrow>> renderList = (rows) -> {
            booksList.getChildren().clear();
            if (rows == null || rows.isEmpty()) {
                Label none = new Label("No books match your filter.");
                none.getStyleClass().add("section-caption");
                booksList.getChildren().add(none);
                return;
            }
            for (Borrow borrow : rows) {
                HBox row = buildBorrowEntry(borrow, sessionService, personDAO, feedbackLabel, refreshRef.get());
                booksList.getChildren().add(row);
            }
        };

        Runnable triggerFilter = () -> {
            String query = searchField.getText() == null ? "" : searchField.getText().trim();
            String status = statusFilter.getValue() == null ? "All" : statusFilter.getValue();

            Thread filterThread = new Thread(() -> {
                List<Borrow> base = allBorrowsRef.get();
                Set<Long> allowedBookIds = null;
                if (personId != null && !query.isEmpty()) {
                    List<Book> matches = personDAO.searchBorrowedBooksByText(personId, query);
                    Set<Long> ids = new HashSet<>();
                    if (matches != null) {
                        for (Book book : matches) {
                            if (book != null && book.getId() != null) {
                                ids.add(book.getId());
                            }
                        }
                    }
                    allowedBookIds = ids;
                }

                List<Borrow> filtered = new ArrayList<>();
                if (base != null) {
                    for (Borrow borrow : base) {
                        if (borrow == null) {
                            continue;
                        }

                        if (allowedBookIds != null) {
                            odb.Library_Book lb = borrow.getLibrary_Book();
                            Book book = lb == null ? null : lb.getB();
                            Long bookId = book == null ? null : book.getId();
                            if (bookId == null || !allowedBookIds.contains(bookId)) {
                                continue;
                            }
                        }

                        if ("Borrowed".equalsIgnoreCase(status) && !"BORROWED".equalsIgnoreCase(borrow.getStatus())) {
                            continue;
                        }
                        if ("Returned".equalsIgnoreCase(status) && !"RETURNED".equalsIgnoreCase(borrow.getStatus())) {
                            continue;
                        }

                        filtered.add(borrow);
                    }
                }

                javafx.application.Platform.runLater(() -> renderList.accept(filtered));
            });
            filterThread.setDaemon(true);
            filterThread.start();
        };

        refreshRef.set(triggerFilter);
        searchField.textProperty().addListener((obs, oldValue, newValue) -> triggerFilter.run());
        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> triggerFilter.run());

        // Load borrowed books in a background thread and update UI with Platform.runLater
        Thread loader = new Thread(() -> {
            try {
                List<Borrow> borrowedBooks = personId == null ? java.util.Collections.emptyList() : borrowDAO.findByPersonId(personId);
                allBorrowsRef.set(borrowedBooks == null ? java.util.Collections.emptyList() : borrowedBooks);

                javafx.application.Platform.runLater(() -> {
                    triggerFilter.run();
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    feedbackLabel.getStyleClass().removeAll("success-text");
                    feedbackLabel.getStyleClass().add("error-text");
                    feedbackLabel.setText(ex.getMessage());
                    booksList.getChildren().clear();
                    Label fail = new Label("Failed to load borrowed books.");
                    fail.getStyleClass().add("section-caption");
                    booksList.getChildren().add(fail);
                });
            }
        });
        loader.setDaemon(true);
        loader.start();

        return details;
    }

    // Build UI row for a single Borrow entry. The actual DB rollback is performed on a background thread;
    // UI updates are applied via Platform.runLater.
    private HBox buildBorrowEntry(Borrow borrow, SessionService sessionService, PersonDAO personDAO, Label feedbackLabel, Runnable onStatusChange) {
        odb.Library_Book lb = borrow.getLibrary_Book();
        String bookTitle = lb != null && lb.getB() != null ? lb.getB().getTitle() : "Unknown title";
        String bookAuthor = lb != null && lb.getB() != null ? lb.getB().getAuthor() : "Unknown author";

        Label bookLabel = new Label(bookTitle + " — " + bookAuthor + "  (Status: " + borrow.getStatus() + ")");
        bookLabel.getStyleClass().add("book-detail");

        Button returnButton = new Button("Return Book");
        returnButton.getStyleClass().add("return-button");
        returnButton.setDisable("RETURNED".equalsIgnoreCase(borrow.getStatus()));

        HBox bookEntry = new HBox(12);
        bookEntry.setAlignment(Pos.CENTER_LEFT);
        bookEntry.getChildren().addAll(bookLabel, returnButton);

        returnButton.setOnAction(evt -> {
            // Perform the potentially slow DB operation off the JavaFX thread
            Thread t = new Thread(() -> {
                try {
                    if (lb != null && lb.getB() != null) {
                        personDAO.rollback(sessionService.getCurrentUser(), lb.getB());
                    }
                    javafx.application.Platform.runLater(() -> {
                        feedbackLabel.getStyleClass().removeAll("error-text");
                        feedbackLabel.getStyleClass().add("success-text");
                        feedbackLabel.setText("Book returned successfully.");
                        borrow.setStatus("RETURNED");
                        bookLabel.setText(bookTitle + " — " + bookAuthor + "  (Status: RETURNED)");
                        returnButton.setDisable(true);
                        if (onStatusChange != null) {
                            onStatusChange.run();
                        }
                    });
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        feedbackLabel.getStyleClass().removeAll("success-text");
                        feedbackLabel.getStyleClass().add("error-text");
                        feedbackLabel.setText(ex.getMessage());
                    });
                }
            });
            t.setDaemon(true);
            t.start();
        });

        return bookEntry;
    }

    // Re-introduce the missing buildGuestBanner method expected by the constructor
    private VBox buildGuestBanner(Runnable onLogin, Runnable onRegister) {
        VBox guest = new VBox(14);
        guest.getStyleClass().add("guest-actions");

        Label title = new Label("Join the Curators");
        title.getStyleClass().add("hero-title");

        Label description = new Label("Access the world's most extensive digital archive and manage your collections.");
        description.getStyleClass().add("hero-subtitle");

        HBox actions = new HBox(12);

        Button loginButton = new Button("Sign In");
        loginButton.getStyleClass().add("primary-button");
        loginButton.setGraphic(iconLabel("login"));
        loginButton.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        loginButton.setOnAction(event -> onLogin.run());

        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("outline-button");
        registerButton.setGraphic(iconLabel("app_registration"));
        registerButton.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        registerButton.setOnAction(event -> onRegister.run());

        actions.getChildren().addAll(loginButton, registerButton);
        guest.getChildren().addAll(title, description, actions);
        return guest;
    }

    private VBox fieldBlock(String labelText, String valueText) {
        VBox block = new VBox(6);
        block.getStyleClass().add("profile-field");

        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");

        Label value = new Label(valueText);
        value.getStyleClass().add("field-value");

        block.getChildren().addAll(label, value);
        return block;
    }

    private VBox inputBlock(String labelText, Control control) {
        VBox block = new VBox(6);
        block.getStyleClass().add("profile-field");

        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");

        control.getStyleClass().add("auth-input");
        control.setMaxWidth(Double.MAX_VALUE);

        block.getChildren().addAll(label, control);
        return block;
    }

    private Button createTopLink(String text, boolean active, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("top-link");
        if (active) {
            button.getStyleClass().add("top-link-active");
        }
        if (action != null) {
            button.setOnAction(event -> action.run());
        }
        return button;
    }

    private Button iconButton(String icon) {
        Button button = new Button();
        button.getStyleClass().add("icon-button");
        button.setGraphic(iconLabel(icon));
        return button;
    }

    private ImageView createImageView(String path, double fitWidth, double fitHeight) {
        ImageView imageView = new ImageView();
        if (path == null || path.trim().isEmpty()) {
            imageView.getStyleClass().add("icon-missing");
            imageView.setFitWidth(fitWidth);
            imageView.setFitHeight(fitHeight);
            imageView.setPreserveRatio(true);
            return imageView;
        }

        String normalized = path.startsWith("/") ? path : "/" + path;
        try (java.io.InputStream is = getClass().getResourceAsStream(normalized)) {
            if (is != null) {
                try {
                    Image image = new Image(is);
                    imageView.setImage(image);
                } catch (Exception ex) {
                    System.out.println("Failed to construct Image from stream: " + normalized + " -> " + ex.getMessage());
                    imageView.getStyleClass().add("icon-missing");
                }
            } else {
                System.out.println("Image not found: " + normalized);
                imageView.getStyleClass().add("icon-missing");
            }
        } catch (Exception ex) {
            System.out.println("Error loading image: " + normalized + " -> " + ex.getMessage());
            imageView.getStyleClass().add("icon-missing");
        }

        imageView.setFitWidth(fitWidth);
        imageView.setFitHeight(fitHeight);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        return imageView;
    }

    private Label iconLabel(String icon) {
        Label label = new Label(icon);
        label.getStyleClass().add("material-symbol");
        return label;
    }


    public Parent getRoot() {
        return root;
    }
}

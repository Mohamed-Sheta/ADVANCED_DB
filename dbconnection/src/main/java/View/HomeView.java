package View;

import Services.LibraryService;
import Services.SessionService;
import Services.BorrowService;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import odb.Library;
import odb.Library_Book;

public class HomeView {
    private final BorderPane root = new BorderPane();
    private final SessionService sessionService;
    private final BorrowService borrowService = new BorrowService();
    private static final String SETTINGS_ICON = "/images/settings_icon.png";
    private static final String PROFILE_ICON = "/images/profile_icon.png";
    private static final String DEFAULT_BOOK_COVER = "/images/library/1_6Jp3vJWe7VFlFHZ9WhSJng.jpg";

    public HomeView(
            LibraryService libraryService,
            SessionService sessionService,
            Runnable onProfile,
            Runnable onSettings,
            Consumer<Library> onLibraryOpen,
            Runnable onLogin
    ) {
        root.getStyleClass().add("app-root");
        root.setPadding(Insets.EMPTY);
        this.sessionService = sessionService;

        root.setTop(buildTopBar(sessionService, onSettings, onProfile, onLogin));

        List<Library> libraries = libraryService.getLibraries();
        VBox content = new VBox(24);
        content.getStyleClass().add("home-content");

        TextField searchField = new TextField();
        searchField.getStyleClass().add("search-field");
        searchField.setPromptText("Search for your books...");

        VBox gridHolder = new VBox();
        gridHolder.getChildren().add(buildLibraryGrid(libraries, libraryService, onLibraryOpen, onLogin, ""));

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String query = normalizeSearchQuery(newVal);
            gridHolder.getChildren().setAll(buildLibraryGrid(libraries, libraryService, onLibraryOpen, onLogin, query));
        });

        content.getChildren().addAll(searchField, gridHolder);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("main-scroll");
        root.setCenter(scrollPane);
    }

    private Node buildTopBar(
            SessionService sessionService,
            Runnable onSettings,
            Runnable onProfile,
            Runnable onLogin
    ) {
        HBox bar = new HBox(12);
        bar.getStyleClass().add("app-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12));

        Label title = new Label("Athenaeum Curator");
        title.getStyleClass().add("app-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button settingsButton = createImageButton(SETTINGS_ICON, "Settings");
        if (settingsButton != null) settingsButton.setOnAction(event -> {
            System.out.println("Settings clicked");
            onSettings.run();
        });

        Button profileButton = createImageButton(PROFILE_ICON, "Profile");
        if (profileButton != null) profileButton.setOnAction(event -> {
            System.out.println("Profile clicked; loggedIn=" + sessionService.isLoggedIn());
            if (sessionService.isLoggedIn()) {
                onProfile.run();
            } else {
                onLogin.run();
            }
        });

        bar.getChildren().addAll(title, spacer, settingsButton, profileButton);
        return bar;
    }

    private Node buildLibraryGrid(
            List<Library> libraries,
            LibraryService libraryService,
            Consumer<Library> onLibraryOpen,
            Runnable onLogin,
            String bookTitleQuery
    ) {
        VBox wrapper = new VBox(18);
        wrapper.setPadding(new Insets(20));
        if (libraries == null || libraries.isEmpty()) {
            VBox emptyCard = new VBox(6);
            emptyCard.getStyleClass().add("card-empty");
            emptyCard.setAlignment(Pos.CENTER);
            Label title = new Label("No libraries found");
            title.getStyleClass().add("section-title");
            Label hint = new Label("No branches are available yet.");
            hint.getStyleClass().add("section-caption");
            emptyCard.getChildren().addAll(title, hint);
            return emptyCard;
        }

        boolean anyCardAdded = false;
        for (Library library : libraries) {
            VBox libCard = new VBox(12);
            libCard.getStyleClass().add("library-card-dark");
            libCard.setPadding(new Insets(14));

            HBox header = new HBox(12);
            VBox titleBlock = new VBox(4);
            Label name = new Label(library.getName());
            name.getStyleClass().add("card-title");
            Label location = new Label();
            location.getStyleClass().add("section-caption");
            String locationText = library.getLoction();
            if (locationText == null || locationText.trim().isEmpty()) {
                location.setText("");
                location.setVisible(false);
                location.setManaged(false);
            } else {
                location.setText("Location: " + locationText.trim());
                location.visibleProperty().bind(sessionService.showLibraryLocationsProperty());
                location.managedProperty().bind(sessionService.showLibraryLocationsProperty());
            }
            titleBlock.getChildren().addAll(name, location);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button viewButton = new Button("View Library");
            viewButton.getStyleClass().add("view-library-button");
            viewButton.setOnAction(e -> onLibraryOpen.accept(library));
            header.getChildren().addAll(titleBlock, spacer, viewButton);

            List<Library_Book> books = libraryService.getBooksForLibrary(library.getId());
            FlowPane bookGrid = new FlowPane();
            bookGrid.getStyleClass().add("book-grid");
            bookGrid.setHgap(16);
            bookGrid.setVgap(16);

            List<Library_Book> filteredBooks = filterBooksByTitle(books, bookTitleQuery);
            if (bookTitleQuery != null && !bookTitleQuery.isEmpty() && (filteredBooks == null || filteredBooks.isEmpty())) {
                continue;
            }

            if (filteredBooks == null || filteredBooks.isEmpty()) {
                VBox emptyBooks = new VBox(8);
                emptyBooks.setAlignment(Pos.CENTER_LEFT);
                emptyBooks.getStyleClass().add("empty-books");
                Label noBooks = new Label("No books available in this library.");
                noBooks.getStyleClass().add("empty-text");
                emptyBooks.getChildren().add(noBooks);
                libCard.getChildren().addAll(header, emptyBooks);
            } else {
                for (Library_Book lb : filteredBooks) {
                    Node bookCard = createBookCard(lb, onLogin);
                    bookGrid.getChildren().add(bookCard);
                }
                libCard.getChildren().addAll(header, bookGrid);
            }

            wrapper.getChildren().add(libCard);
            anyCardAdded = true;
        }

        if (!anyCardAdded) {
            VBox emptyCard = new VBox(6);
            emptyCard.getStyleClass().add("card-empty");
            emptyCard.setAlignment(Pos.CENTER);
            Label title = new Label("No matching books");
            title.getStyleClass().add("section-title");
            Label hint = new Label("Try a different title.");
            hint.getStyleClass().add("section-caption");
            emptyCard.getChildren().addAll(title, hint);
            return emptyCard;
        }

        return wrapper;
    }

    private List<Library_Book> filterBooksByTitle(List<Library_Book> books, String query) {
        if (books == null) {
            return null;
        }
        if (query == null || query.isEmpty()) {
            return books;
        }
        String normalized = query.toLowerCase();
        return books.stream()
                .filter(lb -> {
                    if (lb == null || lb.getB() == null || lb.getB().getTitle() == null) {
                        return false;
                    }
                    return lb.getB().getTitle().toLowerCase().contains(normalized);
                })
                .collect(Collectors.toList());
    }

    private String normalizeSearchQuery(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private Node createBookCard(Library_Book lb, Runnable onLogin) {
        VBox card = new VBox(10);
        card.getStyleClass().add("book-card");
        card.setPrefWidth(220);
        card.setPadding(new Insets(12));

        ImageView cover = createCoverImage(lb);
        cover.getStyleClass().add("book-cover");
        cover.visibleProperty().bind(sessionService.showLibraryCoversProperty());
        cover.managedProperty().bind(sessionService.showLibraryCoversProperty());

        VBox meta = new VBox(6);
        meta.getStyleClass().add("book-meta");

        Label title = new Label(lb.getB() == null ? "Untitled" : lb.getB().getTitle());
        title.getStyleClass().add("book-title");
        Label author = new Label(lb.getB() == null ? "Unknown" : lb.getB().getAuthor());
        author.getStyleClass().add("book-author");

        Label copies = new Label("Available: " + lb.getAvailable_Copies());
        copies.getStyleClass().add("book-copies");

        Button borrow = new Button("Borrow");
        borrow.getStyleClass().add("primary-button");
        borrow.setDisable(lb.getAvailable_Copies() <= 0);

        Label feedback = new Label();
        feedback.getStyleClass().addAll("feedback-text", "subtle-text");

        borrow.setOnAction(event -> {
            if (!sessionService.isLoggedIn()) {
                onLogin.run();
                return;
            }

            borrow.setDisable(true);
            feedback.setText("Processing...");

            Thread t = new Thread(() -> {
                try {
                    borrowService.borrowLibraryBooks(
                            sessionService.getCurrentUser(),
                            java.util.Collections.singletonList(lb.getId())
                    );
                    javafx.application.Platform.runLater(() -> {
                        feedback.getStyleClass().removeAll("error-text");
                        feedback.getStyleClass().add("success-text");
                        feedback.setText("Borrow request submitted");
                        copies.setText("Available: " + Math.max(0, lb.getAvailable_Copies() - 1));
                        borrow.setDisable(true);
                    });
                } catch (RuntimeException ex) {
                    javafx.application.Platform.runLater(() -> {
                        feedback.getStyleClass().removeAll("success-text");
                        feedback.getStyleClass().add("error-text");
                        feedback.setText(ex.getMessage());
                        borrow.setDisable(false);
                    });
                }
            });
            t.setDaemon(true);
            t.start();
        });

        meta.getChildren().addAll(title, author, copies, borrow, feedback);
        card.getChildren().addAll(cover, meta);

        return card;
    }

    private ImageView createCoverImage(Library_Book lb) {
        ImageView imageView = new ImageView();
        String[] tryPaths = new String[3];
        tryPaths[0] = (lb.getB() != null && lb.getB().getId() != null) ? "/images/library/" + lb.getB().getId() + ".jpg" : null;
        tryPaths[1] = (lb.getB() != null && lb.getB().getId() != null) ? "/images/library/" + lb.getB().getId() + ".png" : null;
        tryPaths[2] = DEFAULT_BOOK_COVER;

        Image img = null;
        for (String p : tryPaths) {
            if (p == null) continue;
            // ensure path starts with '/'
            String normalized = p.startsWith("/") ? p : "/" + p;
            try (java.io.InputStream is = getClass().getResourceAsStream(normalized)) {
                if (is != null) {
                    try {
                        img = new Image(is, 200, 140, true, true);
                        break;
                    } catch (Exception e) {
                        // proceed to next candidate
                    }
                }
            } catch (Exception e) {
                // ignore and continue to next path
            }
        }

        if (img == null) {
            // as a last resort try to load default placeholder directly
            try (java.io.InputStream is = getClass().getResourceAsStream(DEFAULT_BOOK_COVER)) {
                if (is != null) {
                    try {
                        img = new Image(is, 200, 140, true, true);
                    } catch (Exception e) {
                        img = null;
                    }
                }
            } catch (Exception e) {
                img = null;
            }
        }

        if (img != null) {
            imageView.setImage(img);
            imageView.setFitWidth(200);
            imageView.setFitHeight(140);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setCache(true);

            Rectangle clip = new Rectangle(200, 140);
            clip.setArcWidth(14);
            clip.setArcHeight(14);
            imageView.setClip(clip);
        } else {
            // No image found: produce an empty ImageView with styling so UI remains intact
            imageView.getStyleClass().add("book-cover-missing");
            imageView.setFitWidth(200);
            imageView.setFitHeight(140);
            imageView.setPreserveRatio(true);
            // add a simple transparent clip to keep layout
            Rectangle clip = new Rectangle(200, 140);
            clip.setArcWidth(14);
            clip.setArcHeight(14);
            imageView.setClip(clip);
        }

        return imageView;
    }

    private Button createImageButton(String imagePath, String accessibleText) {
        Button button = new Button();
        button.getStyleClass().add("icon-button");
        try {
            ImageView iv = createImageView(imagePath, 24, 24);
            if (iv != null) button.setGraphic(iv);
        } catch (Exception ex) {
            System.out.println("Failed to load image for button: " + imagePath + " -> " + ex.getMessage());
        }
        button.setAccessibleText(accessibleText);
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
        Image img = null;
        try (java.io.InputStream is = getClass().getResourceAsStream(normalized)) {
            if (is != null) {
                try {
                    img = new Image(is, fitWidth, fitHeight, true, true);
                } catch (Exception e) {
                    img = null;
                }
            }
        } catch (Exception e) {
            // ignore and try fallback
            img = null;
        }

        if (img == null) {
            // fallback to default book cover if available
            try (java.io.InputStream is = getClass().getResourceAsStream(DEFAULT_BOOK_COVER)) {
                if (is != null) {
                    try {
                        img = new Image(is, fitWidth, fitHeight, true, true);
                    } catch (Exception e) {
                        img = null;
                    }
                }
            } catch (Exception e) {
                img = null;
            }
        }

        if (img != null) {
            imageView.setImage(img);
        } else {
            imageView.getStyleClass().add("icon-missing");
        }
        imageView.setFitWidth(fitWidth);
        imageView.setFitHeight(fitHeight);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        return imageView;
    }

    public Parent getRoot() {
        return root;
    }
}

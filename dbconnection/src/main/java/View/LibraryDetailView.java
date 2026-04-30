package View;

import Services.BorrowService;
import Services.LibraryService;
import Services.SessionService;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import odb.Book;
import odb.Library;
import odb.Library_Book;

public class LibraryDetailView {
    private final BorderPane root = new BorderPane();
    private final Runnable onSettings;
    private final Runnable onProfile;
    private final Runnable onRegister;
    private final Runnable onLogin;
    private final SessionService sessionService;
    private final BorrowService borrowService;
    private static final String BACK_ICON = "/images/arrow_back.png";
    private static final String SETTINGS_ICON = "/images/settings_icon.png";
    private static final String PROFILE_ICON = "/images/profile_icon.png";
    private final ObservableList<BookRow> rows = FXCollections.observableArrayList();
    private final Label selectedCountLabel = new Label("0");
    private final Label messageLabel = new Label();
    private final TableView<BookRow> tableView = new TableView<>();

    public LibraryDetailView(
            Library library,
            LibraryService libraryService,
            BorrowService borrowService,
            SessionService sessionService,
            boolean canGoBack,
            Runnable onBack,
            Runnable onSettings,
            Runnable onProfile,
            Runnable onLogin,
            Runnable onRegister
    ) {
        root.getStyleClass().add("app-root");
        root.setPadding(Insets.EMPTY);
        this.onSettings = onSettings;
        this.onProfile = onProfile;
        this.onRegister = onRegister;
        this.onLogin = onLogin;
        this.sessionService = sessionService;
        this.borrowService = borrowService;

        root.setTop(buildTopBar(canGoBack, onBack));

        List<Library_Book> libraryBooks = libraryService.getBooksForLibrary(library.getId());
        if (libraryBooks != null) {
            for (Library_Book libraryBook : libraryBooks) {
                rows.add(new BookRow(libraryBook));
            }
        }

        VBox page = new VBox(24);
        page.getStyleClass().add("detail-page");
        page.setFillWidth(true);
        page.getChildren().addAll(
            buildHero(library),
            buildInventorySection(sessionService)
        );

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.getStyleClass().add("main-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        root.setCenter(scrollPane);
        updateSelectionSummary();
    }

    private HBox buildTopBar(boolean canGoBack, Runnable onBack) {
        HBox bar = new HBox(20);
        bar.getStyleClass().add("app-bar");
        bar.setAlignment(Pos.CENTER_LEFT);

        Button backButton = createImageButton(BACK_ICON, "Back");
        backButton.setDisable(!canGoBack);
        backButton.setOnAction(event -> onBack.run());

        Label brand = new Label("Athenaeum Curator");
        brand.getStyleClass().add("app-title");

        HBox left = new HBox(16, brand);
        left.setAlignment(Pos.CENTER_LEFT);

        Button settings = createImageButton(SETTINGS_ICON, "Settings");
        settings.setOnAction(event -> onSettings.run());
        Button account = createImageButton(PROFILE_ICON, "Profile");
        account.setOnAction(event -> {
            if (sessionService.isLoggedIn()) {
                onProfile.run();
            } else {
                onLogin.run();
            }
        });
        HBox actions = new HBox(8, settings, account);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bar.getChildren().addAll(backButton, left, spacer, actions);
        return bar;
    }

    private VBox buildHero(Library library) {
        VBox hero = new VBox(8);
        hero.getStyleClass().add("detail-hero");

        Label title = new Label(library.getName() + " Books");
        title.getStyleClass().add("hero-title");

        Label subtitle = new Label("Browsing the central repository of curated academic and historical volumes. Select the works you wish to commission for personal study.");
        subtitle.getStyleClass().add("hero-subtitle");

        hero.getChildren().addAll(title, subtitle);
        return hero;
    }

    private VBox buildInventorySection(SessionService sessionService) {
        VBox section = new VBox(16);
        section.getStyleClass().add("detail-section");

        HBox header = new HBox(12);
        header.getStyleClass().add("detail-section-header");

        VBox heading = new VBox(4);
        Label title = new Label("Inventory");
        title.getStyleClass().add("section-title");
        Label caption = new Label("Select the items you want to borrow from this branch.");
        caption.getStyleClass().add("section-caption");
        heading.getChildren().addAll(title, caption);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(heading, spacer);

        configureTable();
        tableView.setItems(rows);
        tableView.getStyleClass().add("catalog-table");
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tableView.setPlaceholder(emptyState(sessionService));

        section.getChildren().addAll(header, tableView, buildTableFooter());
        return section;
    }

    private void handleBorrow() {
        if (!sessionService.isLoggedIn()) {
            onLogin.run();
            return;
        }

        List<BookRow> selectedRows = rows.stream()
                .filter(BookRow::isSelected)
                .filter(BookRow::isSelectable)
                .collect(Collectors.toList());

        if (selectedRows.isEmpty()) {
            messageLabel.setText("Please select at least one available book.");
            return;
        }

        try {
            List<Long> libraryBookIds = selectedRows.stream()
                    .map(BookRow::getLibraryBookId)
                    .collect(Collectors.toList());
            borrowService.borrowLibraryBooks(sessionService.getCurrentUser(), libraryBookIds);
            for (BookRow row : selectedRows) {
                row.decrementAvailableCopies();
                row.setSelected(false);
            }
            messageLabel.setText("Borrow request submitted for " + selectedRows.size() + " book(s).");
            updateSelectionSummary();
            tableView.refresh();
        } catch (RuntimeException ex) {
            messageLabel.setText(ex.getMessage());
        }
    }

    private void configureTable() {
        TableColumn<BookRow, Boolean> selectCol = new TableColumn<>("Select");
        selectCol.setCellValueFactory(cell -> cell.getValue().selectedProperty());
        selectCol.setPrefWidth(80);
        selectCol.setCellFactory(column -> new TableCell<BookRow, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(event -> {
                    BookRow row = (BookRow) getTableRow().getItem();
                    if (row != null) {
                        row.setSelected(checkBox.isSelected());
                        updateSelectionSummary();
                    }
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                BookRow row = (getTableRow() == null) ? null : (BookRow) getTableRow().getItem();

                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }
                checkBox.selectedProperty().bindBidirectional(row.selectedProperty());
                checkBox.setDisable(!row.isSelectable());
                setGraphic(checkBox);
            }
        });

        TableColumn<BookRow, String> titleCol = new TableColumn<>("Work / Title");
        titleCol.setCellValueFactory(cell -> cell.getValue().titleProperty());

        TableColumn<BookRow, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(cell -> cell.getValue().authorProperty());

        TableColumn<BookRow, String> availableCol = new TableColumn<>("Available");
        availableCol.setCellValueFactory(cell -> cell.getValue().availableCopiesProperty());

        TableColumn<BookRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cell -> cell.getValue().statusProperty());
        statusCol.setCellFactory(column -> new TableCell<BookRow, String>() {
            private final Label chip = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                chip.setText(item);
                chip.getStyleClass().setAll("status-chip", statusClass(item));
                setGraphic(chip);
            }
        });

        tableView.getColumns().addAll(selectCol, titleCol, authorCol, availableCol, statusCol);
        tableView.setRowFactory(tv -> new javafx.scene.control.TableRow<BookRow>() {
            @Override
            protected void updateItem(BookRow item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("row-muted");
                if (!empty && item != null && !item.isSelectable()) {
                    getStyleClass().add("row-muted");
                }
            }
        });
    }

    private VBox buildTableFooter() {
        VBox footer = new VBox(10);
        footer.getStyleClass().add("detail-table-footer");

        Label summary = new Label("Showing 1-" + rows.size() + " of " + rows.size() + " total volumes");
        summary.getStyleClass().add("section-caption");

        Button previous = new Button("Previous");
        previous.getStyleClass().add("outline-button");
        Button next = new Button("Next");
        next.getStyleClass().add("outline-button");
        HBox pager = new HBox(8, previous, next);
        pager.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(12, summary);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(spacer, pager);

        HBox borrowRow = new HBox(10);
        borrowRow.setAlignment(Pos.CENTER_RIGHT);
        Label selectedLabel = new Label("Selected:");
        selectedLabel.getStyleClass().add("section-caption");
        selectedCountLabel.getStyleClass().add("section-caption");
        Button borrowButton = new Button("Borrow Selected");
        borrowButton.getStyleClass().add("primary-button");
        borrowButton.setOnAction(event -> handleBorrow());
        borrowRow.getChildren().addAll(selectedLabel, selectedCountLabel, borrowButton);

        footer.getChildren().addAll(row, borrowRow, messageLabel);
        messageLabel.getStyleClass().add("subtle-text");
        return footer;
    }

    private VBox emptyState(SessionService sessionService) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        Label title = new Label("No books found for this library.");
        title.getStyleClass().add("section-caption");
        box.getChildren().add(title);
        return box;
    }

    private Button sideNavButton(String text, String icon, boolean active, Runnable action) {
        Button button = new Button(text);
        button.setGraphic(iconLabel(icon));
        button.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        button.getStyleClass().add("nav-item");
        if (active) {
            button.getStyleClass().add("active-nav");
        }
        if (action != null) {
            button.setOnAction(event -> action.run());
        }
        return button;
    }

    private Button actionButton(String text, String icon, boolean primary, Runnable action) {
        Button button = new Button(text);
        button.setGraphic(iconLabel(icon));
        button.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        button.getStyleClass().add(primary ? "primary-button" : "outline-button");
        if (action != null) {
            button.setOnAction(event -> action.run());
        }
        return button;
    }

    private Button createImageButton(String imagePath, String accessibleText) {
        Button button = new Button();
        button.getStyleClass().add("icon-button");
        button.setGraphic(createImageView(imagePath, 24, 24));
        button.setAccessibleText(accessibleText);
        return button;
    }

    private ImageView createImageView(String path, double fitWidth, double fitHeight) {
        ImageView imageView = new ImageView();
        if (path == null || path.trim().isEmpty()) {
            imageView.getStyleClass().add("icon-missing");
            imageView.setFitWidth(fitWidth);
            imageView.setFitHeight(fitHeight);
            imageView.setPreserveRatio(false);
            return imageView;
        }

        String normalized = path.startsWith("/") ? path : "/" + path;
        try (java.io.InputStream is = getClass().getResourceAsStream(normalized)) {
            if (is != null) {
                try {
                    Image image = new Image(is, fitWidth, fitHeight, true, true);
                    imageView.setImage(image);
                } catch (Exception ex) {
                    imageView.getStyleClass().add("icon-missing");
                }
            } else {
                imageView.getStyleClass().add("icon-missing");
            }
        } catch (Exception ex) {
            imageView.getStyleClass().add("icon-missing");
        }
        imageView.setFitWidth(fitWidth);
        imageView.setFitHeight(fitHeight);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageView.setCache(true);
        return imageView;
    }

    private Label iconLabel(String icon) {
        Label label = new Label(icon);
        label.getStyleClass().add("material-symbol");
        return label;
    }


    private String statusClass(String status) {
        String normalized = status == null ? "" : status.toLowerCase(Locale.ROOT);
        if (normalized.contains("available")) {
            return "available";
        }
        if (normalized.contains("reserved")) {
            return "reserved";
        }
        if (normalized.contains("loan")) {
            return "on-loan";
        }
        return "unavailable";
    }

    private void updateSelectionSummary() {
        long selected = rows.stream().filter(BookRow::isSelected).count();
        selectedCountLabel.setText(String.valueOf(selected));
    }

    public Parent getRoot() {
        return root;
    }

    private static final class BookRow {
        private final Book book;
        private final Long libraryBookId;
        private final BooleanProperty selected = new SimpleBooleanProperty(false);
        private final StringProperty title;
        private final StringProperty author;
        private final StringProperty availableCopies;
        private final StringProperty totalCopies;
        private final int totalCopiesValue;
        private final StringProperty status;
        private final BooleanProperty selectable = new SimpleBooleanProperty(false);

        private BookRow(Library_Book libraryBook) {
            this.book = libraryBook.getB();
            this.libraryBookId = libraryBook.getId();
            this.title = new SimpleStringProperty(book == null ? "Untitled Work" : book.getTitle());
            this.author = new SimpleStringProperty(book == null ? "Unknown Author" : book.getAuthor());
            this.availableCopies = new SimpleStringProperty(String.valueOf(libraryBook.getAvailable_Copies()));
            this.totalCopiesValue = libraryBook.getTotal_Copies();
            this.totalCopies = new SimpleStringProperty(String.valueOf(totalCopiesValue));
            this.status = new SimpleStringProperty(determineStatus(libraryBook.getAvailable_Copies(), totalCopiesValue));
            this.selectable.set(libraryBook.getAvailable_Copies() > 0);
        }

        private static String determineStatus(int availableCopies, int totalCopies) {
            if (availableCopies <= 0) {
                return "On Loan";
            }
            if (availableCopies < totalCopies) {
                return "Reserved";
            }
            return "Available";
        }

        public Long getLibraryBookId() {
            return libraryBookId;
        }

        public Book getBook() {
            return book;
        }

        public boolean isSelectable() {
            return selectable.get();
        }

        public boolean isAvailable() {
            String s = status == null ? null : status.get();
            return "Available".equals(s);
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean value) {
            selected.set(value);
        }

        public void decrementAvailableCopies() {
            int current = getAvailableCopies();
            int next = Math.max(0, current - 1);
            availableCopies.set(String.valueOf(next));
            status.set(determineStatus(next, totalCopiesValue));
            selectable.set(next > 0);
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        public StringProperty titleProperty() {
            return title;
        }

        public StringProperty authorProperty() {
            return author;
        }

        public StringProperty availableCopiesProperty() {
            return availableCopies;
        }

        public StringProperty totalCopiesProperty() {
            return totalCopies;
        }

        public StringProperty statusProperty() {
            return status;
        }

        private int getAvailableCopies() {
            try {
                return Integer.parseInt(availableCopies.get());
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
    }
}

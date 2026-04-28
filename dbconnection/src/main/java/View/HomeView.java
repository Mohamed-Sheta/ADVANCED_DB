package View;

import Services.LibraryService;
import Services.SessionService;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import odb.Library;

public class HomeView {
    private final BorderPane root = new BorderPane();
    private final SessionService sessionService;
    private static final String SETTINGS_ICON = "/images/settings_icon.png";
    private static final String PROFILE_ICON = "/images/profile_icon.png";
    private static final String DEFAULT_LIBRARY_COVER = "/images/library/1_6Jp3vJWe7VFlFHZ9WhSJng.jpg";

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
        content.getChildren().add(buildLibraryGrid(libraries, onLibraryOpen));

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
        HBox bar = new HBox(8);
        bar.getStyleClass().add("app-bar");
        bar.setAlignment(Pos.CENTER_LEFT);

        Button settingsButton = createImageButton(SETTINGS_ICON, "Settings");
        settingsButton.setOnAction(event -> onSettings.run());

        Button profileButton = createImageButton(PROFILE_ICON, "Profile");
        profileButton.setOnAction(event -> {
            if (sessionService.isLoggedIn()) {
                onProfile.run();
            } else {
                onLogin.run();
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bar.getChildren().addAll(spacer, settingsButton, profileButton);
        return bar;
    }

    private Node buildLibraryGrid(List<Library> libraries, Consumer<Library> onLibraryOpen) {
        FlowPane grid = new FlowPane();
        grid.getStyleClass().add("library-grid");
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPrefWrapLength(1100);

        if (libraries == null || libraries.isEmpty()) {
            VBox emptyCard = new VBox(6);
            emptyCard.getStyleClass().add("card");
            Label title = new Label("No libraries found");
            title.getStyleClass().add("section-title");
            Label hint = new Label("No branches are available yet.");
            hint.getStyleClass().add("section-caption");
            emptyCard.getChildren().addAll(title, hint);
            return emptyCard;
        }

        for (Library library : libraries) {
            VBox card = new VBox(10);
            card.getStyleClass().add("library-card");
            card.setPrefWidth(260);

            StackPane imageWrap = new StackPane();
            imageWrap.getStyleClass().add("image-wrap");
            imageWrap.setPrefHeight(180);

            if (sessionService.isShowLibraryCovers()) {
                ImageView coverImage = createCoverImage(DEFAULT_LIBRARY_COVER);
                imageWrap.getChildren().add(coverImage);
            } else {
                VBox defaultCover = new VBox(8);
                defaultCover.getStyleClass().add("library-cover");
                defaultCover.setAlignment(Pos.CENTER);

                Label coverTitle = new Label("Athenaeum");
                coverTitle.getStyleClass().add("library-cover-title");

                Label coverSubtitle = new Label("Covers hidden in Settings");
                coverSubtitle.getStyleClass().add("library-cover-subtitle");

                defaultCover.getChildren().addAll(createIconLabel("account_balance"), coverTitle, coverSubtitle);
                imageWrap.getChildren().add(defaultCover);
            }

            VBox content = new VBox(8);
            content.getStyleClass().add("content");

            Label name = new Label(library.getName());
            name.getStyleClass().add("card-title");

            HBox locationRow = new HBox(6);
            locationRow.setAlignment(Pos.CENTER_LEFT);
            locationRow.setManaged(sessionService.isShowLibraryLocations());
            locationRow.setVisible(sessionService.isShowLibraryLocations());
            if (sessionService.isShowLibraryLocations()) {
                Label locationIcon = createIconLabel("location:");
                Label location = new Label(library.getLoction() == null ? "UNKNOWN" : library.getLoction());
                location.getStyleClass().add("subtle-text");
                locationRow.getChildren().addAll(locationIcon, location);
            }

            Button viewButton = new Button("View Library");
            viewButton.getStyleClass().add("outline-button");
            viewButton.setOnAction(event -> onLibraryOpen.accept(library));
            viewButton.setMaxWidth(Double.MAX_VALUE);

            HBox footer = new HBox(viewButton);
            HBox.setHgrow(viewButton, Priority.ALWAYS);

            content.getChildren().addAll(name, locationRow, footer);
            card.getChildren().addAll(imageWrap, content);
            grid.getChildren().add(card);
        }

        return grid;
    }

    private Button createImageButton(String imagePath, String accessibleText) {
        Button button = new Button();
        button.getStyleClass().add("icon-button");
        button.setGraphic(createImageView(imagePath, 24, 24));
        button.setAccessibleText(accessibleText);
        return button;
    }

    private Label createIconLabel(String icon) {
        Label label = new Label(icon);
        label.getStyleClass().add("material-symbol");
        return label;
    }

    private ImageView createCoverImage(String imagePath) {
        ImageView imageView = createImageView(imagePath, 260, 180);
        imageView.getStyleClass().add("library-cover-image");
        Rectangle clip = new Rectangle(260, 180);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        imageView.setClip(clip);
        return imageView;
    }

    private ImageView createImageView(String imagePath, double fitWidth, double fitHeight) {
        Image image = new Image(getClass().getResourceAsStream(imagePath));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(fitWidth);
        imageView.setFitHeight(fitHeight);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageView.setCache(true);
        return imageView;
    }

    public Parent getRoot() {
        return root;
    }
}

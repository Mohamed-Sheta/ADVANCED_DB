package View;

import Services.SessionService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SettingsView {
    private static final String BACK_ICON = "/images/arrow_back.png";
    private final BorderPane root = new BorderPane();

    public SettingsView(SessionService sessionService, boolean canGoBack, Runnable onBack) {
        root.getStyleClass().add("app-root");
        root.setPadding(Insets.EMPTY);

        root.setTop(buildTopBar(canGoBack, onBack));
        root.setCenter(buildContent(sessionService, onBack));
    }

    private HBox buildTopBar(boolean canGoBack, Runnable onBack) {
        HBox bar = new HBox(12);
        bar.getStyleClass().add("app-bar");
        bar.setAlignment(Pos.CENTER_LEFT);

        Button backButton = new Button();
        backButton.getStyleClass().add("icon-button");
        backButton.setGraphic(imageIcon(BACK_ICON, 24, 24));
        backButton.setDisable(!canGoBack);
        backButton.setOnAction(event -> onBack.run());

        VBox titleBlock = new VBox(2);
        Label title = new Label("Settings");
        title.getStyleClass().add("app-title");
        Label subtitle = new Label("Simple preferences and app information");
        subtitle.getStyleClass().add("section-caption");
        titleBlock.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bar.getChildren().addAll(backButton, titleBlock, spacer);
        return bar;
    }

    private VBox buildContent(SessionService sessionService, Runnable onBack) {
        VBox page = new VBox(20);
        page.getStyleClass().add("settings-page");

        VBox preferencesCard = new VBox(14);
        preferencesCard.getStyleClass().add("card");

        Label preferencesTitle = new Label("Preferences");
        preferencesTitle.getStyleClass().add("section-title");

        CheckBox showCovers = new CheckBox("Show Book cover images");
        showCovers.getStyleClass().add("settings-toggle");
        showCovers.selectedProperty().bindBidirectional(sessionService.showLibraryCoversProperty());

        CheckBox showLocations = new CheckBox("Show library locations on home cards");
        showLocations.getStyleClass().add("settings-toggle");
        showLocations.selectedProperty().bindBidirectional(sessionService.showLibraryLocationsProperty());

        Label helpText = new Label("These settings are applied immediately when you return to the home screen.");
        helpText.getStyleClass().add("section-caption");

        HBox actions = new HBox(10);
        Button reset = new Button("Reset Defaults");
        reset.getStyleClass().add("outline-button");
        reset.setOnAction(event -> {
            sessionService.setShowLibraryCovers(true);
            sessionService.setShowLibraryLocations(true);
        });

        Button done = new Button("Done");
        done.getStyleClass().add("primary-button");
        done.setOnAction(event -> onBack.run());

        actions.getChildren().addAll(reset, done);

        preferencesCard.getChildren().addAll(preferencesTitle, showCovers, showLocations, helpText, actions);

        VBox aboutCard = new VBox(12);
        aboutCard.getStyleClass().add("card");

        Label aboutTitle = new Label("About");
        aboutTitle.getStyleClass().add("section-title");

        Label appName = new Label("Athenaeum Curator");
        appName.getStyleClass().add("header-serif");

        Label description = new Label("A small JavaFX library system for browsing branches, viewing collections, and borrowing books.");
        description.getStyleClass().add("section-caption");

        HBox infoRow = new HBox(10, infoChip("JavaFX UI"), infoChip("Maven app"), infoChip("Local session settings"));
        infoRow.setAlignment(Pos.CENTER_LEFT);
        aboutCard.getChildren().addAll(aboutTitle, appName, description, infoRow);

        page.getChildren().addAll(preferencesCard, aboutCard);
        return page;
    }

    private HBox infoChip(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("settings-chip");
        return new HBox(label);
    }

    private ImageView imageIcon(String path, double fitWidth, double fitHeight) {
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
                    Image img = new Image(is, fitWidth, fitHeight, true, true);
                    imageView.setImage(img);
                } catch (Exception e) {
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

    public Parent getRoot() {
        return root;
    }
}

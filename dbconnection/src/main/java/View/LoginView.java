package View;

import Services.PersonService;
import Services.SessionService;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Control;
import javafx.util.Duration;
import javafx.scene.input.MouseEvent;
import javafx.animation.Interpolator;
import odb.Person;

public class LoginView {
    private final BorderPane root = new BorderPane();
    private static final String BACK_ICON = "/images/arrow_back.png";
    private static final String HERO_IMAGE = "/images/patrick-robert-doyle-OvXht_wi5Ew-unsplash.png";

    // design constants
    private static final String PAGE_BG = "#0f172a"; // page background
    private static final String CARD_BG = "#1e293b"; // card background
    private static final String INPUT_BG = "#0f172a"; // input background (per spec)

    public LoginView(
            PersonService personService,
            SessionService sessionService,
            boolean canGoBack,
            Runnable onBack,
            Runnable onSuccess,
            Runnable onRegister
    ) {
        System.out.println("Building LoginView");
        root.getStyleClass().add("auth-root");
        root.setStyle("-fx-background-color: " + PAGE_BG + ";");

        System.out.println("LoginView: adding TOP to root; children before=" + root.getChildren().size());
        root.setTop(buildTopBar(canGoBack, onBack));
        System.out.println("LoginView: added TOP to root; children after=" + root.getChildren().size());

        System.out.println("LoginView: adding CENTER to root; children before=" + root.getChildren().size());
        root.setCenter(buildPage(personService, sessionService, onSuccess, onRegister));
        System.out.println("LoginView: added CENTER to root; children after=" + root.getChildren().size());
    }

    private HBox buildTopBar(boolean canGoBack, Runnable onBack) {
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(12, 20, 12, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button backButton = new Button();
        backButton.getStyleClass().add("icon-button");
        // inline small icon load for topbar
        Image backImg = loadImage(BACK_ICON);
        if (backImg != null) {
            ImageView backIv = new ImageView(backImg);
            backIv.setFitWidth(20);
            backIv.setFitHeight(20);
            backIv.setPreserveRatio(true);
            backIv.setSmooth(true);
            backIv.setCache(true);
            backButton.setGraphic(backIv);
        }
        backButton.setDisable(!canGoBack);
        backButton.setOnAction(event -> onBack.run());
        backButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        Label brand = new Label("Athenaeum Curator");
        brand.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 14px; -fx-font-weight: 600;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(backButton, brand, spacer);
        return topBar;
    }

    private ScrollPane buildPage(PersonService personService, SessionService sessionService, Runnable onSuccess, Runnable onRegister) {
        HBox shell = new HBox();
        shell.setAlignment(Pos.CENTER);
        // No gap between image and form; image will fill 65% and form 35%
        shell.setSpacing(0);
        shell.setPadding(Insets.EMPTY);

        // LEFT: hero image container
        StackPane heroWrapper = createImageContainer(HERO_IMAGE);
        HBox.setHgrow(heroWrapper, Priority.ALWAYS);
        heroWrapper.setMaxWidth(Double.MAX_VALUE);
        heroWrapper.setMaxHeight(Double.MAX_VALUE);
        heroWrapper.prefWidthProperty().bind(shell.widthProperty().multiply(0.65));

        // RIGHT: centered form card (glassmorphism)
        VBox rightWrapper = new VBox();
        rightWrapper.setAlignment(Pos.CENTER);
        rightWrapper.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(rightWrapper, Priority.NEVER);
        // Make form take 35% width
        rightWrapper.prefWidthProperty().bind(shell.widthProperty().multiply(0.35));

        VBox card = buildFormCard(personService, sessionService, onSuccess, onRegister);
        // apply glass style (slightly transparent, soft shadow); form centered vertically
        card.setPadding(new Insets(40));
        card.setMaxWidth(400);
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.06); " +
                        "-fx-background-radius: 20; " +
                        "-fx-border-radius: 20; " +
                        "-fx-border-color: rgba(255,255,255,0.06); " +
                        "-fx-effect: dropshadow(gaussian, rgba(2,6,23,0.6), 24, 0.2, 0, 8);"
        );
        rightWrapper.getChildren().add(card);

        // ensure card stays centered vertically inside available space
        shell.getChildren().addAll(heroWrapper, rightWrapper);

        ScrollPane scrollPane = new ScrollPane(shell);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scrollPane;
    }

    // Create a StackPane that contains the ImageView, overlay, clipping and animations
    private StackPane createImageContainer(String imagePath) {
        StackPane container = new StackPane();
        container.setMaxWidth(Double.MAX_VALUE);
        container.setMaxHeight(Double.MAX_VALUE);

        // create ImageView up-front to keep it effectively final for lambdas
        final ImageView iv = new ImageView();
        try {
            java.net.URL url = getClass().getResource(imagePath);
            if (url == null) {
                System.out.println("❌ IMAGE NOT FOUND: " + imagePath);
            } else {
                System.out.println("✅ IMAGE LOADED: " + url);
            }

            Image img = null;
            if (url != null) {
                img = new Image(url.toExternalForm(), true);
                if (img.isError()) {
                    System.out.println("❌ Image failed to load: " + imagePath + " -> " + img.getException());
                    img = null;
                }
            }

            if (img != null) {
                iv.setImage(img);
            } else {
                // fallback: solid color placeholder as WritableImage
                int w = 600, h = 420;
                javafx.scene.image.WritableImage wi = new javafx.scene.image.WritableImage(w, h);
                javafx.scene.image.PixelWriter pw = wi.getPixelWriter();
                javafx.scene.paint.Color c = javafx.scene.paint.Color.web("#334155");
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        pw.setColor(x, y, c);
                    }
                }
                iv.setImage(wi);
            }
        } catch (Exception ex) {
            System.out.println("createImageView failed: " + ex.getMessage());
            javafx.scene.image.WritableImage wi = new javafx.scene.image.WritableImage(10, 10);
            javafx.scene.image.PixelWriter pw = wi.getPixelWriter();
            pw.setColor(0, 0, javafx.scene.paint.Color.web("#334155"));
            iv.setImage(wi);
        }

        // Ensure ImageView fills the container
        iv.setPreserveRatio(false);
        iv.setSmooth(true);
        iv.setCache(true);
        iv.fitWidthProperty().bind(container.widthProperty());
        iv.fitHeightProperty().bind(container.heightProperty());

        // clip rounded corners bound to container
        Rectangle wrapperClip = new Rectangle();
        wrapperClip.setArcWidth(20);
        wrapperClip.setArcHeight(20);
        wrapperClip.widthProperty().bind(container.widthProperty());
        wrapperClip.heightProperty().bind(container.heightProperty());
        container.setClip(wrapperClip);

        // dark gradient overlay (right -> left)
        javafx.scene.paint.LinearGradient gradient = new javafx.scene.paint.LinearGradient(
                1, 0, 0, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.rgb(0,0,0,0.65)),
                new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.rgb(0,0,0,0.10))
        );
        Rectangle overlay = new Rectangle();
        overlay.widthProperty().bind(container.widthProperty());
        overlay.heightProperty().bind(container.heightProperty());
        overlay.setArcWidth(20);
        overlay.setArcHeight(20);
        overlay.setFill(gradient);
        overlay.setMouseTransparent(true);

        // subtle outer shadow
        DropShadow heroShadow = new DropShadow();
        heroShadow.setRadius(20);
        heroShadow.setOffsetY(8);
        heroShadow.setColor(Color.color(0, 0, 0, 0.6));
        container.setEffect(heroShadow);

        // add image and overlay
        container.getChildren().addAll(iv, overlay);

        // premium animations applied to container
        // Fade in once
        FadeTransition fade = new FadeTransition(Duration.seconds(1.2), container);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setInterpolator(Interpolator.EASE_BOTH);

        // continuous subtle zoom (auto-reverse)
        ScaleTransition scale = new ScaleTransition(Duration.seconds(6), container);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.05);
        scale.setToY(1.05);
        scale.setCycleCount(javafx.animation.Animation.INDEFINITE);
        scale.setAutoReverse(true);
        scale.setInterpolator(Interpolator.EASE_BOTH);

        // play fade+scale together for a smooth intro/loop
        javafx.animation.ParallelTransition intro = new javafx.animation.ParallelTransition(fade, scale);
        intro.play();

        // floating effect
        TranslateTransition floatAnim = new TranslateTransition(Duration.seconds(4), container);
        floatAnim.setFromY(0);
        floatAnim.setToY(-10);
        floatAnim.setCycleCount(javafx.animation.Animation.INDEFINITE);
        floatAnim.setAutoReverse(true);
        floatAnim.setInterpolator(Interpolator.EASE_BOTH);
        floatAnim.play();

        // parallax: move the imageView inside container on mouse move
        container.setOnMouseMoved(evt -> {
            double cx = container.getWidth() / 2.0;
            double cy = container.getHeight() / 2.0;
            double dx = (evt.getX() - cx) / cx; // -1..1
            double dy = (evt.getY() - cy) / cy;
            iv.setTranslateX(dx * 12);
            iv.setTranslateY(dy * 8);
        });
        container.setOnMouseExited(evt -> {
            javafx.animation.TranslateTransition rt = new javafx.animation.TranslateTransition(Duration.millis(400), iv);
            rt.setToX(0);
            rt.setToY(0);
            rt.setInterpolator(Interpolator.EASE_BOTH);
            rt.play();
        });

        return container;
    }

    // Attach premium animations (fade+scale once, floating loop, parallax mouse)
    private void createAnimatedImage(ImageView iv, StackPane heroWrapper) {
        // initial state
        iv.setOpacity(0);
        iv.setScaleX(1.0);
        iv.setScaleY(1.0);

        FadeTransition fade = new FadeTransition(Duration.seconds(1.2), iv);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setInterpolator(Interpolator.EASE_BOTH);

        ScaleTransition scale = new ScaleTransition(Duration.seconds(1.2), iv);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.05);
        scale.setToY(1.05);
        scale.setInterpolator(Interpolator.EASE_BOTH);

        javafx.animation.ParallelTransition intro = new javafx.animation.ParallelTransition(fade, scale);
        intro.play();

        TranslateTransition floatAnim = new TranslateTransition(Duration.seconds(4), iv);
        floatAnim.setFromY(0);
        floatAnim.setToY(-10);
        floatAnim.setCycleCount(javafx.animation.Animation.INDEFINITE);
        floatAnim.setAutoReverse(true);
        floatAnim.setInterpolator(Interpolator.EASE_BOTH);
        floatAnim.play();

        // simple parallax on mouse move
        heroWrapper.setOnMouseMoved(evt -> {
            double cx = heroWrapper.getWidth() / 2.0;
            double cy = heroWrapper.getHeight() / 2.0;
            double dx = (evt.getX() - cx) / cx; // -1..1
            double dy = (evt.getY() - cy) / cy;
            // small translate and slight scale shift
            iv.setTranslateX(dx * 12);
            iv.setTranslateY(dy * 8);
        });
        heroWrapper.setOnMouseExited(evt -> {
            // reset smoothly
            javafx.animation.TranslateTransition rt = new javafx.animation.TranslateTransition(Duration.millis(400), iv);
            rt.setToX(0);
            rt.setToY(0);
            rt.setInterpolator(Interpolator.EASE_BOTH);
            rt.play();
        });
    }

    private VBox buildFormCard(PersonService personService, SessionService sessionService, Runnable onSuccess, Runnable onRegister) {
        VBox card = new VBox(18);
        card.setPadding(new Insets(35));
        card.setMaxWidth(420);
        card.setMinWidth(420);
        card.setPrefWidth(420);
        card.setStyle(
                "-fx-background-color: " + CARD_BG + "; " +
                "-fx-background-radius: 20; " +
                "-fx-border-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 20, 0.2, 0, 6);"
        );

        // appear animation
        FadeTransition ft = new FadeTransition(Duration.millis(420), card);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
        ScaleTransition st = new ScaleTransition(Duration.millis(420), card);
        st.setFromX(0.995);
        st.setFromY(0.995);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();

        Label title = new Label("Sign In");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: 800;");
        Label subtitle = new Label("Enter your archive credentials to continue.");
        subtitle.setStyle("-fx-text-fill: rgba(255,255,255,0.75); -fx-font-size: 12px;");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(16);

        TextField emailField = textField();
        emailField.setPromptText("alex@library.org");
        PasswordField passwordField = passwordField();
        passwordField.setPromptText("••••••••");

        form.addRow(0, labeledField("Email Address", emailField));
        form.addRow(1, labeledField("Password", passwordField));

        Label message = new Label();
        message.setStyle("-fx-text-fill: #ffb4b4; -fx-font-size: 12px;");

        Button loginButton = new Button("Sign In");
        stylePrimaryButton(loginButton);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnMouseEntered(e -> applyButtonHover(loginButton, true));
        loginButton.setOnMouseExited(e -> applyButtonHover(loginButton, false));
        loginButton.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> loginButton.setStyle(getPrimaryButtonPressedStyle()));
        loginButton.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> loginButton.setStyle(getPrimaryButtonStyle()));

        loginButton.disableProperty().bind(
                Bindings.createBooleanBinding(() ->
                                emailField.getText() == null || emailField.getText().trim().isEmpty()
                                        || passwordField.getText() == null || passwordField.getText().isEmpty(),
                        emailField.textProperty(), passwordField.textProperty())
        );
        loginButton.setOnAction(event -> {
            try {
                Person person = personService.login(emailField.getText(), passwordField.getText());
                if (person == null) {
                    message.setText("Invalid email or password.");
                    return;
                }
                sessionService.setCurrentUser(person);
                onSuccess.run();
            } catch (RuntimeException ex) {
                message.setText(ex.getMessage());
            }
        });

        HBox footer = new HBox(6);
        footer.setAlignment(Pos.CENTER_LEFT);
        Label prompt = new Label("Need an account?");
        prompt.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 12px;");
        Button registerLink = new Button("Register");
        registerLink.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.9); -fx-underline: false; -fx-cursor: hand; -fx-font-weight:600;");
        registerLink.setOnMouseEntered(e -> registerLink.setStyle(registerLink.getStyle() + " -fx-underline: true;"));
        registerLink.setOnMouseExited(e -> registerLink.setStyle(registerLink.getStyle().replace(" -fx-underline: true;", "")));
        registerLink.setOnAction(event -> onRegister.run());
        footer.getChildren().addAll(prompt, registerLink);

        // layout assembly with spacing
        card.getChildren().addAll(title, subtitle, form, loginButton, message, footer);
        VBox.setMargin(loginButton, new Insets(8, 0, 0, 0));

        return card;
    }

    private HBox labeledField(String labelText, Control control) {
        VBox column = new VBox(12);
        column.setFillWidth(true);
        Label label = new Label(labelText.toUpperCase());
        label.setStyle("-fx-text-fill: rgba(255,255,255,0.78); -fx-font-size: 11px; -fx-font-weight:600;");
        styleInput(control);
        control.setMaxWidth(Double.MAX_VALUE);
        control.setPrefWidth(380);
        column.getChildren().addAll(label, control);
        HBox row = new HBox(column);
        row.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(column, Priority.ALWAYS);
        return row;
    }

    private TextField textField() {
        return new TextField();
    }

    private PasswordField passwordField() {
        return new PasswordField();
    }

    private void styleInput(Control control) {
        control.setStyle(
                "-fx-background-color: " + INPUT_BG + "; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 10 12 10 12; " +
                "-fx-border-color: transparent; " +
                "-fx-border-radius: 12;"
        );

        // focus glow / border animation
        DropShadow focusGlow = new DropShadow(18, Color.web("#6366f1"));
        focusGlow.setSpread(0.12);
        control.focusedProperty().addListener((obs, oldV, newV) -> {
            if (newV) {
                // apply glow and subtle outline
                control.setEffect(focusGlow);
                control.setStyle(control.getStyle() + " -fx-border-color: rgba(99,102,241,0.95); -fx-border-width: 1.5;");
            } else {
                // remove
                control.setEffect(null);
                control.setStyle(control.getStyle().replaceAll(" -fx-border-color: rgba\\(99,102,241,0.95\\); -fx-border-width: 1.5;", ""));
            }
        });
    }

    private void stylePrimaryButton(Button btn) {
        btn.setStyle(getPrimaryButtonStyle());
        btn.setPrefHeight(45);
        btn.setMinHeight(45);
        btn.setMaxHeight(45);
        btn.setStyle(getPrimaryButtonStyle());
        btn.setWrapText(false);
        btn.setPadding(new Insets(8, 18, 8, 18));

        // initial subtle shadow
        DropShadow ds = new DropShadow(12, Color.color(12/255.0, 18/255.0, 42/255.0, 0.6));
        btn.setEffect(ds);
    }

    private String getPrimaryButtonStyle() {
        return "-fx-background-radius: 20; -fx-text-fill: white; -fx-font-weight: 700; " +
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #6366f1 0%, #8b5cf6 100%); " +
                "-fx-cursor: hand; -fx-font-size: 14px;";
    }

    private String getPrimaryButtonPressedStyle() {
        return "-fx-background-radius: 20; -fx-text-fill: white; -fx-font-weight: 700; " +
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #5450d6 0%, #6f3fd6 100%); " +
                "-fx-cursor: hand;";
    }

    private void applyButtonHover(Button btn, boolean hover) {
        if (hover) {
            ScaleTransition st = new ScaleTransition(Duration.millis(160), btn);
            st.setToX(1.05);
            st.setToY(1.05);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.playFromStart();
            DropShadow glow = new DropShadow(22, Color.web("#8b5cf6"));
            glow.setSpread(0.18);
            btn.setEffect(glow);
            // brighten slightly
            btn.setOpacity(1.02);
        } else {
            ScaleTransition st = new ScaleTransition(Duration.millis(160), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.setInterpolator(Interpolator.EASE_IN);
            st.playFromStart();
            DropShadow ds = new DropShadow(12, Color.color(12/255.0, 18/255.0, 42/255.0, 0.6));
            btn.setEffect(ds);
            btn.setOpacity(1.0);
        }
    }

    private Image loadImage(String path) {
        if (path == null || path.trim().isEmpty()) return null;
        String normalized = path.startsWith("/") ? path : "/" + path;
        try (java.io.InputStream is = getClass().getResourceAsStream(normalized)) {
            if (is != null) return new Image(is);
        } catch (Exception e) {
            System.out.println("Failed to load image: " + normalized + " -> " + e.getMessage());
        }
        System.out.println("Image resource not found: " + normalized);
        return null;
    }


    public Parent getRoot() {
        return root;
    }
}

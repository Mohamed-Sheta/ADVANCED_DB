package View;

import Services.PersonService;
import Services.SessionService;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
import javafx.util.Duration;
import javafx.scene.input.MouseEvent;
import odb.Person;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterView {
    private final BorderPane root = new BorderPane();
    private static final String BACK_ICON = "/images/arrow_back.png";
    private static final String HERO_IMAGE = "/images/uladzislau-petrushkevich-lsWtxgcCWrY-unsplash.png";

    // design constants
    private static final String PAGE_BG = "#0f172a"; // page background
    private static final String CARD_BG = "#1e293b"; // card background
    private static final String INPUT_BG = "#0f172a"; // input background

    // validation patterns
    private static final Pattern USERNAME_RE = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_RE = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PASSWORD_RE = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$");
    private static final Pattern SSN_RE = Pattern.compile("^\\d{3}-\\d{2}-\\d{4}$");
    private static final Pattern PHONE_RE = Pattern.compile("^\\+?[0-9]{10,15}$");

    public RegisterView(
            PersonService personService,
            SessionService sessionService,
            boolean canGoBack,
            Runnable onBack,
            Runnable onSuccess,
            Runnable onLogin
    ) {
        root.getStyleClass().add("auth-root");
        root.setStyle("-fx-background-color: " + PAGE_BG + ";");

        root.setTop(buildTopBar(canGoBack, onBack));
        root.setCenter(buildPage(personService, sessionService, onSuccess, onLogin));
    }

    private HBox buildTopBar(boolean canGoBack, Runnable onBack) {
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(12, 20, 12, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button backButton = new Button();
        backButton.getStyleClass().add("icon-button");
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

    private ScrollPane buildPage(PersonService personService, SessionService sessionService, Runnable onSuccess, Runnable onLogin) {
        HBox shell = new HBox();
        shell.setAlignment(Pos.CENTER);
        shell.setSpacing(0);
        shell.setPadding(Insets.EMPTY);

        // LEFT: hero image
        StackPane heroWrapper = createImageContainer(HERO_IMAGE);
        heroWrapper.setMaxWidth(Double.MAX_VALUE);
        heroWrapper.setMaxHeight(Double.MAX_VALUE);
        HBox.setHgrow(heroWrapper, Priority.ALWAYS);
        heroWrapper.prefWidthProperty().bind(shell.widthProperty().multiply(0.65));

        // RIGHT: centered form card (glassmorphism)
        VBox rightWrapper = new VBox();
        rightWrapper.setAlignment(Pos.CENTER);
        rightWrapper.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(rightWrapper, Priority.NEVER);
        rightWrapper.prefWidthProperty().bind(shell.widthProperty().multiply(0.35));

        VBox card = buildFormCard(personService, sessionService, onSuccess, onLogin);
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

        // create ImageView up-front so it is effectively final for lambdas below
        final ImageView iv = new ImageView();
        try {
            // DEBUG: check resource URL first
            java.net.URL url = getClass().getResource(imagePath);
            if (url == null) {
                System.out.println("❌ IMAGE NOT FOUND: " + imagePath);
            } else {
                System.out.println("✅ IMAGE LOADED: " + url);
            }

            Image img = null;
            if (url != null) {
                // Use the exact pattern requested: load from URL.toExternalForm() with background loading
                img = new Image(url.toExternalForm(), true);
                if (img.isError()) {
                    System.out.println("❌ Image failed to load: " + imagePath + " -> " + img.getException());
                    img = null;
                }
            }

            if (img != null) {
                // set image on existing ImageView (keeps variable effectively final)
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
            // final fallback: tiny single-color image
            javafx.scene.image.WritableImage wi = new javafx.scene.image.WritableImage(10, 10);
            javafx.scene.image.PixelWriter pw = wi.getPixelWriter();
            pw.setColor(0, 0, javafx.scene.paint.Color.web("#334155"));
            iv.setImage(wi);
        }

        // Configure ImageView consistently
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

        // dark gradient overlay
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

        DropShadow heroShadow = new DropShadow();
        heroShadow.setRadius(20);
        heroShadow.setOffsetY(8);
        heroShadow.setColor(Color.color(0, 0, 0, 0.6));
        container.setEffect(heroShadow);

        container.getChildren().addAll(iv, overlay);

        // premium animations applied to container
        FadeTransition fade = new FadeTransition(Duration.seconds(1.2), container);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setInterpolator(Interpolator.EASE_BOTH);

        ScaleTransition scale = new ScaleTransition(Duration.seconds(6), container);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.05);
        scale.setToY(1.05);
        scale.setCycleCount(javafx.animation.Animation.INDEFINITE);
        scale.setAutoReverse(true);
        scale.setInterpolator(Interpolator.EASE_BOTH);

        javafx.animation.ParallelTransition intro = new javafx.animation.ParallelTransition(fade, scale);
        intro.play();

        TranslateTransition floatAnim = new TranslateTransition(Duration.seconds(4), container);
        floatAnim.setFromY(0);
        floatAnim.setToY(-10);
        floatAnim.setCycleCount(javafx.animation.Animation.INDEFINITE);
        floatAnim.setAutoReverse(true);
        floatAnim.setInterpolator(Interpolator.EASE_BOTH);
        floatAnim.play();

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

    private VBox buildFormCard(PersonService personService, SessionService sessionService, Runnable onSuccess, Runnable onLogin) {
        VBox card = new VBox(16);
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

        Label title = new Label("Create Account");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: 800;");
        Label subtitle = new Label("Begin your stewardship of the archives.");
        subtitle.setStyle("-fx-text-fill: rgba(255,255,255,0.75); -fx-font-size: 12px;");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(14);

        // fields
        TextField nameField = textField();
        nameField.setPromptText("e.g. curator_alex");
        TextField emailField = textField();
        emailField.setPromptText("alex@library.org");
        PasswordField passwordField = passwordField();
        passwordField.setPromptText("••••••••");
        PasswordField confirmField = passwordField();
        confirmField.setPromptText("••••••••");
        TextField ssnField = textField();
        ssnField.setPromptText("e.g. 303-22-4099");
        TextField phoneField = textField();
        phoneField.setPromptText("e.g. +1 555 0199");
        TextField addressField = textField();
        addressField.setPromptText("e.g. North Wing, Room 402");
        ComboBox<String> genderField = genderCombo();

        // error labels
        Label nameErr = errorLabel();
        Label emailErr = errorLabel();
        Label passErr = errorLabel();
        Label confirmErr = errorLabel();
        Label ssnErr = errorLabel();
        Label phoneErr = errorLabel();
        Label addressErr = errorLabel();
        Label genderErr = errorLabel();

        // add labeled fields + error labels
        form.addRow(0, labeledField("Username", nameField, nameErr));
        form.addRow(1, labeledField("Email Address", emailField, emailErr));
        form.addRow(2, labeledField("Password", passwordField, passErr));
        form.addRow(3, labeledField("Confirm Password", confirmField, confirmErr));
        form.addRow(4, labeledField("SSN", ssnField, ssnErr));
        form.addRow(5, labeledField("Phone", phoneField, phoneErr));
        form.addRow(6, labeledField("Address", addressField, addressErr));
        form.addRow(7, labeledField("Gender", genderField, genderErr));

        Label message = new Label();
        message.setStyle("-fx-text-fill: #ffb4b4; -fx-font-size: 12px;");

        Button registerButton = new Button("Register");
        stylePrimaryButton(registerButton);
        registerButton.setMaxWidth(Double.MAX_VALUE);

        // store base styles so we can toggle valid/invalid states without losing base look
        Map<javafx.scene.control.Control, String> baseStyles = new HashMap<>();
        baseStyles.put(nameField, nameField.getStyle());
        baseStyles.put(emailField, emailField.getStyle());
        baseStyles.put(passwordField, passwordField.getStyle());
        baseStyles.put(confirmField, confirmField.getStyle());
        baseStyles.put(ssnField, ssnField.getStyle());
        baseStyles.put(phoneField, phoneField.getStyle());
        baseStyles.put(addressField, addressField.getStyle());
        baseStyles.put(genderField, genderField.getStyle());

        // boolean properties for validity
        BooleanProperty nameValid = new SimpleBooleanProperty(false);
        BooleanProperty emailValid = new SimpleBooleanProperty(false);
        BooleanProperty passwordValid = new SimpleBooleanProperty(false);
        BooleanProperty confirmValid = new SimpleBooleanProperty(false);
        BooleanProperty ssnValid = new SimpleBooleanProperty(false);
        BooleanProperty phoneValid = new SimpleBooleanProperty(false);
        BooleanProperty addressValid = new SimpleBooleanProperty(false);
        BooleanProperty genderValid = new SimpleBooleanProperty(false);

        // listeners: validate on typing
        nameField.textProperty().addListener((obs, oldV, newV) -> {
            String err = validateUsername(newV);
            if (err == null) applyValid(nameField, baseStyles.get(nameField), nameErr, nameValid);
            else applyInvalid(nameField, baseStyles.get(nameField), nameErr, err, nameValid);
        });

        emailField.textProperty().addListener((obs, oldV, newV) -> {
            String err = validateEmail(newV);
            if (err == null) applyValid(emailField, baseStyles.get(emailField), emailErr, emailValid);
            else applyInvalid(emailField, baseStyles.get(emailField), emailErr, err, emailValid);
        });

        passwordField.textProperty().addListener((obs, oldV, newV) -> {
            String err = validatePassword(newV);
            if (err == null) applyValid(passwordField, baseStyles.get(passwordField), passErr, passwordValid);
            else applyInvalid(passwordField, baseStyles.get(passwordField), passErr, err, passwordValid);
            // re-check confirm when password changes
            String cErr = validateConfirm(newV, confirmField.getText());
            if (cErr == null) applyValid(confirmField, baseStyles.get(confirmField), confirmErr, confirmValid);
            else applyInvalid(confirmField, baseStyles.get(confirmField), confirmErr, cErr, confirmValid);
        });

        confirmField.textProperty().addListener((obs, oldV, newV) -> {
            String err = validateConfirm(passwordField.getText(), newV);
            if (err == null) applyValid(confirmField, baseStyles.get(confirmField), confirmErr, confirmValid);
            else applyInvalid(confirmField, baseStyles.get(confirmField), confirmErr, err, confirmValid);
        });

        ssnField.textProperty().addListener((obs, oldV, newV) -> {
            String err = validateSSN(newV);
            if (err == null) applyValid(ssnField, baseStyles.get(ssnField), ssnErr, ssnValid);
            else applyInvalid(ssnField, baseStyles.get(ssnField), ssnErr, err, ssnValid);
        });

        phoneField.textProperty().addListener((obs, oldV, newV) -> {
            String err = validatePhone(newV);
            if (err == null) applyValid(phoneField, baseStyles.get(phoneField), phoneErr, phoneValid);
            else applyInvalid(phoneField, baseStyles.get(phoneField), phoneErr, err, phoneValid);
        });

        addressField.textProperty().addListener((obs, oldV, newV) -> {
            String err = validateAddress(newV);
            if (err == null) applyValid(addressField, baseStyles.get(addressField), addressErr, addressValid);
            else applyInvalid(addressField, baseStyles.get(addressField), addressErr, err, addressValid);
        });

        genderField.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                applyInvalid(genderField, baseStyles.get(genderField), genderErr, "Please select a gender.", genderValid);
            } else {
                applyValid(genderField, baseStyles.get(genderField), genderErr, genderValid);
            }
        });

        // initial validation state (empty)
        nameField.getProperties().put("initValidation", true);
        emailField.getProperties().put("initValidation", true);
        passwordField.getProperties().put("initValidation", true);
        confirmField.getProperties().put("initValidation", true);
        ssnField.getProperties().put("initValidation", true);
        phoneField.getProperties().put("initValidation", true);
        addressField.getProperties().put("initValidation", true);
        genderField.getProperties().put("initValidation", true);

        // Disable register button unless all valid
        registerButton.disableProperty().bind(
                nameValid.not()
                        .or(emailValid.not())
                        .or(passwordValid.not())
                        .or(confirmValid.not())
                        .or(ssnValid.not())
                        .or(phoneValid.not())
                        .or(addressValid.not())
                        .or(genderValid.not())
        );

        registerButton.setOnMouseEntered(e -> applyButtonHover(registerButton, true));
        registerButton.setOnMouseExited(e -> applyButtonHover(registerButton, false));
        registerButton.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> registerButton.setStyle(getPrimaryButtonPressedStyle()));
        registerButton.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> registerButton.setStyle(getPrimaryButtonStyle()));
        registerButton.setOnAction(event -> {
            // final check - defensive
            if (!nameValid.get() || !emailValid.get() || !passwordValid.get() || !confirmValid.get() || !ssnValid.get() || !phoneValid.get() || !addressValid.get() || !genderValid.get()) {
                message.setText("Please fix validation errors before continuing.");
                // shake invalid fields quickly
                if (!nameValid.get()) shake(nameField);
                if (!emailValid.get()) shake(emailField);
                if (!passwordValid.get()) shake(passwordField);
                if (!confirmValid.get()) shake(confirmField);
                if (!ssnValid.get()) shake(ssnField);
                if (!phoneValid.get()) shake(phoneField);
                if (!addressValid.get()) shake(addressField);
                if (!genderValid.get()) shake(genderField);
                return;
            }

            // Send raw password to service; service will hash before storing
            String rawPassword = passwordField.getText();

            try {
                Person person = personService.register(
                        nameField.getText(),
                        emailField.getText(),
                        rawPassword,
                        ssnField.getText(),
                        phoneField.getText(),
                        addressField.getText(),
                        genderField.getValue()
                );
                // success animation
                FadeTransition fade = new FadeTransition(Duration.millis(420), card);
                fade.setFromValue(1.0);
                fade.setToValue(0.92);
                ScaleTransition s = new ScaleTransition(Duration.millis(420), card);
                s.setToX(1.02);
                s.setToY(1.02);
                fade.play(); s.play();

                sessionService.setCurrentUser(person);
                onSuccess.run();
            } catch (RuntimeException ex) {
                message.setText(ex.getMessage());
                // shake card to show general error
                shake(card);
            }
        });

        HBox footer = new HBox(6);
        footer.setAlignment(Pos.CENTER_LEFT);
        Label prompt = new Label("Already a member of the archives?");
        prompt.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 12px;");
        Button loginLink = new Button("Sign In");
        loginLink.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.9); -fx-underline: false; -fx-cursor: hand; -fx-font-weight:600;");
        loginLink.setOnMouseEntered(e -> loginLink.setStyle(loginLink.getStyle() + " -fx-underline: true;"));
        loginLink.setOnMouseExited(e -> loginLink.setStyle(loginLink.getStyle().replace(" -fx-underline: true;", "")));
        loginLink.setOnAction(event -> onLogin.run());
        footer.getChildren().addAll(prompt, loginLink);

        card.getChildren().addAll(title, subtitle, form, registerButton, message, footer);
        return card;
    }

    // New labeledField that accepts an error label so error text appears under the control
    private HBox labeledField(String labelText, javafx.scene.control.Control control, Label errorLabel) {
        VBox column = new VBox(6);
        column.setFillWidth(true);
        Label label = new Label(labelText.toUpperCase());
        label.setStyle("-fx-text-fill: rgba(255,255,255,0.78); -fx-font-size: 11px; -fx-font-weight:600;");
        styleInput(control);
        control.setMaxWidth(Double.MAX_VALUE);
        control.setPrefWidth(380);
        column.getChildren().addAll(label, control, errorLabel);
        HBox row = new HBox(column);
        row.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(column, Priority.ALWAYS);
        return row;
    }

    // ...existing simple helpers... (kept but unchanged)
    private TextField textField() {
        return new TextField();
    }

    private PasswordField passwordField() {
        return new PasswordField();
    }

    private ComboBox<String> genderCombo() {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll("Male", "Female");
        combo.setPromptText("Select gender");
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setPrefWidth(380);
        return combo;
    }

    private Label errorLabel() {
        Label l = new Label();
        l.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 11px; -fx-opacity: 0.95;");
        l.setWrapText(true);
        l.setMinHeight(16);
        return l;
    }

    private void styleInput(javafx.scene.control.Control control) {
        control.setStyle(
                "-fx-background-color: " + INPUT_BG + "; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 10 12 10 12; " +
                "-fx-border-color: transparent; " +
                "-fx-border-radius: 12;"
        );

        DropShadow focusGlow = new DropShadow(18, Color.web("#6366f1"));
        focusGlow.setSpread(0.12);
        control.focusedProperty().addListener((obs, oldV, newV) -> {
            if (newV) {
                control.setEffect(focusGlow);
                control.setStyle(control.getStyle() + " -fx-border-color: rgba(99,102,241,0.95); -fx-border-width: 1.5;");
            } else {
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

    // ---------------- Validation helpers ----------------
    private String validateUsername(String v) {
        if (v == null || v.trim().isEmpty()) return "Username is required.";
        if (!USERNAME_RE.matcher(v.trim()).matches()) return "3-20 chars; letters, numbers, underscore only.";
        return null;
    }

    private String validateEmail(String v) {
        if (v == null || v.trim().isEmpty()) return "Email is required.";
        if (!EMAIL_RE.matcher(v.trim()).matches()) return "Enter a valid email address.";
        return null;
    }

    private String validatePassword(String v) {
        if (v == null || v.isEmpty()) return "Password is required.";
        if (!PASSWORD_RE.matcher(v).matches()) return "Min 8 chars, include upper, lower, number & special.";
        return null;
    }

    private String validateConfirm(String pwd, String confirm) {
        if (confirm == null || confirm.isEmpty()) return "Please confirm your password.";
        if (pwd == null || !pwd.equals(confirm)) return "Passwords do not match.";
        return null;
    }

    private String validateSSN(String v) {
        if (v == null || v.trim().isEmpty()) return "SSN is required.";
        if (!SSN_RE.matcher(v.trim()).matches()) return "SSN must be in format XXX-XX-XXXX.";
        return null;
    }

    private String validatePhone(String v) {
        if (v == null || v.trim().isEmpty()) return "Phone is required.";
        String normalized = v.replaceAll("[\\s()-]", "");
        if (!PHONE_RE.matcher(normalized).matches()) return "Enter phone (international, 10-15 digits).";
        return null;
    }

    private String validateAddress(String v) {
        if (v == null || v.trim().isEmpty()) return "Address is required.";
        if (v.trim().length() < 5) return "Address must be at least 5 characters.";
        return null;
    }

    private void applyValid(javafx.scene.control.Control control, String baseStyle, Label errLabel, BooleanProperty prop) {
        errLabel.setText("");
        control.setStyle(baseStyle + " -fx-border-color: #10b981; -fx-border-width: 1.5; -fx-border-radius:12;");
        DropShadow glow = new DropShadow(10, Color.web("#10b981"));
        glow.setSpread(0.06);
        control.setEffect(glow);
        prop.set(true);
    }

    private void applyInvalid(javafx.scene.control.Control control, String baseStyle, Label errLabel, String message, BooleanProperty prop) {
        errLabel.setText(message);
        control.setStyle(baseStyle + " -fx-border-color: #ef4444; -fx-border-width: 1.5; -fx-border-radius:12;");
        control.setEffect(null);
        // subtle shake to call attention (quick non-blocking)
        shake(control);
        prop.set(false);
    }

    private void shake(javafx.scene.Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(80), node);
        tt.setFromX(-6);
        tt.setToX(6);
        tt.setCycleCount(4);
        tt.setAutoReverse(true);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.playFromStart();
    }

}

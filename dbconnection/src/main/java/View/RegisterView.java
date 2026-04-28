package View;

import Services.PersonService;
import Services.SessionService;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import odb.Person;

public class RegisterView {
    private final BorderPane root = new BorderPane();
    private static final String BACK_ICON = "/images/arrow_back.png";
    private static final String HERO_IMAGE = "/images/photo_in_create_account.jpg";

    public RegisterView(
            PersonService personService,
            SessionService sessionService,
            boolean canGoBack,
            Runnable onBack,
            Runnable onSuccess,
            Runnable onLogin
    ) {
        root.getStyleClass().add("auth-root");

        root.setTop(buildTopBar(canGoBack, onBack));
        root.setCenter(buildPage(personService, sessionService, onSuccess, onLogin));
    }

    private HBox buildTopBar(boolean canGoBack, Runnable onBack) {
        HBox topBar = new HBox();
        topBar.getStyleClass().add("auth-topbar");

        Button backButton = new Button();
        backButton.getStyleClass().add("icon-button");
        backButton.setGraphic(imageIcon(BACK_ICON, 24, 24));
        backButton.setDisable(!canGoBack);
        backButton.setOnAction(event -> onBack.run());

        Label brand = new Label("Athenaeum Curator");
        brand.getStyleClass().add("auth-brand");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(backButton, brand, spacer);
        return topBar;
    }

    private ScrollPane buildPage(PersonService personService, SessionService sessionService, Runnable onSuccess, Runnable onLogin) {
        HBox shell = new HBox(24);
        shell.getStyleClass().add("auth-page");
        shell.setAlignment(Pos.CENTER);
        shell.setFillHeight(false);

        VBox hero = buildHeroPanel();
        VBox card = buildFormCard(personService, sessionService, onSuccess, onLogin);

        HBox.setHgrow(hero, Priority.ALWAYS);
        HBox.setHgrow(card, Priority.ALWAYS);
        shell.getChildren().addAll(hero, card);

        ScrollPane scrollPane = new ScrollPane(shell);
        scrollPane.getStyleClass().add("main-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scrollPane;
    }

    private VBox buildHeroPanel() {
        VBox hero = new VBox();
        hero.getStyleClass().add("auth-hero");
        hero.setMinWidth(380);
        hero.setPrefWidth(460);

        StackPane visual = new StackPane();
        visual.getStyleClass().add("auth-hero-image");
        visual.setMinHeight(620);
        visual.setMaxWidth(Double.MAX_VALUE);

        ImageView heroImage = new ImageView(new Image(getClass().getResourceAsStream(HERO_IMAGE)));
        heroImage.getStyleClass().add("auth-hero-photo");
        heroImage.setFitWidth(460);
        heroImage.setFitHeight(620);
        heroImage.setPreserveRatio(true);
        heroImage.setSmooth(true);
        heroImage.setCache(true);
        Rectangle clip = new Rectangle(460, 620);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        heroImage.setClip(clip);

        visual.getChildren().add(heroImage);
        hero.getChildren().add(visual);
        return hero;
    }

    private VBox buildFormCard(PersonService personService, SessionService sessionService, Runnable onSuccess, Runnable onLogin) {
        VBox card = new VBox(18);
        card.getStyleClass().add("auth-card");
        card.setMaxWidth(520);

        Label title = new Label("Create Account");
        title.getStyleClass().add("auth-card-title");
        Label subtitle = new Label("Begin your stewardship of the archives.");
        subtitle.getStyleClass().add("auth-card-subtitle");

        GridPane form = new GridPane();
        form.getStyleClass().add("auth-form");
        form.setHgap(12);
        form.setVgap(14);

        TextField nameField = textField("e.g. curator_alex");
        TextField emailField = textField("alex@library.org");
        PasswordField passwordField = passwordField("••••••••");
        PasswordField confirmField = passwordField("••••••••");
        TextField ssnField = textField("e.g. 303-22-4099");
        TextField phoneField = textField("e.g. +1 555 0199");
        TextField addressField = textField("e.g. North Wing, Room 402");
        ComboBox<String> genderField = genderCombo();

        form.addRow(0, labeledField("Username", nameField));
        form.addRow(1, labeledField("Email Address", emailField));
        form.addRow(2, labeledField("Password", passwordField));
        form.addRow(3, labeledField("Confirm Password", confirmField));
        form.addRow(4, labeledField("SSN", ssnField));
        form.addRow(5, labeledField("Phone", phoneField));
        form.addRow(6, labeledField("Address", addressField));
        form.addRow(7, labeledField("Gender", genderField));

        Label message = new Label();
        message.getStyleClass().add("auth-message");

        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("primary-button");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setOnAction(event -> {
            String password = passwordField.getText();
            String confirm = confirmField.getText();
            if (password == null || !password.equals(confirm)) {
                message.setText("Passwords do not match.");
                return;
            }
            try {
                Person person = personService.register(
                    nameField.getText(),
                    emailField.getText(),
                    password,
                    ssnField.getText(),
                    phoneField.getText(),
                    addressField.getText(),
                    genderField.getValue()
                );
                sessionService.setCurrentUser(person);
                onSuccess.run();
            } catch (RuntimeException ex) {
                message.setText(ex.getMessage());
            }
        });

        HBox footer = new HBox(6);
        footer.getStyleClass().add("auth-footer");
        Label prompt = new Label("Already a member of the archives?");
        prompt.getStyleClass().add("subtle-text");
        Button loginLink = new Button("Sign In");
        loginLink.getStyleClass().add("auth-link");
        loginLink.setOnAction(event -> onLogin.run());
        footer.getChildren().addAll(prompt, loginLink);

        card.getChildren().addAll(title, subtitle, form, registerButton, message, footer);
        return card;
    }

    private HBox labeledField(String labelText, javafx.scene.control.Control control) {
        VBox column = new VBox(6);
        column.setFillWidth(true);
        Label label = new Label(labelText.toUpperCase());
        label.getStyleClass().add("auth-label");
        control.getStyleClass().add("auth-input");
        control.setMaxWidth(Double.MAX_VALUE);
        control.setPrefWidth(380);
        column.getChildren().addAll(label, control);
        HBox row = new HBox(column);
        row.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(column, Priority.ALWAYS);
        return row;
    }

    private TextField textField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        return field;
    }

    private PasswordField passwordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        return field;
    }

    private ComboBox<String> genderCombo() {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll("Male", "Female");
        combo.setPromptText("Select gender");
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setPrefWidth(380);
        return combo;
    }

    private ImageView imageIcon(String path, double fitWidth, double fitHeight) {
        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(path)));
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

package View;

import Services.PersonService;
import Services.SessionService;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
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

public class LoginView {
    private final BorderPane root = new BorderPane();
    private static final String BACK_ICON = "/images/arrow_back.png";
    private static final String HERO_IMAGE = "/images/photo_in_signin.png";

    public LoginView(
            PersonService personService,
            SessionService sessionService,
            boolean canGoBack,
            Runnable onBack,
            Runnable onSuccess,
            Runnable onRegister
    ) {
        root.getStyleClass().add("auth-root");

        root.setTop(buildTopBar(canGoBack, onBack));
        root.setCenter(buildPage(personService, sessionService, onSuccess, onRegister));
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

    private ScrollPane buildPage(PersonService personService, SessionService sessionService, Runnable onSuccess, Runnable onRegister) {
        HBox shell = new HBox(24);
        shell.getStyleClass().add("auth-page");
        shell.setAlignment(Pos.CENTER);
        shell.setFillHeight(false);

        VBox hero = buildHeroPanel();
        VBox card = buildFormCard(personService, sessionService, onSuccess, onRegister);

        HBox.setHgrow(hero, Priority.ALWAYS);
        HBox.setHgrow(card, Priority.ALWAYS);
        shell.getChildren().addAll(hero, card);

        ScrollPane scrollPane = new ScrollPane(shell);
        scrollPane.getStyleClass().add("main-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
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

    private VBox buildFormCard(PersonService personService, SessionService sessionService, Runnable onSuccess, Runnable onRegister) {
        VBox card = new VBox(18);
        card.getStyleClass().add("auth-card");
        card.setMaxWidth(520);

        Label title = new Label("Sign In");
        title.getStyleClass().add("auth-card-title");
        Label subtitle = new Label("Enter your archive credentials to continue.");
        subtitle.getStyleClass().add("auth-card-subtitle");

        GridPane form = new GridPane();
        form.getStyleClass().add("auth-form");
        form.setHgap(12);
        form.setVgap(14);

        TextField emailField = textField("alex@library.org");
        PasswordField passwordField = passwordField("••••••••");

        form.addRow(0, labeledField("Email Address", emailField));
        form.addRow(1, labeledField("Password", passwordField));

        Label message = new Label();
        message.getStyleClass().add("auth-message");

        Button loginButton = new Button("Sign In");
        loginButton.getStyleClass().add("primary-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
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
        footer.getStyleClass().add("auth-footer");
        Label prompt = new Label("Need an account?");
        prompt.getStyleClass().add("subtle-text");
        Button registerLink = new Button("Register");
        registerLink.getStyleClass().add("auth-link");
        registerLink.setOnAction(event -> onRegister.run());
        footer.getChildren().addAll(prompt, registerLink);

        card.getChildren().addAll(title, subtitle, form, loginButton, message, footer);
        return card;
    }

    private HBox labeledField(String labelText, javafx.scene.control.TextInputControl control) {
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

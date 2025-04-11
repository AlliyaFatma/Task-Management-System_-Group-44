package frontend;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TaskManagerLogin extends Application {
    private TextField usernameField;
    private PasswordField passwordField;
    private ComboBox<String> userTypeComboBox;
    private Label messageLabel;

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1C2526;"); // Dark background

        Label titleLabel = new Label("Task Management System");
        styleLabel(titleLabel, true);

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        styleTextField(usernameField);

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        styleTextField(passwordField);

        userTypeComboBox = new ComboBox<>();
        userTypeComboBox.getItems().addAll("Manager", "Team Member"); // Standardized role values
        userTypeComboBox.setValue("Team Member");
        styleComboBox(userTypeComboBox);

        Button loginButton = new Button("Login");
        stylePrimaryButton(loginButton);
        loginButton.setOnAction(e -> handleLogin(primaryStage));

        Button registerButton = new Button("Register");
        styleSecondaryButton(registerButton);
        registerButton.setOnAction(e -> handleRegister());

        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #FF6F61;"); // Coral red for error messages

        root.getChildren().addAll(titleLabel, usernameField, passwordField, userTypeComboBox, loginButton, registerButton, messageLabel);

        Scene scene = new Scene(root, 400, 400);
        primaryStage.setTitle("TMS - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleLogin(Stage stage) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password.");
            return;
        }

        try (java.sql.Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT role FROM users WHERE username = ? AND password_hash = ?";
            java.sql.PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password));
            java.sql.ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                stage.close();
                if (role.equals("Manager")) {
                    new TaskManagementSystem(username).start(new Stage());
                } else {
                    new TaskManagementLandingPage(username).start(new Stage());
                }
            } else {
                messageLabel.setText("Invalid username or password.");
            }
        } catch (java.sql.SQLException e) {
            messageLabel.setText("Database error: " + e.getMessage());
        }
    }

    private void handleRegister() {
        // Launch the TaskManagerRegister form
        new TaskManagerRegister().start(new Stage());
    }

    private String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            return password;
        }
    }

    // Styling methods
    private void styleLabel(Label label, boolean isTitle) {
        label.setStyle("-fx-text-fill: #FFFFFF; -fx-font-family: 'Arial';" + (isTitle ? "-fx-font-size: 24;" : "-fx-font-size: 14;"));
    }

    private void styleTextField(TextField textField) {
        textField.setStyle("-fx-background-color: #283034; -fx-text-fill: #FFFFFF; -fx-prompt-text-fill: #A0A0A0; -fx-border-color: #3A4A4D; -fx-border-radius: 5; -fx-background-radius: 5;");
        textField.setPrefWidth(200);
    }

    private void styleComboBox(ComboBox<?> comboBox) {
        comboBox.setStyle("-fx-background-color: #283034; -fx-text-fill: #FFFFFF; -fx-border-color: #3A4A4D; -fx-border-radius: 5; -fx-background-radius: 5;");
        comboBox.setPrefWidth(200);
    }

    private void stylePrimaryButton(Button button) {
        button.setPrefWidth(200);
        button.setStyle("-fx-background-color: #3A4A4D; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-radius: 5; -fx-background-radius: 5;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #4A5A5D; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-radius: 5; -fx-background-radius: 5;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #3A4A4D; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-radius: 5; -fx-background-radius: 5;"));
    }

    private void styleSecondaryButton(Button button) {
        button.setPrefWidth(200);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-color: #3A4A4D; -fx-border-radius: 5; -fx-background-radius: 5;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #3A4A4D; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-color: #3A4A4D; -fx-border-radius: 5; -fx-background-radius: 5;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: transparent; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-color: #3A4A4D; -fx-border-radius: 5; -fx-background-radius: 5;"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
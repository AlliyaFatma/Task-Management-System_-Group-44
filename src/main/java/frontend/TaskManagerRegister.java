package frontend;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TaskManagerRegister extends Application {
    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1C2526;"); // Dark background

        Label title = new Label("Register");
        styleLabel(title, true);

        Label nameLabel = new Label("Full Name:");
        styleLabel(nameLabel, false);
        TextField nameField = new TextField();
        styleTextField(nameField);

        Label emailLabel = new Label("Email:");
        styleLabel(emailLabel, false);
        TextField emailField = new TextField();
        styleTextField(emailField);

        Label usernameLabel = new Label("Username:");
        styleLabel(usernameLabel, false);
        TextField usernameField = new TextField();
        styleTextField(usernameField);

        Label passwordLabel = new Label("Password:");
        styleLabel(passwordLabel, false);
        PasswordField passwordField = new PasswordField();
        styleTextField(passwordField);

        Label roleLabel = new Label("Role:");
        styleLabel(roleLabel, false);
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Manager", "Team Member"); // Standardized role values
        roleComboBox.setValue("Team Member");
        styleComboBox(roleComboBox);

        Button registerButton = new Button("Register");
        stylePrimaryButton(registerButton);
        registerButton.setOnAction(e -> handleRegister(primaryStage, nameField.getText(), emailField.getText(),
                usernameField.getText(), passwordField.getText(), roleComboBox.getValue()));

        root.getChildren().addAll(title, nameLabel, nameField, emailLabel, emailField, usernameLabel, usernameField,
                passwordLabel, passwordField, roleLabel, roleComboBox, registerButton);

        Scene scene = new Scene(root, 400, 500);
        primaryStage.setTitle("TMS - Register");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleRegister(Stage stage, String name, String email, String username, String password, String role) {
        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "All fields are required.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO users (full_name, email, username, password_hash, role) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, username);
            stmt.setString(4, hashPassword(password));
            stmt.setString(5, role);
            stmt.executeUpdate();
            showAlert("Success", "Registered successfully.");
            stage.close();
            new TaskManagerLogin().start(new Stage());
        } catch (SQLException e) {
            showAlert("Error", "Registration failed: " + e.getMessage());
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return password;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color: #1C2526; -fx-font-family: 'Arial';");
        alert.getDialogPane().lookup(".content").setStyle("-fx-text-fill: #FFFFFF;");
        alert.showAndWait();
    }

    // Styling methods (copied from TaskManagerLogin.java for consistency)
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

    public static void main(String[] args) {
        launch(args);
    }
}
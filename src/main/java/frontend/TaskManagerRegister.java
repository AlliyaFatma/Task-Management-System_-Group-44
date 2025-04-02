package frontend;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TaskManagerRegister extends Application {
    @Override
    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25));

        Label title = new Label("Register");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        grid.add(title, 0, 0, 2, 1);

        Label nameLabel = new Label("Full Name:");
        TextField nameField = new TextField();
        grid.add(nameLabel, 0, 1);
        grid.add(nameField, 1, 1);

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        grid.add(emailLabel, 0, 2);
        grid.add(emailField, 1, 2);

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        grid.add(usernameLabel, 0, 3);
        grid.add(usernameField, 1, 3);

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        grid.add(passwordLabel, 0, 4);
        grid.add(passwordField, 1, 4);

        Label roleLabel = new Label("Role:");
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Team Member", "Project Manager");
        roleComboBox.setValue("Team Member");
        grid.add(roleLabel, 0, 5);
        grid.add(roleComboBox, 1, 5);

        Button registerButton = new Button("Register");
        registerButton.setOnAction(e -> handleRegister(primaryStage, nameField.getText(), emailField.getText(),
                usernameField.getText(), passwordField.getText(), roleComboBox.getValue()));
        grid.add(registerButton, 1, 6);

        Scene scene = new Scene(grid, 400, 300);
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
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
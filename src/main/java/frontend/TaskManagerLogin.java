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
import java.sql.ResultSet;
import java.sql.SQLException;

public class TaskManagerLogin extends Application {
    @Override
    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25));

        Label title = new Label("Login");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        grid.add(title, 0, 0, 2, 1);

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 1, 1);

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);

        Label roleLabel = new Label("Role:");
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Team Member", "Project Manager");
        roleComboBox.setValue("Team Member");
        grid.add(roleLabel, 0, 3);
        grid.add(roleComboBox, 1, 3);

        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> handleLogin(primaryStage, usernameField.getText(), passwordField.getText(), roleComboBox.getValue()));
        grid.add(loginButton, 1, 4);

        Button registerButton = new Button("Register");
        registerButton.setOnAction(e -> {
            primaryStage.close();
            new TaskManagerRegister().start(new Stage());
        });
        grid.add(registerButton, 1, 5);

        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleLogin(Stage stage, String username, String password, String role) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Username and password are required.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT password_hash, role FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String inputHash = hashPassword(password);
                String storedRole = rs.getString("role");

                if (storedHash.equals(inputHash) && storedRole.equals(role)) {
                    stage.close();
                    if (role.equals("Project Manager")) {
                        new TaskManagementSystem(username).start(new Stage());
                    } else {
                        new TaskManagementLandingPage(username).start(new Stage());
                    }
                } else {
                    showAlert("Error", "Invalid username, password, or role.");
                }
            } else {
                showAlert("Error", "User not found.");
            }
        } catch (SQLException e) {
            showAlert("Error", "Login failed: " + e.getMessage());
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
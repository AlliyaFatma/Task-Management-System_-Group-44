package frontend;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TaskManagerRegister extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Task Management System - Registration");

        // Create the grid pane layout
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // Add background color
        grid.setBackground(new Background(new BackgroundFill(
                Color.rgb(240, 240, 240), CornerRadii.EMPTY, Insets.EMPTY)));

        // Create a header
        Text sceneTitle = new Text("Task Management System");
        sceneTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        sceneTitle.setFill(Color.rgb(44, 62, 80));
        grid.add(sceneTitle, 0, 0, 2, 1);

        // Add registration subtitle
        Text registerText = new Text("Create a new account");
        registerText.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        grid.add(registerText, 0, 1, 2, 1);

        // Full Name label and field
        Label nameLabel = new Label("Full Name:");
        grid.add(nameLabel, 0, 2);

        TextField nameField = new TextField();
        nameField.setPromptText("Enter your full name");
        grid.add(nameField, 1, 2);

        // Email label and field
        Label emailLabel = new Label("Email:");
        grid.add(emailLabel, 0, 3);

        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email address");
        grid.add(emailField, 1, 3);

        // Username label and field
        Label usernameLabel = new Label("Username:");
        grid.add(usernameLabel, 0, 4);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");
        grid.add(usernameField, 1, 4);

        // Password label and field
        Label passwordLabel = new Label("Password:");
        grid.add(passwordLabel, 0, 5);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Create a password");
        grid.add(passwordField, 1, 5);

        // Confirm Password label and field
        Label confirmPasswordLabel = new Label("Confirm Password:");
        grid.add(confirmPasswordLabel, 0, 6);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        grid.add(confirmPasswordField, 1, 6);

        // Role selection
        Label roleLabel = new Label("Role:");
        grid.add(roleLabel, 0, 7);

        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Team Member", "Project Manager");
        roleComboBox.setValue("Team Member");
        roleComboBox.setPrefWidth(200);
        grid.add(roleComboBox, 1, 7);

        // Terms and conditions checkbox
        CheckBox termsCheckBox = new CheckBox("I agree to the Terms and Conditions");
        grid.add(termsCheckBox, 1, 8);

        // Register button
        Button registerButton = new Button("Register");
        registerButton.setPrefWidth(120);

        // Style the button
        registerButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

        // Button action
        registerButton.setOnAction(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            String role = roleComboBox.getValue();
            boolean agreedToTerms = termsCheckBox.isSelected();

            if (name.isEmpty() || email.isEmpty() || username.isEmpty() ||
                    password.isEmpty() || confirmPassword.isEmpty()) {
                showAlert("Error", "Please fill in all fields.");
            } else if (!password.equals(confirmPassword)) {
                showAlert("Error", "Passwords do not match.");
            } else if (!agreedToTerms) {
                showAlert("Error", "You must agree to the Terms and Conditions.");
            } else if (!isValidEmail(email)) {
                showAlert("Error", "Please enter a valid email address.");
            } else {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String query = "INSERT INTO users (full_name, email, username, password_hash, role, agreed_to_terms) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, name);
                    stmt.setString(2, email);
                    stmt.setString(3, username);
                    stmt.setString(4, hashPassword(password)); // Replace BCrypt with custom hash
                    stmt.setString(5, role);
                    stmt.setBoolean(6, agreedToTerms);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        showAlert("Success", "Registration successful for username: " + username);
                        primaryStage.close();
                        new TaskManagerLogin().start(new Stage());
                    }
                } catch (SQLException ex) {
                    showAlert("Error", "Registration failed: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        // Create HBox for register button to center it
        HBox hbRegisterBtn = new HBox(10);
        hbRegisterBtn.setAlignment(Pos.CENTER);
        hbRegisterBtn.getChildren().add(registerButton);
        grid.add(hbRegisterBtn, 1, 9);

        // Back to login link
        HBox loginBox = new HBox(5);
        loginBox.setAlignment(Pos.CENTER_LEFT);
        Text alreadyHaveAccount = new Text("Already have an account?");
        Hyperlink loginLink = new Hyperlink("Login");
        loginLink.setOnAction(e -> {
            TaskManagerLogin loginStage = new TaskManagerLogin();
            try {
                loginStage.start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        loginBox.getChildren().addAll(alreadyHaveAccount, loginLink);
        grid.add(loginBox, 1, 10);

        // Create scene
        Scene scene = new Scene(grid, 450, 550);
        primaryStage.setScene(scene);

        // Set min size for the stage
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(470);

        primaryStage.show();
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
            e.printStackTrace();
            return password; // Fallback (not secure)
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
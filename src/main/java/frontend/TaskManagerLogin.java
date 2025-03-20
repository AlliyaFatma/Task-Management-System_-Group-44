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
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert.AlertType;

public class TaskManagerLogin extends Application {

    private ComboBox<String> roleComboBox;
    private TextField usernameField;
    private PasswordField passwordField;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Task Management System");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        grid.setBackground(new Background(new BackgroundFill(
                Color.rgb(240, 240, 240), CornerRadii.EMPTY, Insets.EMPTY)));

        Text sceneTitle = new Text("Task Management System");
        sceneTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        sceneTitle.setFill(Color.rgb(44, 62, 80));
        grid.add(sceneTitle, 0, 0, 2, 1);

        Text loginText = new Text("Login to continue");
        loginText.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        grid.add(loginText, 0, 1, 2, 1);

        Label usernameLabel = new Label("Username:");
        grid.add(usernameLabel, 0, 2);

        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        grid.add(usernameField, 1, 2);

        Label passwordLabel = new Label("Password:");
        grid.add(passwordLabel, 0, 3);

        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        grid.add(passwordField, 1, 3);

        Label roleLabel = new Label("Role:");
        grid.add(roleLabel, 0, 4);

        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Team Member", "Project Manager");
        roleComboBox.setPromptText("Select your role");
        grid.add(roleComboBox, 1, 4);

        CheckBox rememberMe = new CheckBox("Remember me");
        grid.add(rememberMe, 1, 5);

        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(120);
        loginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

        loginButton.setOnAction(this::handleLogin);

        Hyperlink forgotPassword = new Hyperlink("Forgot Password?");
        forgotPassword.setOnAction(e -> {
            showAlert("Information", "Password reset functionality will be implemented here.");
        });

        Hyperlink registerLink = new Hyperlink("Create an account");
        registerLink.setOnAction(e -> {
            TaskManagerRegister registerStage = new TaskManagerRegister();
            try {
                registerStage.start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        HBox hbLoginBtn = new HBox(10);
        hbLoginBtn.setAlignment(Pos.CENTER);
        hbLoginBtn.getChildren().add(loginButton);
        grid.add(hbLoginBtn, 1, 6);

        grid.add(forgotPassword, 1, 7);

        HBox registerBox = new HBox(5);
        registerBox.setAlignment(Pos.CENTER_LEFT);
        registerBox.getChildren().addAll(new Text("Don't have an account?"), registerLink);
        grid.add(registerBox, 1, 8);

        Scene scene = new Scene(grid, 400, 450);
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(500);
        primaryStage.setMinWidth(420);
        primaryStage.show();
    }

    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String selectedRole = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || selectedRole == null) {
            showAlert("Error", "Please fill all fields: username, password, and role.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT password_hash, role FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String dbRole = rs.getString("role");
                String hashedInput = hashPassword(password);

                if (hashedInput.equals(storedHash) && selectedRole.equals(dbRole)) {
                    showAlert("Success", "Login successful for " + selectedRole + ": " + username);

                    // Close the login window
                    primaryStage.close();

                    //Open the correct landing page based on Role
                    openTaskManagementSystem(username, selectedRole);


                } else {
                    showAlert("Error", "Invalid credentials or role mismatch.");
                }
            } else {
                showAlert("Error", "User not found.");
            }
        } catch (SQLException ex) {
            showAlert("Error", "Database connection failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void openTaskManagementSystem(String username, String selectedRole) {
        Stage newStage = new Stage(); // Create a new Stage

        if (selectedRole.equals("Team Member")) {
            TaskManagementLandingPage landingPage = new TaskManagementLandingPage(username);
            landingPage.start(newStage); // Pass the new stage to start
        } else if (selectedRole.equals("Project Manager")) {
            TaskManagementSystem tms = new TaskManagementSystem(username, selectedRole);
            tms.start(newStage); // Pass the new stage to start
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
            e.printStackTrace();
            return password;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
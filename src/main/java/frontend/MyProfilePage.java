package frontend;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MyProfilePage extends VBox {
    private String username;
    private String role;
    private Label messageLabel;
    private ImageView profileImageView;
    private String profilePicturePath;

    public MyProfilePage(String username, String role) {
        this.username = username;
        this.role = role;
        setSpacing(15);
        setPadding(new Insets(20));
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #1C2526;");

        VBox profileCard = new VBox(10);
        profileCard.setPadding(new Insets(15));
        profileCard.setStyle("-fx-background-color: #283034; -fx-border-color: #3A4A4D; -fx-border-radius: 5; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");

        profileImageView = new ImageView();
        profileImageView.setFitWidth(100);
        profileImageView.setFitHeight(100);
        profilePicturePath = fetchProfilePicturePath();
        if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
            try {
                profileImageView.setImage(new Image(new File(profilePicturePath).toURI().toString()));
            } catch (Exception e) {
                profileImageView.setImage(new Image("file:default_profile.png", true));
            }
        } else {
            profileImageView.setImage(new Image("file:default_profile.png", true));
        }

        Button uploadImageButton = new Button("Upload Profile Picture");
        stylePrimaryButton(uploadImageButton);
        uploadImageButton.setTooltip(new Tooltip("Upload a new profile picture (PNG, JPG)"));
        uploadImageButton.setOnAction(e -> uploadProfilePicture());

        String[] userDetails = fetchUserDetails();
        String fullName = userDetails[0];
        String email = userDetails[1];

        Label titleLabel = new Label("My Profile");
        styleLabel(titleLabel, true);

        Label roleLabel = new Label("Role: " + role);
        styleLabel(roleLabel, false);

        Label fullNameLabel = new Label("Full Name:");
        styleLabel(fullNameLabel, false);
        TextField fullNameField = new TextField(fullName);
        fullNameField.setPromptText("Full Name");
        fullNameField.setTooltip(new Tooltip("Enter your full name"));
        styleTextField(fullNameField);

        Label emailLabel = new Label("Email:");
        styleLabel(emailLabel, false);
        TextField emailField = new TextField(email);
        emailField.setPromptText("Email");
        emailField.setTooltip(new Tooltip("Enter your email address"));
        styleTextField(emailField);

        Label passwordLabel = new Label("Password:");
        styleLabel(passwordLabel, false);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("New Password (leave blank to keep current)");
        passwordField.setTooltip(new Tooltip("Enter a new password (minimum 8 characters)"));
        styleTextField(passwordField);

        emailField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !newValue.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                emailField.setStyle("-fx-background-color: #283034; -fx-text-fill: #FFFFFF; -fx-prompt-text-fill: #A0A0A0; -fx-border-color: #FF6F61; -fx-border-radius: 5; -fx-background-radius: 5;");
            } else {
                emailField.setStyle("-fx-background-color: #283034; -fx-text-fill: #FFFFFF; -fx-prompt-text-fill: #A0A0A0; -fx-border-color: #3A4A4D; -fx-border-radius: 5; -fx-background-radius: 5;");
            }
        });

        Label passwordStrengthLabel = new Label();
        styleLabel(passwordStrengthLabel, false);
        passwordField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                passwordStrengthLabel.setText("");
            } else if (newValue.length() < 8) {
                passwordStrengthLabel.setText("Password too short (min 8 chars)");
                passwordStrengthLabel.setStyle("-fx-text-fill: #FF6F61;");
            } else {
                passwordStrengthLabel.setText("Password strength: Good");
                passwordStrengthLabel.setStyle("-fx-text-fill: #4CAF50;");
            }
        });

        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #FF6F61; -fx-font-size: 12;");

        Button saveButton = new Button("Save Changes");
        stylePrimaryButton(saveButton);
        saveButton.setTooltip(new Tooltip("Save your profile changes"));
        saveButton.setOnAction(e -> saveProfile(fullNameField.getText(), emailField.getText(), passwordField.getText()));

        profileCard.getChildren().addAll(
                profileImageView, uploadImageButton, titleLabel, roleLabel,
                fullNameLabel, fullNameField,
                emailLabel, emailField,
                passwordLabel, passwordField, passwordStrengthLabel,
                saveButton, messageLabel
        );

        getChildren().add(profileCard);
    }

    private String[] fetchUserDetails() {
        String[] details = new String[2];
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT full_name, email FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                details[0] = rs.getString("full_name");
                details[1] = rs.getString("email");
            }
        } catch (SQLException e) {
            messageLabel.setText("Error fetching details: " + e.getMessage());
        }
        return details;
    }

    private String fetchProfilePicturePath() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT profile_picture FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("profile_picture");
            }
        } catch (SQLException e) {
            messageLabel.setText("Error fetching profile picture: " + e.getMessage());
        }
        return null;
    }

    private void uploadProfilePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            profilePicturePath = file.getAbsolutePath();
            Image image = new Image(file.toURI().toString());
            profileImageView.setImage(image);
            messageLabel.setText("Profile picture updated. Click 'Save Changes' to persist.");
            messageLabel.setStyle("-fx-text-fill: #4CAF50;");
        }
    }

    private void saveProfile(String fullName, String email, String password) {
        if (fullName.isEmpty() || email.isEmpty()) {
            messageLabel.setText("Full Name and Email are required.");
            return;
        }
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            messageLabel.setText("Invalid email format.");
            return;
        }
        if (!password.isEmpty() && password.length() < 8) {
            messageLabel.setText("Password must be at least 8 characters long.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE users SET full_name = ?, email = ?, profile_picture = ?" + (password.isEmpty() ? "" : ", password_hash = ?") + " WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, profilePicturePath);
            if (!password.isEmpty()) {
                stmt.setString(4, hashPassword(password));
                stmt.setString(5, username);
            } else {
                stmt.setString(4, username);
            }
            stmt.executeUpdate();
            messageLabel.setText("Profile updated successfully!");
            messageLabel.setStyle("-fx-text-fill: #4CAF50;");
        } catch (SQLException e) {
            messageLabel.setText("Error updating profile: " + e.getMessage());
        }
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

    private void stylePrimaryButton(Button button) {
        button.setPrefWidth(160);
        button.setStyle("-fx-background-color: #3A4A4D; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-radius: 5; -fx-background-radius: 5;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #4A5A5D; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-radius: 5; -fx-background-radius: 5;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #3A4A4D; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-radius: 5; -fx-background-radius: 5;"));
    }
}
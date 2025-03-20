package frontend;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class MyProfilePage extends VBox { // Extends VBox
    private String currentUserRole = "Employee"; // Can be "Manager" or "Employee"
    private User currentUser; // Simulated user data

    // Simple User class for demonstration
    private static class User {
        String name;
        String email;
        String role;

        User(String name, String email, String role) {
            this.name = name;
            this.email = email;
            this.role = role;
        }
    }

    public MyProfilePage(String username, String role) {
        this.currentUserRole = role;  // Store the role
        // Initialize sample user data
        currentUser = new User(username, "john.doe@example.com", currentUserRole);
        initialize();
    }

    private void initialize() {
        this.setPadding(new Insets(20));
        this.setAlignment(Pos.CENTER);
        this.setSpacing(20);

        // Title
        Label titleLabel = new Label("My Profile");
        titleLabel.setFont(Font.font("Arial", 24));

        // Profile details
        VBox detailsBox = new VBox(15);
        detailsBox.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("Name: " + currentUser.name);
        Label emailLabel = new Label("Email: " + currentUser.email);
        Label roleLabel = new Label("Role: " + currentUser.role);

        styleDetailLabel(nameLabel);
        styleDetailLabel(emailLabel);
        styleDetailLabel(roleLabel);

        detailsBox.getChildren().addAll(nameLabel, emailLabel, roleLabel);

        // Action buttons
        HBox buttonBox = new HBox(10);
        Button editButton = new Button("Edit Profile");
        Button changePasswordButton = new Button("Change Password");
        styleActionButton(editButton);
        styleActionButton(changePasswordButton);

        editButton.setOnAction(e -> showEditProfileDialog());
        changePasswordButton.setOnAction(e -> showChangePasswordDialog());

        buttonBox.getChildren().addAll(editButton, changePasswordButton);

        this.getChildren().addAll(titleLabel, detailsBox, buttonBox); // Add to MyProfilePage (VBox)
    }

    private void styleDetailLabel(Label label) {
        label.setFont(Font.font("Arial", 16));
        label.setTextFill(Color.DARKGRAY);
    }

    private void styleActionButton(Button button) {
        button.setPrefWidth(150);
        button.setPrefHeight(40);
        button.setFont(Font.font("Arial", 14));
        button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        button.setOnMouseEntered(e ->
                button.setStyle("-fx-background-color: #45a049; -fx-text-fill: white;")
        );
        button.setOnMouseExited(e ->
                button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;")
        );
    }

    private void showEditProfileDialog() {
        Stage dialog = new Stage();
        VBox dialogPane = new VBox(10);
        dialogPane.setPadding(new Insets(10));

        TextField nameField = new TextField(currentUser.name);
        TextField emailField = new TextField(currentUser.email);

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            currentUser.name = nameField.getText();
            currentUser.email = emailField.getText();
            initialize(); // Refresh display
            dialog.close();
        });

        dialogPane.getChildren().addAll(
                new Label("Edit Profile"),
                new Label("Name:"), nameField,
                new Label("Email:"), emailField,
                saveButton
        );

        Scene dialogScene = new Scene(dialogPane, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void showChangePasswordDialog() {
        Stage dialog = new Stage();
        VBox dialogPane = new VBox(10);
        dialogPane.setPadding(new Insets(10));

        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Current Password");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm New Password");

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            String currentPassword = currentPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (newPassword.equals(confirmPassword)) {
                // In a real app, validate current password and update in database
                showAlert("Success", "Password changed successfully!");
                dialog.close();
            } else {
                showAlert("Error", "New passwords do not match!");
            }
        });

        dialogPane.getChildren().addAll(
                new Label("Change Password"),
                new Label("Current Password:"), currentPasswordField,
                new Label("New Password:"), newPasswordField,
                new Label("Confirm Password:"), confirmPasswordField,
                saveButton
        );

        Scene dialogScene = new Scene(dialogPane, 300, 250);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void showAlert(String title, String message) {
        Stage alert = new Stage();
        VBox alertPane = new VBox(20);
        alertPane.setAlignment(Pos.CENTER);
        Label msgLabel = new Label(message);
        Button okButton = new Button("OK");

        okButton.setOnAction(e -> alert.close());

        alertPane.getChildren().addAll(msgLabel, okButton);
        Scene alertScene = new Scene(alertPane, 250, 150);
        alert.setScene(alertScene);
        alert.setTitle(title);
        alert.show();
    }

}
//package frontend;
//
//import javafx.application.Application;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.layout.*;
//import javafx.scene.paint.Color;
//import javafx.scene.text.Font;
//import javafx.stage.Stage;
//
//public class HomePage extends Application {
//    private StackPane contentPane;
//    private String currentUserRole = "Employee"; // Can be "Manager" or "Employee" - set based on login
//
//    @Override
//    public void start(Stage primaryStage) {
//        // Main layout
//        BorderPane root = new BorderPane();
//
//        // Create side menu
//        VBox sideMenu = createSideMenu(primaryStage);
//
//        // Create content area
//        contentPane = new StackPane();
//        contentPane.setStyle("-fx-background-color: white;");
//        updateHomeContent();
//
//        // Set up the layout
//        root.setLeft(sideMenu);
//        root.setCenter(contentPane);
//
//        // Create and configure scene
//        Scene scene = new Scene(root, 800, 600);
//        primaryStage.setTitle("Task Management System - Home");
//        primaryStage.setScene(scene);
//        primaryStage.show();
//    }
//
//    private VBox createSideMenu(Stage primaryStage) {
//        VBox sideMenu = new VBox(10);
//        sideMenu.setPadding(new Insets(20));
//        sideMenu.setPrefWidth(200);
//        sideMenu.setStyle("-fx-background-color: #283034;");
//
//        String[] menuItems = {"Home", "All Tasks", "My Tasks", "My Profile", "Logout"};
//
//        for (String item : menuItems) {
//            Button menuButton = new Button(item);
//            styleMenuButton(menuButton);
//
//            menuButton.setOnAction(e -> {
//                if (item.equals("Logout")) {
//                    showLogoutDialog(primaryStage);
//                } else {
//                    // For this Home Page example, we'll just update content
//                    updateContent(item);
//                }
//            });
//
//            sideMenu.getChildren().add(menuButton);
//        }
//
//        return sideMenu;
//    }
//
//    private void styleMenuButton(Button button) {
//        button.setPrefWidth(160);
//        button.setPrefHeight(40);
//        button.setFont(Font.font("Arial", 14));
//        button.setStyle("-fx-background-color: #3f51b5; -fx-text-fill: white;");
//
//        button.setOnMouseEntered(e ->
//                button.setStyle("-fx-background-color: #303f9f; -fx-text-fill: white;")
//        );
//        button.setOnMouseExited(e ->
//                button.setStyle("-fx-background-color: #3f51b5; -fx-text-fill: white;")
//        );
//    }
//
//    private void updateContent(String page) {
//        if (page.equals("Home")) {
//            updateHomeContent();
//        } else {
//            contentPane.getChildren().clear();
//            Label contentLabel = new Label("Welcome to " + page + " Page");
//            contentLabel.setFont(Font.font("Arial", 20));
//            contentPane.getChildren().add(contentLabel);
//        }
//    }
//
//    private void updateHomeContent() {
//        contentPane.getChildren().clear();
//
//        // Create home content based on user role
//        VBox homeContent = new VBox(20);
//        homeContent.setPadding(new Insets(20));
//        homeContent.setAlignment(Pos.CENTER);
//
//        // Welcome message
//        Label welcomeLabel = new Label("Welcome to Task Management System");
//        welcomeLabel.setFont(Font.font("Arial", 24));
//
//        // Quick stats or overview
//        VBox statsBox = new VBox(10);
//        statsBox.setAlignment(Pos.CENTER);
//
//        if (currentUserRole.equals("Manager")) {
//            Label totalTasks = new Label("Total Tasks: 25"); // Example data
//            Label pendingTasks = new Label("Pending Tasks: 10");
//            Label completedTasks = new Label("Completed Tasks: 15");
//            styleStatLabel(totalTasks);
//            styleStatLabel(pendingTasks);
//            styleStatLabel(completedTasks);
//            statsBox.getChildren().addAll(totalTasks, pendingTasks, completedTasks);
//
//            Button createTaskButton = new Button("Create New Task");
//            styleActionButton(createTaskButton);
//            createTaskButton.setOnAction(e -> showCreateTaskDialog());
//
//            homeContent.getChildren().addAll(welcomeLabel, statsBox, createTaskButton);
//        } else { // Employee view
//            Label myTasks = new Label("My Tasks: 5"); // Example data
//            Label dueToday = new Label("Due Today: 2");
//            styleStatLabel(myTasks);
//            styleStatLabel(dueToday);
//            statsBox.getChildren().addAll(myTasks, dueToday);
//
//            Button updateStatusButton = new Button("Update Task Status");
//            styleActionButton(updateStatusButton);
//            updateStatusButton.setOnAction(e -> showUpdateStatusDialog());
//
//            homeContent.getChildren().addAll(welcomeLabel, statsBox, updateStatusButton);
//        }
//
//        contentPane.getChildren().add(homeContent);
//    }
//
//    private void styleStatLabel(Label label) {
//        label.setFont(Font.font("Arial", 16));
//        label.setTextFill(Color.DARKGRAY);
//    }
//
//    private void styleActionButton(Button button) {
//        button.setPrefWidth(200);
//        button.setPrefHeight(40);
//        button.setFont(Font.font("Arial", 14));
//        button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
//
//        button.setOnMouseEntered(e ->
//                button.setStyle("-fx-background-color: #45a049; -fx-text-fill: white;")
//        );
//        button.setOnMouseExited(e ->
//                button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;")
//        );
//    }
//
//    private void showCreateTaskDialog() {
//        // Simple dialog for task creation (Manager only)
//        Stage dialog = new Stage();
//        VBox dialogPane = new VBox(10);
//        dialogPane.setPadding(new Insets(10));
//
//        TextField titleField = new TextField();
//        titleField.setPromptText("Task Title");
//        TextArea descArea = new TextArea();
//        descArea.setPromptText("Description");
//        descArea.setPrefHeight(100);
//
//        Button createButton = new Button("Create");
//        createButton.setOnAction(e -> {
//            // Add task creation logic here (to be connected to backend)
//            dialog.close();
//        });
//
//        dialogPane.getChildren().addAll(
//                new Label("Create New Task"), titleField, descArea, createButton
//        );
//
//        Scene dialogScene = new Scene(dialogPane, 300, 250);
//        dialog.setScene(dialogScene);
//        dialog.show();
//    }
//
//    private void showUpdateStatusDialog() {
//        // Simple dialog for status update (Employee)
//        Stage dialog = new Stage();
//        VBox dialogPane = new VBox(10);
//        dialogPane.setPadding(new Insets(10));
//
//        ComboBox<String> statusBox = new ComboBox<>();
//        statusBox.getItems().addAll("To-Do", "In Progress", "Completed");
//        statusBox.setValue("To-Do");
//
//        Button updateButton = new Button("Update");
//        updateButton.setOnAction(e -> {
//            // Add status update logic here (to be connected to backend)
//            dialog.close();
//        });
//
//        dialogPane.getChildren().addAll(
//                new Label("Update Task Status"), statusBox, updateButton
//        );
//
//        Scene dialogScene = new Scene(dialogPane, 250, 150);
//        dialog.setScene(dialogScene);
//        dialog.show();
//    }
//
//    private void showLogoutDialog(Stage primaryStage) {
//        Stage dialog = new Stage();
//        VBox dialogPane = new VBox(20);
//        dialogPane.setAlignment(Pos.CENTER);
//        Label message = new Label("Are you sure you want to logout?");
//        Button yesButton = new Button("Yes");
//        Button noButton = new Button("No");
//
//        yesButton.setOnAction(e -> {
//            dialog.close();
//            primaryStage.close(); // Could redirect to login page instead
//        });
//
//        noButton.setOnAction(e -> dialog.close());
//
//        dialogPane.getChildren().addAll(message, yesButton, noButton);
//        Scene dialogScene = new Scene(dialogPane, 300, 150);
//        dialog.setScene(dialogScene);
//        dialog.show();
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}
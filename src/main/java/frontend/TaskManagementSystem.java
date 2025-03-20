package frontend;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskManagementSystem extends Application {

    private StackPane contentPane;
    private String currentUserRole = "Manager"; // Can be "Manager" or "Employee"
    private String currentUsername = "john.doe"; // Example username - REPLACE THIS!
    private static final String DB_URL = "jdbc:mysql://localhost:3306/TaskManagementDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "zeenunew100"; // Replace with your actual password

    public TaskManagementSystem() {
        // Required no-argument constructor for JavaFX
    }

    public TaskManagementSystem(String username, String selectedRole) {
        this.currentUsername = username; // Initialize currentUsername
        this.currentUserRole = selectedRole; // Initialize currentUserRole
    }

    @Override
    public void start(Stage primaryStage) {
        // Main layout
        BorderPane root = new BorderPane();

        // Create side menu
        VBox sideMenu = createSideMenu(primaryStage);

        // Create content area
        contentPane = new StackPane();
        contentPane.setStyle("-fx-background-color: white;");
        updateContent("Home", primaryStage);

        // Set up the layout
        root.setLeft(sideMenu);
        root.setCenter(contentPane);

        // Create and configure scene
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Task Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createSideMenu(Stage primaryStage) {
        VBox sideMenu = new VBox(10);
        sideMenu.setPadding(new Insets(20));
        sideMenu.setPrefWidth(200);
        sideMenu.setStyle("-fx-background-color: #283034;");

        String[] menuItems = {"Home", "My Profile", "Logout"};

        // Create "Open All Tasks" Button
        Button allTasksButton = new Button("All Tasks");
        styleMenuButton(allTasksButton);
        allTasksButton.setOnAction(e -> {
            AllTasksPage allTasksPage = new AllTasksPage(currentUserRole);
            Stage allTasksStage = new Stage(); // Create a new stage
            allTasksStage.setOnCloseRequest(event -> {
                // Refresh tasks in home
                updateContent("Home", primaryStage);
            });
            allTasksPage.start(allTasksStage); // Start the AllTasksPage GUI in a new window
        });
        sideMenu.getChildren().add(allTasksButton);

        for (String item : menuItems) {
            Button menuButton = new Button(item);
            styleMenuButton(menuButton);

            menuButton.setOnAction(e -> {
                if (item.equals("Logout")) {
                    showLogoutDialog(primaryStage);
                } else {
                    updateContent(item, primaryStage);
                }
            });

            sideMenu.getChildren().add(menuButton);
        }

        return sideMenu;
    }

    private void styleMenuButton(Button button) {
        button.setPrefWidth(160);
        button.setPrefHeight(40);
        button.setFont(Font.font("Arial", 14));
        button.setStyle("-fx-background-color: #3f51b5; -fx-text-fill: white;");

        button.setOnMouseEntered(e ->
                button.setStyle("-fx-background-color: #303f9f; -fx-text-fill: white;")
        );
        button.setOnMouseExited(e ->
                button.setStyle("-fx-background-color: #3f51b5; -fx-text-fill: white;")
        );
    }

    public void updateContent(String page, Stage primaryStage) {
        contentPane.getChildren().clear();

        if (page.equals("Home")) {
            createHomePageContent();
        } else if (page.equals("My Profile")) {
            MyProfilePage myProfilePage = new MyProfilePage(currentUsername, currentUserRole); //create a variable for primary stage
            contentPane.getChildren().add(myProfilePage);
        } else {
            Label contentLabel = new Label("Welcome to " + page + " Page");
            contentLabel.setFont(Font.font("Arial", 20));
            contentPane.getChildren().add(contentLabel);
        }
    }

    private void createHomePageContent() {
        List<AllTasksPage.Task> taskList = loadTasksFromDatabase();

        // Calculate task counts
        int totalTasks = taskList.size();
        int pendingTasks = (int) taskList.stream().filter(task -> task.status.equals("To-Do") || task.status.equals("In Progress")).count();
        int completedTasks = (int) taskList.stream().filter(task -> task.status.equals("Completed")).count();

        // Create VBox to display task information in the content area
        VBox homeContent = new VBox(10);
        homeContent.setAlignment(Pos.CENTER);
        homeContent.setPadding(new Insets(20));

        Label welcomeLabel = new Label("Welcome, " + currentUsername);  // Display username
        welcomeLabel.setFont(Font.font("Arial", 24));

        Label totalLabel = new Label("Total Tasks: " + totalTasks);
        totalLabel.setFont(Font.font("Arial", 14));

        Label pendingLabel = new Label("Pending Tasks: " + pendingTasks);
        pendingLabel.setFont(Font.font("Arial", 14));

        Label completedLabel = new Label("Completed Tasks: " + completedTasks);
        completedLabel.setFont(Font.font("Arial", 14));

        homeContent.getChildren().addAll(welcomeLabel, totalLabel, pendingLabel, completedLabel);
        contentPane.getChildren().add(homeContent);
    }

    private List<AllTasksPage.Task> loadTasksFromDatabase() {
        List<AllTasksPage.Task> taskList = new ArrayList<>(); // Clear existing tasks
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM tasks")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String status = rs.getString("status");
                String priority = rs.getString("priority");
                String deadline = rs.getString("deadline");
                String assignedTo = rs.getString("assigned_to");

                taskList.add(new AllTasksPage.Task(id, title, description, status, priority, deadline, assignedTo));
            }
        } catch (SQLException e) {
            System.err.println("Error loading tasks from database: " + e.getMessage());
            e.printStackTrace();
            // Handle the exception appropriately, e.g., show an alert to the user
        }
        return taskList;
    }

    private void showLogoutDialog(Stage primaryStage) {
        Stage dialog = new Stage();
        VBox dialogPane = new VBox(20);
        dialogPane.setAlignment(Pos.CENTER);
        Label message = new Label("Are you sure you want to logout?");
        Button yesButton = new Button("Yes");
        Button noButton = new Button("No");

        yesButton.setOnAction(e -> {
            dialog.close();
            primaryStage.close();
        });

        noButton.setOnAction(e -> dialog.close());

        dialogPane.getChildren().addAll(message, yesButton, noButton);
        Scene dialogScene = new Scene(dialogPane, 300, 150);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void styleActionButton(Button button) {
        button.setPrefWidth(200);
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

    private void styleSmallButton(Button button) {
        button.setPrefWidth(80);
        button.setFont(Font.font("Arial", 12));
        button.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        button.setOnMouseEntered(e ->
                button.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white;")
        );
        button.setOnMouseExited(e ->
                button.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;")
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
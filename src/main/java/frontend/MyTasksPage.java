package frontend;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class MyTasksPage extends Application {
    private StackPane contentPane;
    private String currentUserRole = "Employee"; // Can be "Manager" or "Employee"
    private String currentUser = "John Doe"; // Simulated logged-in user
    private List<Task> myTaskList; // Simulated task data for the current user

    // Simple Task class for demonstration
    private static class Task {
        String title;
        String description;
        String status;
        String priority;
        String deadline;
        String assignedTo;
        String remarks;

        Task(String title, String description, String status, String priority, String deadline, String assignedTo) {
            this.title = title;
            this.description = description;
            this.status = status;
            this.priority = priority;
            this.deadline = deadline;
            this.assignedTo = assignedTo;
            this.remarks = "";
        }
    }

    @Override
    public void start(Stage primaryStage) {
        // Initialize sample data for the current user
        initializeSampleTasks();

        // Main layout
        BorderPane root = new BorderPane();

        // Create side menu
        VBox sideMenu = createSideMenu(primaryStage);

        // Create content area
        contentPane = new StackPane();
        contentPane.setStyle("-fx-background-color: white;");
        updateMyTasksContent();

        // Set up the layout
        root.setLeft(sideMenu);
        root.setCenter(contentPane);

        // Create and configure scene
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Task Management System - My Tasks");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeSampleTasks() {
        myTaskList = new ArrayList<>();
        myTaskList.add(new Task("Project Report", "Complete quarterly report", "In Progress", "High", "2025-03-20", currentUser));
        myTaskList.add(new Task("Team Meeting", "Prepare agenda", "To-Do", "Medium", "2025-03-18", currentUser));
        myTaskList.add(new Task("Code Review", "Review pull requests", "Completed", "Low", "2025-03-15", currentUser));
    }

    private VBox createSideMenu(Stage primaryStage) {
        VBox sideMenu = new VBox(10);
        sideMenu.setPadding(new Insets(20));
        sideMenu.setPrefWidth(200);
        sideMenu.setStyle("-fx-background-color: #283034;");

        String[] menuItems = {"Home", "All Tasks", "My Tasks", "My Profile", "Logout"};

        for (String item : menuItems) {
            Button menuButton = new Button(item);
            styleMenuButton(menuButton);

            menuButton.setOnAction(e -> {
                if (item.equals("Logout")) {
                    showLogoutDialog(primaryStage);
                } else {
                    updateContent(item);
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

    private void updateContent(String page) {
        if (page.equals("My Tasks")) {
            updateMyTasksContent();
        } else {
            contentPane.getChildren().clear();
            Label contentLabel = new Label("Welcome to " + page + " Page");
            contentLabel.setFont(Font.font("Arial", 20));
            contentPane.getChildren().add(contentLabel);
        }
    }

    private void updateMyTasksContent() {
        contentPane.getChildren().clear();

        VBox myTasksContent = new VBox(20);
        myTasksContent.setPadding(new Insets(20));
        myTasksContent.setAlignment(Pos.CENTER);

        // Title
        Label titleLabel = new Label("My Tasks");
        titleLabel.setFont(Font.font("Arial", 24));

        // Filter controls
        HBox filterBox = new HBox(10);
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "To-Do", "In Progress", "Completed");
        statusFilter.setValue("All");
        Button filterButton = new Button("Filter");
        styleActionButton(filterButton);
        filterBox.getChildren().addAll(
                new Label("Status:"), statusFilter,
                filterButton
        );

        // Task list display
        ScrollPane scrollPane = new ScrollPane();
        VBox taskDisplay = new VBox(10);
        scrollPane.setContent(taskDisplay);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        // Initial task display
        displayTasks(taskDisplay, statusFilter.getValue());

        // Filter action
        filterButton.setOnAction(e ->
                displayTasks(taskDisplay, statusFilter.getValue())
        );

        myTasksContent.getChildren().addAll(titleLabel, filterBox, scrollPane);
        contentPane.getChildren().add(myTasksContent);
    }

    private void displayTasks(VBox taskDisplay, String statusFilter) {
        taskDisplay.getChildren().clear();

        for (Task task : myTaskList) {
            if (statusFilter.equals("All") || task.status.equals(statusFilter)) {
                HBox taskBox = new HBox(10);
                taskBox.setPadding(new Insets(10));
                taskBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1;");

                Label taskInfo = new Label(String.format("%s - %s (Priority: %s, Due: %s)",
                        task.title, task.status, task.priority, task.deadline));
                taskInfo.setFont(Font.font("Arial", 14));

                Button viewButton = new Button("View");
                styleSmallButton(viewButton);
                viewButton.setOnAction(e -> showTaskDetails(task));

                Button updateButton = new Button("Update Status");
                styleSmallButton(updateButton);
                updateButton.setOnAction(e -> showUpdateStatusDialog(task, taskDisplay));

                Button remarkButton = new Button("Add Remark");
                styleSmallButton(remarkButton);
                remarkButton.setOnAction(e -> showAddRemarkDialog(task, taskDisplay));

                taskBox.getChildren().addAll(taskInfo, viewButton, updateButton, remarkButton);
                taskDisplay.getChildren().add(taskBox);
            }
        }
    }

    private void styleActionButton(Button button) {
        button.setPrefWidth(100);
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
        button.setPrefWidth(100);
        button.setFont(Font.font("Arial", 12));
        button.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        button.setOnMouseEntered(e ->
                button.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white;")
        );
        button.setOnMouseExited(e ->
                button.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;")
        );
    }

    private void showTaskDetails(Task task) {
        Stage dialog = new Stage();
        VBox dialogPane = new VBox(10);
        dialogPane.setPadding(new Insets(10));

        dialogPane.getChildren().addAll(
                new Label("Task Details"),
                new Label("Title: " + task.title),
                new Label("Description: " + task.description),
                new Label("Status: " + task.status),
                new Label("Priority: " + task.priority),
                new Label("Deadline: " + task.deadline),
                new Label("Assigned To: " + task.assignedTo),
                new Label("Remarks: " + (task.remarks.isEmpty() ? "None" : task.remarks))
        );

        Scene dialogScene = new Scene(dialogPane, 300, 250);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void showUpdateStatusDialog(Task task, VBox taskDisplay) {
        Stage dialog = new Stage();
        VBox dialogPane = new VBox(10);
        dialogPane.setPadding(new Insets(10));

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("To-Do", "In Progress", "Completed");
        statusBox.setValue(task.status);

        Button updateButton = new Button("Update");
        updateButton.setOnAction(e -> {
            task.status = statusBox.getValue();
            updateMyTasksContent(); // Refresh display
            dialog.close();
        });

        dialogPane.getChildren().addAll(
                new Label("Update Task Status"),
                new Label("Status:"), statusBox,
                updateButton
        );

        Scene dialogScene = new Scene(dialogPane, 250, 150);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void showAddRemarkDialog(Task task, VBox taskDisplay) {
        Stage dialog = new Stage();
        VBox dialogPane = new VBox(10);
        dialogPane.setPadding(new Insets(10));

        TextArea remarkArea = new TextArea(task.remarks);
        remarkArea.setPromptText("Enter your remarks here");
        remarkArea.setPrefHeight(100);

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            task.remarks = remarkArea.getText();
            updateMyTasksContent(); // Refresh display
            dialog.close();
        });

        dialogPane.getChildren().addAll(
                new Label("Add/Update Remark"),
                remarkArea,
                saveButton
        );

        Scene dialogScene = new Scene(dialogPane, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();
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

    public static void main(String[] args) {
        launch(args);
    }
}
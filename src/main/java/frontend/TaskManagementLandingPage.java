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
import javafx.stage.Stage;
import java.util.List;

public class TaskManagementLandingPage extends Application {
    private Stage primaryStage;
    private StackPane contentPane;
    private String currentUser;

    public TaskManagementLandingPage(String username) {
        this.currentUser = username;
    }

    public TaskManagementLandingPage() {
        this.currentUser = "Default User";
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        BorderPane root = new BorderPane();
        root.setLeft(createSideMenu());
        contentPane = new StackPane();
        contentPane.setStyle("-fx-background-color: white;");
        root.setCenter(contentPane);

        updateContent("Home");

        Scene mainScene = new Scene(root, 800, 600);
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Task Management System");
        primaryStage.show();
    }

    private VBox createSideMenu() {
        VBox sideMenu = new VBox(10);
        sideMenu.setPadding(new Insets(20));
        sideMenu.setPrefWidth(200);
        sideMenu.setStyle("-fx-background-color: #283034;");

        String[] menuItems = {"Home", "My Tasks", "My Profile", "Logout"};

        for (String item : menuItems) {
            Button menuButton = new Button(item);
            styleMenuButton(menuButton);

            menuButton.setOnAction(e -> {
                if (item.equals("Logout")) {
                    showLogoutDialog();
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
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #303f9f; -fx-text-fill: white;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #3f51b5; -fx-text-fill: white;"));
    }

    private void styleActionButton(Button button) {
        button.setPrefWidth(150);
        button.setPrefHeight(40);
        button.setFont(Font.font("Arial", 14));
        button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #45a049; -fx-text-fill: white;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"));
    }

    private void updateContent(String page) {
        contentPane.getChildren().clear();
        switch (page) {
            case "Home":
                contentPane.getChildren().add(createHomeContent());
                break;
            case "My Tasks":
                contentPane.getChildren().add(createMyTasksContent());
                break;
            case "My Profile":
                contentPane.getChildren().add(createMyProfileContent());
                break;
        }
    }

    private VBox createHomeContent() {
        VBox homeContent = new VBox(20);
        homeContent.setPadding(new Insets(20));
        homeContent.setAlignment(Pos.CENTER);

        Label welcomeLabel = new Label("Welcome, " + currentUser);
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        welcomeLabel.setTextFill(Color.DARKBLUE);

        Label summaryLabel = new Label("Here’s a quick overview of your tasks:");
        summaryLabel.setFont(Font.font("Arial", 16));
        summaryLabel.setTextFill(Color.DARKGRAY);

        VBox statsBox = new VBox(10);
        statsBox.setAlignment(Pos.CENTER);

        // Get the tasks assigned to the current user
        List<TaskData.Task> userTasks = TaskData.getTaskList().stream()
                .filter(t -> t.getAssignedTo().equals(currentUser))
                .toList();

        long totalTasksCount = userTasks.size();
        long pendingTasksCount = userTasks.stream().filter(t -> !t.getStatus().equals("Completed")).count();
        long completedTasksCount = userTasks.stream().filter(t -> t.getStatus().equals("Completed")).count();

        Label totalTasks = new Label("Total Tasks: " + totalTasksCount);
        Label pendingTasks = new Label("Pending Tasks: " + pendingTasksCount);
        Label completedTasks = new Label("Completed Tasks: " + completedTasksCount);

        totalTasks.setFont(Font.font("Arial", 14));
        totalTasks.setTextFill(Color.DARKGRAY);
        pendingTasks.setFont(Font.font("Arial", 14));
        pendingTasks.setTextFill(Color.DARKGRAY);
        completedTasks.setFont(Font.font("Arial", 14));
        completedTasks.setTextFill(Color.DARKGRAY);
        statsBox.getChildren().addAll(totalTasks, pendingTasks, completedTasks);

        Button viewTasksButton = new Button("View My Tasks");
        styleActionButton(viewTasksButton);
        viewTasksButton.setOnAction(e -> updateContent("My Tasks"));

        homeContent.getChildren().addAll(welcomeLabel, summaryLabel, statsBox, viewTasksButton);
        return homeContent;
    }


    private VBox createMyTasksContent() {
        VBox tasksContent = new VBox(20);
        tasksContent.setPadding(new Insets(20));
        tasksContent.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("My Tasks");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.DARKBLUE);

        ScrollPane scrollPane = new ScrollPane();
        VBox taskListDisplay = new VBox(10);
        scrollPane.setContent(taskListDisplay);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        // Debugging: Print currentUser value
        System.out.println("Current User: " + currentUser);

        List<TaskData.Task> userTasks = TaskData.getTaskList().stream()
                .filter(task -> {
                    boolean assigned = task.getAssignedTo().equals(currentUser);
                    System.out.println("Task: " + task.getTitle() + ", Assigned To: " + task.getAssignedTo() + ", Matches User: " + assigned);  // Debugging
                    return assigned;
                })
                .toList();

        for (TaskData.Task task : userTasks) {
            HBox taskBox = new HBox(10);
            taskBox.setPadding(new Insets(10));
            taskBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1;");

            Label taskLabel = new Label(task.getTitle() + " - " + task.getStatus() + " - Due: " + task.getDeadline());
            taskLabel.setFont(Font.font("Arial", 14));

            Button viewButton = new Button("View");
            viewButton.setPrefWidth(80);
            viewButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            viewButton.setOnAction(e -> showAlert("Task Details",
                    "Title: " + task.getTitle() + "\nDescription: " + task.getDescription() + "\nStatus: " + task.getStatus()));

            taskBox.getChildren().addAll(taskLabel, viewButton);
            taskListDisplay.getChildren().add(taskBox);
        }

        tasksContent.getChildren().addAll(titleLabel, scrollPane);
        return tasksContent;
    }


    private VBox createMyProfileContent() {
        VBox profileContent = new VBox(20);
        profileContent.setPadding(new Insets(20));
        profileContent.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("My Profile");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.DARKBLUE);

        VBox detailsBox = new VBox(15);
        detailsBox.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label("Name: " + currentUser);
        Label roleLabel = new Label("Role: Team Member");
        nameLabel.setFont(Font.font("Arial", 16));
        nameLabel.setTextFill(Color.DARKGRAY);
        roleLabel.setFont(Font.font("Arial", 16));
        roleLabel.setTextFill(Color.DARKGRAY);
        detailsBox.getChildren().addAll(nameLabel, roleLabel);

        Button editButton = new Button("Edit Profile");
        styleActionButton(editButton);
        editButton.setOnAction(e -> showAlert("Edit Profile", "Edit profile functionality to be implemented."));

        profileContent.getChildren().addAll(titleLabel, detailsBox, editButton);
        return profileContent;
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

    private void showLogoutDialog() {
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
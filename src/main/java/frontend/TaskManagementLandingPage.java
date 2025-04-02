package frontend;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskManagementLandingPage extends Application {
    private String currentUsername;
    private StackPane contentPane;
    private VBox notificationPane;

    public TaskManagementLandingPage() {
        this.currentUsername = "DefaultUser";
    }

    public TaskManagementLandingPage(String username) {
        this.currentUsername = username;
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setLeft(createSideMenu(primaryStage));
        contentPane = new StackPane();

        notificationPane = new VBox(5);
        notificationPane.setPadding(new Insets(10));
        notificationPane.setStyle("-fx-background-color: #f0f0f0;");
        updateNotifications();

        VBox mainContent = new VBox(10);
        mainContent.getChildren().addAll(notificationPane, contentPane);
        root.setCenter(mainContent);

        updateContent("Home");

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("TMS - Employee");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createSideMenu(Stage primaryStage) {
        VBox sideMenu = new VBox(10);
        sideMenu.setPadding(new Insets(20));
        sideMenu.setPrefWidth(200);
        sideMenu.setStyle("-fx-background-color: #283034;");

        String[] menuItems = {"Home", "My Tasks", "My Profile", "Logout"};
        for (String item : menuItems) {
            Button button = new Button(item);
            styleButton(button);
            button.setOnAction(e -> {
                if (item.equals("Logout")) {
                    primaryStage.close();
                    new TaskManagerLogin().start(new Stage());
                } else if (item.equals("My Tasks")) {
                    new MyTasksPage(currentUsername).start(new Stage());
                } else {
                    updateContent(item);
                }
            });
            sideMenu.getChildren().add(button);
        }
        return sideMenu;
    }

    private void updateContent(String page) {
        contentPane.getChildren().clear();
        if (page.equals("Home")) {
            VBox homeContent = new VBox(20);
            homeContent.setAlignment(Pos.CENTER);
            homeContent.setPadding(new Insets(20));

            Label welcomeLabel = new Label("Welcome, " + currentUsername);
            welcomeLabel.setFont(Font.font("Arial", 24));

            // Fetch tasks for the current user
            List<Task> tasks = TaskData.getTasksByUser(currentUsername);
            int total = tasks.size();
            int pending = (int) tasks.stream().filter(t -> !t.getStatus().equals("Completed")).count();
            int completed = total - pending;

            // Text-based statistics
            HBox statsBox = new HBox(20);
            statsBox.setAlignment(Pos.CENTER);
            statsBox.getChildren().addAll(
                    new Label("My Tasks: " + total),
                    new Label("Pending: " + pending),
                    new Label("Completed: " + completed)
            );

            // Pie Chart: Task Status Distribution
            PieChart statusChart = new PieChart();
            statusChart.setTitle("My Task Status Distribution");
            Map<String, Long> statusCounts = tasks.stream()
                    .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));
            statusCounts.forEach((status, count) ->
                    statusChart.getData().add(new PieChart.Data(status, count)));
            statusChart.setPrefWidth(400);
            statusChart.setPrefHeight(300);

            // Bar Chart: Task Priority Distribution
            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel("Priority");
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Number of Tasks");
            BarChart<String, Number> priorityChart = new BarChart<>(xAxis, yAxis);
            priorityChart.setTitle("My Task Priority Distribution");
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Tasks");
            Map<String, Long> priorityCounts = tasks.stream()
                    .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));
            priorityCounts.forEach((priority, count) ->
                    series.getData().add(new XYChart.Data<>(priority, count)));
            priorityChart.getData().add(series);
            priorityChart.setPrefWidth(400);
            priorityChart.setPrefHeight(300);

            // Layout for charts
            HBox chartsBox = new HBox(20);
            chartsBox.setAlignment(Pos.CENTER);
            chartsBox.getChildren().addAll(statusChart, priorityChart);

            homeContent.getChildren().addAll(welcomeLabel, statsBox, chartsBox);
            contentPane.getChildren().add(homeContent);
        } else if (page.equals("My Profile")) {
            contentPane.getChildren().add(new MyProfilePage(currentUsername, "Team Member"));
        }
    }

    private void updateNotifications() {
        notificationPane.getChildren().clear();
        List<Notification> notifications = TaskData.getNotifications(currentUsername);
        if (notifications.isEmpty()) {
            notificationPane.getChildren().add(new Label("No new notifications."));
            return;
        }

        for (Notification notification : notifications) {
            Label notificationLabel = new Label(String.format("%s (Received: %s)",
                    notification.getMessage(), notification.getCreatedAt()));
            notificationLabel.setStyle("-fx-text-fill: #d32f2f;");
            notificationPane.getChildren().add(notificationLabel);
        }

        Button markAsReadButton = new Button("Mark as Read");
        styleButton(markAsReadButton);
        markAsReadButton.setOnAction(e -> {
            try {
                TaskData.markNotificationsAsRead(currentUsername);
                updateNotifications();
            } catch (SQLException ex) {
                showAlert("Error", "Failed to mark notifications as read: " + ex.getMessage());
            }
        });
        notificationPane.getChildren().add(markAsReadButton);
    }

    private void styleButton(Button button) {
        button.setPrefWidth(160);
        button.setStyle("-fx-background-color: #3f51b5; -fx-text-fill: white;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #303f9f; -fx-text-fill: white;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #3f51b5; -fx-text-fill: white;"));
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
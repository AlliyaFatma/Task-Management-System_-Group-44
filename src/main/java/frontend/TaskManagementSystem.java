package frontend;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskManagementSystem extends Application {
    private String currentUsername;
    private StackPane contentPane;
    private VBox notificationPane;

    public TaskManagementSystem(String username) {
        this.currentUsername = username;
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1C2526;");

        root.setLeft(createSideMenu(primaryStage));

        contentPane = new StackPane();
        contentPane.setStyle("-fx-background-color: #1C2526;");

        notificationPane = new VBox(5);
        notificationPane.setPadding(new Insets(10));
        notificationPane.setStyle("-fx-background-color: #283034; -fx-border-color: #3A4A4D; -fx-border-width: 1;");

        VBox mainContent = new VBox(10);
        mainContent.getChildren().addAll(notificationPane, contentPane);
        root.setCenter(mainContent);

        updateContent("Home");

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("TMS - Manager");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createSideMenu(Stage primaryStage) {
        VBox sideMenu = new VBox(10);
        sideMenu.setPadding(new Insets(20));
        sideMenu.setPrefWidth(200);
        sideMenu.setStyle("-fx-background-color: #283034;");

        String[] menuItems = {"Home", "All Tasks", "My Profile", "Logout"};
        for (String item : menuItems) {
            Button button = new Button(item);
            styleMenuButton(button, item.equals("Home"));
            button.setOnAction(e -> {
                if (item.equals("Logout")) {
                    primaryStage.close();
                    new TaskManagerLogin().start(new Stage());
                } else if (item.equals("All Tasks")) {
                    new AllTasksPage(currentUsername).start(new Stage());
                } else {
                    updateContent(item);
                    for (javafx.scene.Node node : sideMenu.getChildren()) {
                        if (node instanceof Button) {
                            styleMenuButton((Button) node, node == button);
                        }
                    }
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
            styleLabel(welcomeLabel, true);

            List<Task> tasks;
            tasks = TaskData.getAllTasks();
            int total = tasks.size();
            int pending = (int) tasks.stream().filter(t -> !t.getStatus().equals("Completed")).count();
            int completed = total - pending;

            HBox statsBox = new HBox(20);
            statsBox.setAlignment(Pos.CENTER);
            statsBox.getChildren().addAll(
                    new Label("Total Tasks: " + total),
                    new Label("Pending: " + pending),
                    new Label("Completed: " + completed)
            );
            statsBox.getChildren().forEach(node -> styleLabel((Label) node, false));

            PieChart statusChart = new PieChart();
            statusChart.setTitle("Task Status Distribution");
            styleChart(statusChart);
            Map<String, Long> statusCounts = tasks.stream()
                    .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));
            statusCounts.forEach((status, count) ->
                    statusChart.getData().add(new PieChart.Data(status, count)));
            statusChart.setPrefWidth(400);
            statusChart.setPrefHeight(300);

            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel("Priority");
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Number of Tasks");
            BarChart<String, Number> priorityChart = new BarChart<>(xAxis, yAxis);
            priorityChart.setTitle("Task Priority Distribution");
            styleChart(priorityChart);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Tasks");
            Map<String, Long> priorityCounts = tasks.stream()
                    .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));
            priorityCounts.forEach((priority, count) ->
                    series.getData().add(new XYChart.Data<>(priority, count)));
            priorityChart.getData().add(series);
            priorityChart.setPrefWidth(400);
            priorityChart.setPrefHeight(300);

            HBox chartsBox = new HBox(20);
            chartsBox.setAlignment(Pos.CENTER);
            chartsBox.getChildren().addAll(statusChart, priorityChart);

            homeContent.getChildren().addAll(welcomeLabel, statsBox, chartsBox);
            contentPane.getChildren().add(homeContent);
        } else if (page.equals("My Profile")) {
            contentPane.getChildren().add(new MyProfilePage(currentUsername, "Manager"));
        }
    }

    private void updateNotifications() {
        notificationPane.getChildren().clear();
        List<Notification> notifications;
        notifications = TaskData.getNotifications(currentUsername);

        if (notifications.isEmpty()) {
            Label noNotificationsLabel = new Label("No new notifications.");
            styleLabel(noNotificationsLabel, false);
            notificationPane.getChildren().add(noNotificationsLabel);
            return;
        }

        for (Notification notification : notifications) {
            Label notificationLabel = new Label(String.format("%s (Received: %s)",
                    notification.getMessage(), notification.getCreatedAt()));
            notificationLabel.setStyle("-fx-text-fill: #FF6F61;");
            notificationPane.getChildren().add(notificationLabel);
        }

        Button markAsReadButton = new Button("Mark as Read");
        stylePrimaryButton(markAsReadButton);
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

    // Styling methods
    private void styleLabel(Label label, boolean isTitle) {
        label.setStyle("-fx-text-fill: #FFFFFF; -fx-font-family: 'Arial';" + (isTitle ? "-fx-font-size: 24;" : "-fx-font-size: 14;"));
    }

    private void styleMenuButton(Button button, boolean isActive) {
        button.setPrefWidth(160);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle("-fx-background-color: " + (isActive ? "#3A4A4D" : "#283034") + "; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-color: transparent;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #4A5A5D; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-color: transparent;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + (isActive ? "#3A4A4D" : "#283034") + "; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-color: transparent;"));
    }

    private void stylePrimaryButton(Button button) {
        button.setPrefWidth(160);
        button.setStyle("-fx-background-color: #3A4A4D; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-radius: 5; -fx-background-radius: 5;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #4A5A5D; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-radius: 5; -fx-background-radius: 5;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #3A4A4D; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-radius: 5; -fx-background-radius: 5;"));
    }

    private void styleChart(Chart chart) {
        chart.setStyle("-fx-background-color: #283034; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial';");
        chart.lookupAll(".chart-title").forEach(node ->
                node.setStyle("-fx-text-fill: #FFFFFF; -fx-font-family: 'Arial';"));
        chart.lookupAll(".axis-label").forEach(node ->
                node.setStyle("-fx-text-fill: #FFFFFF; -fx-font-family: 'Arial';"));
        chart.lookupAll(".chart-legend").forEach(node ->
                node.setStyle("-fx-background-color: #283034; -fx-text-fill: #FFFFFF;"));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color: #1C2526; -fx-font-family: 'Arial';");
        alert.getDialogPane().lookup(".content").setStyle("-fx-text-fill: #FFFFFF;");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
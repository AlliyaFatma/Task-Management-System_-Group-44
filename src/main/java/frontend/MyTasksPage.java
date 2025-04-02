package frontend;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.Tooltip;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MyTasksPage extends Application {
    private String currentUsername;
    private VBox taskDisplay;
    private ComboBox<String> statusFilter;
    private TextField fromDateField;
    private TextField toDateField;

    public MyTasksPage(String username) {
        this.currentUsername = username;
    }

    @Override
    public void start(Stage primaryStage) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("My Tasks");
        titleLabel.setFont(Font.font("Arial", 24));

        HBox filterBox = new HBox(10);
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "To-Do", "In Progress", "Completed");
        statusFilter.setValue("All");
        fromDateField = new TextField();
        fromDateField.setPromptText("From (YYYY-MM-DD)");
        toDateField = new TextField();
        toDateField.setPromptText("To (YYYY-MM-DD)");
        Button filterButton = new Button("Filter");
        styleButton(filterButton);
        filterButton.setOnAction(e -> displayTasks(statusFilter.getValue(), fromDateField.getText(), toDateField.getText()));
        Button clearButton = new Button("Clear");
        styleButton(clearButton);
        clearButton.setOnAction(e -> {
            statusFilter.setValue("All");
            fromDateField.clear();
            toDateField.clear();
            displayTasks("All", "", "");
        });
        filterBox.getChildren().addAll(
                new Label("Status:"), statusFilter,
                new Label("From Date:"), fromDateField,
                new Label("To Date:"), toDateField,
                filterButton, clearButton
        );

        ScrollPane scrollPane = new ScrollPane();
        taskDisplay = new VBox(10);
        scrollPane.setContent(taskDisplay);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        displayTasks("All", "", "");
        showTaskNotifications();

        content.getChildren().addAll(titleLabel, filterBox, scrollPane);

        Scene scene = new Scene(content, 800, 600);
        primaryStage.setTitle("TMS - My Tasks");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void displayTasks(String statusFilter, String fromDate, String toDate) {
        taskDisplay.getChildren().clear();
        List<Task> tasks = TaskData.getTasksByUser(currentUsername);
        if (tasks.isEmpty()) {
            taskDisplay.getChildren().add(new Label("No tasks available."));
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate from = fromDate.isEmpty() ? null : LocalDate.parse(fromDate, formatter);
        LocalDate to = toDate.isEmpty() ? null : LocalDate.parse(toDate, formatter);

        for (Task task : tasks) {
            boolean matchesStatus = statusFilter.equals("All") || task.getStatus().equals(statusFilter);
            boolean matchesDate = true;
            if (task.getDeadline() != null && !task.getDeadline().isEmpty()) {
                try {
                    LocalDate deadline = LocalDate.parse(task.getDeadline(), formatter);
                    if (from != null && deadline.isBefore(from)) matchesDate = false;
                    if (to != null && deadline.isAfter(to)) matchesDate = false;
                } catch (Exception e) {
                    matchesDate = false;
                }
            }

            if (matchesStatus && matchesDate) {
                VBox taskCard = new VBox(5);
                taskCard.setPadding(new Insets(10));
                // Color-code based on priority
                String cardColor = task.getPriority().equals("High") ? "#ffcccc" :
                        task.getPriority().equals("Medium") ? "#fff4cc" : "#ccffcc";
                taskCard.setStyle("-fx-background-color: " + cardColor + "; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-radius: 5;");
                // Hover effect
                taskCard.setOnMouseEntered(e -> taskCard.setStyle("-fx-background-color: " + cardColor + "; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
                taskCard.setOnMouseExited(e -> taskCard.setStyle("-fx-background-color: " + cardColor + "; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-radius: 5;"));

                HBox taskInfoBox = new HBox(5);
                taskInfoBox.setAlignment(Pos.CENTER_LEFT);
                // Add flame icon for High priority
                if (task.getPriority().equals("High")) {
                    ImageView flameIcon = new ImageView(new Image("file:flame_icon.png", 16, 16, true, true));
                    flameIcon.setVisible(true);
                    taskInfoBox.getChildren().add(flameIcon);
                }
                String deadline = task.getDeadline() != null ? task.getDeadline() : "N/A";
                Label taskInfo = new Label(String.format("%s - %s (Priority: %s, Due: %s)",
                        task.getTitle(), task.getStatus(), task.getPriority(), deadline));
                taskInfoBox.getChildren().add(taskInfo);

                if (deadline != null && !deadline.equals("N/A")) {
                    try {
                        LocalDate dueDate = LocalDate.parse(deadline, formatter);
                        LocalDate today = LocalDate.now();
                        long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(today, dueDate);
                        if (daysUntilDue < 0) {
                            taskInfo.setStyle("-fx-text-fill: red;");
                        } else if (daysUntilDue <= 2) {
                            taskInfo.setStyle("-fx-text-fill: orange;");
                        }
                    } catch (Exception e) {
                        // Invalid date format, skip highlighting
                    }
                }

                // Progress bar based on the progress field
                ProgressBar progressBar = new ProgressBar();
                progressBar.setProgress(task.getProgress() / 100.0); // Progress is stored as 0-100
                progressBar.setPrefWidth(200);

                HBox buttonBox = new HBox(10);
                Button updateButton = new Button("Update Status");
                styleSmallButton(updateButton);
                updateButton.setTooltip(new Tooltip("Change the task status and progress"));
                updateButton.setOnAction(e -> showUpdateStatusDialog(task));

                Button markCompleteButton = new Button("Mark as Completed");
                styleSmallButton(markCompleteButton);
                markCompleteButton.setTooltip(new Tooltip("Quickly mark this task as completed"));
                markCompleteButton.setOnAction(e -> {
                    if (!task.getStatus().equals("Completed")) {
                        task.setStatus("Completed");
                        task.setProgress(100); // Set progress to 100% when marking as completed
                        try {
                            TaskData.updateTask(task, currentUsername);
                            displayTasks(statusFilter, fromDate, toDate);
                        } catch (SQLException ex) {
                            showAlert("Error", "Failed to update task: " + ex.getMessage());
                        }
                    }
                });
                if (task.getStatus().equals("Completed")) {
                    markCompleteButton.setDisable(true);
                }

                Button remarkButton = new Button("Add Remark");
                styleSmallButton(remarkButton);
                remarkButton.setTooltip(new Tooltip("Add or update remarks for this task"));
                remarkButton.setOnAction(e -> showAddRemarkDialog(task));

                Button historyButton = new Button("View History");
                styleSmallButton(historyButton);
                historyButton.setTooltip(new Tooltip("View the history of changes for this task"));
                historyButton.setOnAction(e -> showTaskHistoryDialog(task));

                buttonBox.getChildren().addAll(updateButton, markCompleteButton, remarkButton, historyButton);
                taskCard.getChildren().addAll(taskInfoBox, progressBar, buttonBox);
                taskDisplay.getChildren().add(taskCard);
            }
        }
    }

    private void showTaskNotifications() {
        List<Task> tasks = TaskData.getTasksByUser(currentUsername);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        StringBuilder notificationMessage = new StringBuilder();

        for (Task task : tasks) {
            if (task.getDeadline() != null && !task.getDeadline().isEmpty()) {
                try {
                    LocalDate dueDate = LocalDate.parse(task.getDeadline(), formatter);
                    long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(today, dueDate);
                    if (daysUntilDue < 0) {
                        notificationMessage.append(String.format("Task '%s' is overdue (Due: %s)!\n", task.getTitle(), task.getDeadline()));
                    } else if (daysUntilDue <= 2) {
                        notificationMessage.append(String.format("Task '%s' is due soon (Due: %s)!\n", task.getTitle(), task.getDeadline()));
                    }
                } catch (Exception e) {
                    // Invalid date format, skip
                }
            }
        }

        if (notificationMessage.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Task Reminders");
            alert.setHeaderText("You have upcoming or overdue tasks!");
            alert.setContentText(notificationMessage.toString());
            alert.showAndWait();
        }
    }

    private void showUpdateStatusDialog(Task task) {
        Stage dialog = new Stage();
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("To-Do", "In Progress", "Completed");
        statusBox.setValue(task.getStatus());

        Slider progressSlider = new Slider(0, 100, task.getProgress());
        progressSlider.setShowTickLabels(true);
        progressSlider.setShowTickMarks(true);
        progressSlider.setMajorTickUnit(25);
        progressSlider.setMinorTickCount(5);
        progressSlider.setSnapToTicks(true);
        Label progressLabel = new Label("Progress: " + (int) progressSlider.getValue() + "%");
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            progressLabel.setText("Progress: " + newVal.intValue() + "%");
        });

        Button saveButton = new Button("Save");
        styleButton(saveButton);
        saveButton.setOnAction(e -> {
            task.setStatus(statusBox.getValue());
            task.setProgress((int) progressSlider.getValue());
            try {
                TaskData.updateTask(task, currentUsername);
                displayTasks(statusFilter.getValue(), fromDateField.getText(), toDateField.getText());
                dialog.close();
            } catch (SQLException ex) {
                showAlert("Error", "Failed to update task: " + ex.getMessage());
            }
        });

        pane.getChildren().addAll(
                new Label("Update Status:"), statusBox,
                new Label("Set Progress:"), progressSlider, progressLabel,
                saveButton
        );
        dialog.setScene(new Scene(pane, 300, 200));
        dialog.show();
    }

    private void showAddRemarkDialog(Task task) {
        Stage dialog = new Stage();
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));

        TextArea remarkArea = new TextArea(task.getRemarks());
        remarkArea.setPromptText("Enter your remarks here");
        remarkArea.setPrefHeight(100);

        Button saveButton = new Button("Save");
        styleButton(saveButton);
        saveButton.setOnAction(e -> {
            task.setRemarks(remarkArea.getText());
            try {
                TaskData.updateTask(task, currentUsername);
                displayTasks(statusFilter.getValue(), fromDateField.getText(), toDateField.getText());
                dialog.close();
                showAlert("Success", "Remark added successfully.");
            } catch (SQLException ex) {
                showAlert("Error", "Failed to update remark: " + ex.getMessage());
            }
        });

        pane.getChildren().addAll(new Label("Add/Update Remark:"), remarkArea, saveButton);
        dialog.setScene(new Scene(pane, 300, 200));
        dialog.show();
    }

    private void showTaskHistoryDialog(Task task) {
        Stage dialog = new Stage();
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));

        Label titleLabel = new Label("History for Task: " + task.getTitle());
        titleLabel.setFont(Font.font("Arial", 16));

        List<String> history = TaskData.getTaskHistory(task.getId());
        VBox historyBox = new VBox(5);
        if (history.isEmpty()) {
            historyBox.getChildren().add(new Label("No history available."));
        } else {
            for (String entry : history) {
                historyBox.getChildren().add(new Label(entry));
            }
        }

        ScrollPane scrollPane = new ScrollPane(historyBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);

        Button closeButton = new Button("Close");
        styleButton(closeButton);
        closeButton.setOnAction(e -> dialog.close());

        pane.getChildren().addAll(titleLabel, scrollPane, closeButton);
        dialog.setScene(new Scene(pane, 400, 300));
        dialog.setTitle("Task History");
        dialog.show();
    }

    private void styleButton(Button button) {
        button.setPrefWidth(100);
        button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #45a049; -fx-text-fill: white;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"));
    }

    private void styleSmallButton(Button button) {
        button.setPrefWidth(100);
        button.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;"));
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
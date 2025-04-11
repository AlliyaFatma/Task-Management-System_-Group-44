package frontend;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
        content.setStyle("-fx-background-color: #1C2526;");

        Label titleLabel = new Label("My Tasks");
        styleLabel(titleLabel, true);

        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER);
        Label statusLabel = new Label("Status:");
        styleLabel(statusLabel, false);
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "To-Do", "In Progress", "Completed");
        statusFilter.setValue("All");
        styleComboBox(statusFilter);

        Label fromDateLabel = new Label("From Date:");
        styleLabel(fromDateLabel, false);
        fromDateField = new TextField();
        fromDateField.setPromptText("YYYY-MM-DD");
        styleTextField(fromDateField);

        Label toDateLabel = new Label("To Date:");
        styleLabel(toDateLabel, false);
        toDateField = new TextField();
        toDateField.setPromptText("YYYY-MM-DD");
        styleTextField(toDateField);

        Button filterButton = new Button("Filter");
        stylePrimaryButton(filterButton);
        filterButton.setOnAction(e -> displayTasks(statusFilter.getValue(), fromDateField.getText(), toDateField.getText()));

        Button clearButton = new Button("Clear");
        styleSecondaryButton(clearButton);
        clearButton.setOnAction(e -> {
            statusFilter.setValue("All");
            fromDateField.clear();
            toDateField.clear();
            displayTasks("All", "", "");
        });

        filterBox.getChildren().addAll(
                statusLabel, statusFilter,
                fromDateLabel, fromDateField,
                toDateLabel, toDateField,
                filterButton, clearButton
        );

        ScrollPane scrollPane = new ScrollPane();
        taskDisplay = new VBox(10);
        scrollPane.setContent(taskDisplay);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background-color: #1C2526; -fx-border-color: #3A4A4D;");

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
        List<Task> tasks;
        tasks = TaskData.getTasksByUser(currentUsername);

        if (tasks.isEmpty()) {
            Label noTasksLabel = new Label("No tasks available.");
            styleLabel(noTasksLabel, false);
            taskDisplay.getChildren().add(noTasksLabel);
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
                String cardColor = task.getPriority().equals("High") ? "#FF6F61" :
                        task.getPriority().equals("Medium") ? "#FFB347" : "#4CAF50";
                taskCard.setStyle("-fx-background-color: #283034; -fx-border-color: " + cardColor + "; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-width: 2;");
                taskCard.setOnMouseEntered(e -> taskCard.setStyle("-fx-background-color: #3A4A4D; -fx-border-color: " + cardColor + "; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-width: 2; -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
                taskCard.setOnMouseExited(e -> taskCard.setStyle("-fx-background-color: #283034; -fx-border-color: " + cardColor + "; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-width: 2;"));

                HBox taskInfoBox = new HBox(5);
                taskInfoBox.setAlignment(Pos.CENTER_LEFT);
                if (task.getPriority().equals("High")) {
                    ImageView flameIcon;
                    try {
                        flameIcon = new ImageView(new Image("file:flame_icon.png", 16, 16, true, true));
                    } catch (Exception e) {
                        flameIcon = new ImageView(); // Fallback if image is missing
                    }
                    flameIcon.setVisible(true);
                    taskInfoBox.getChildren().add(flameIcon);
                }
                String deadline = task.getDeadline() != null ? task.getDeadline() : "N/A";
                Label taskInfo = new Label(String.format("%s - %s (Priority: %s, Due: %s)",
                        task.getTitle(), task.getStatus(), task.getPriority(), deadline));
                styleLabel(taskInfo, false);
                taskInfoBox.getChildren().add(taskInfo);

                if (deadline != null && !deadline.equals("N/A")) {
                    try {
                        LocalDate dueDate = LocalDate.parse(deadline, formatter);
                        LocalDate today = LocalDate.now();
                        long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);
                        if (daysUntilDue < 0) {
                            taskInfo.setStyle("-fx-text-fill: #FF6F61;");
                        } else if (daysUntilDue <= 2) {
                            taskInfo.setStyle("-fx-text-fill: #FFB347;");
                        }
                    } catch (Exception e) {
                        // Invalid date format, skip highlighting
                    }
                }

                ProgressBar progressBar = new ProgressBar();
                progressBar.setProgress(task.getProgress() / 100.0);
                progressBar.setPrefWidth(200);
                progressBar.setStyle("-fx-accent: #4CAF50;");

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
                        task.setProgress(100);
                        try {
                            TaskData.updateTask(task, currentUsername);
                            displayTasks(String.valueOf(statusFilter.getClass()), fromDateField.getText(), toDateField.getText());
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
        List<Task> tasks;
        tasks = TaskData.getTasksByUser(currentUsername);

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
            showAlert("Task Reminders", notificationMessage.toString());
        }
    }

    private void showUpdateStatusDialog(Task task) {
        Stage dialog = new Stage();
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));
        pane.setStyle("-fx-background-color: #1C2526;");

        Label statusLabel = new Label("Update Status:");
        styleLabel(statusLabel, false);
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("To-Do", "In Progress", "Completed");
        statusBox.setValue(task.getStatus());
        styleComboBox(statusBox);

        Label progressLabel = new Label("Set Progress:");
        styleLabel(progressLabel, false);
        Slider progressSlider = new Slider(0, 100, task.getProgress());
        progressSlider.setShowTickLabels(true);
        progressSlider.setShowTickMarks(true);
        progressSlider.setMajorTickUnit(25);
        progressSlider.setMinorTickCount(5);
        progressSlider.setSnapToTicks(true);
        Label progressValueLabel = new Label("Progress: " + (int) progressSlider.getValue() + "%");
        styleLabel(progressValueLabel, false);
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            progressValueLabel.setText("Progress: " + newVal.intValue() + "%");
        });

        Button saveButton = new Button("Save");
        stylePrimaryButton(saveButton);
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

        pane.getChildren().addAll(statusLabel, statusBox, progressLabel, progressSlider, progressValueLabel, saveButton);
        dialog.setScene(new Scene(pane, 300, 200));
        dialog.show();
    }

    private void showAddRemarkDialog(Task task) {
        Stage dialog = new Stage();
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));
        pane.setStyle("-fx-background-color: #1C2526;");

        Label remarkLabel = new Label("Add/Update Remark:");
        styleLabel(remarkLabel, false);
        TextArea remarkArea = new TextArea(task.getRemarks() != null ? task.getRemarks() : "");
        remarkArea.setPromptText("Enter your remarks here");
        remarkArea.setPrefHeight(100);
        styleTextArea(remarkArea);

        Button saveButton = new Button("Save");
        stylePrimaryButton(saveButton);
        saveButton.setOnAction(e -> {
            String newRemark = remarkArea.getText();
            task.setRemarks(newRemark);
            try {
                TaskData.updateTask(task, currentUsername);
                TaskData.logTaskAction(task.getId(), currentUsername, "Added/Updated remark: " + newRemark);
                displayTasks(statusFilter.getValue(), fromDateField.getText(), toDateField.getText());
                dialog.close();
                showAlert("Success", "Remark added successfully.");
            } catch (SQLException ex) {
                showAlert("Error", "Failed to update remark: " + ex.getMessage());
            }
        });

        pane.getChildren().addAll(remarkLabel, remarkArea, saveButton);
        dialog.setScene(new Scene(pane, 300, 200));
        dialog.show();
    }

    private void showTaskHistoryDialog(Task task) {
        Stage dialog = new Stage();
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));
        pane.setStyle("-fx-background-color: #1C2526;");

        Label titleLabel = new Label("History for Task: " + task.getTitle());
        styleLabel(titleLabel, true);

        List<String> history;
        history = TaskData.getTaskHistory(task.getId());

        VBox historyBox = new VBox(5);
        historyBox.setStyle("-fx-background-color: #283034; -fx-border-color: #3A4A4D; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
        if (history.isEmpty()) {
            Label noHistoryLabel = new Label("No history available.");
            styleLabel(noHistoryLabel, false);
            historyBox.getChildren().add(noHistoryLabel);
        } else {
            for (String entry : history) {
                Label historyLabel = new Label(entry);
                styleLabel(historyLabel, false);
                historyBox.getChildren().add(historyLabel);
            }
        }

        ScrollPane scrollPane = new ScrollPane(historyBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);
        scrollPane.setStyle("-fx-background-color: #1C2526; -fx-border-color: #3A4A4D;");

        Button closeButton = new Button("Close");
        stylePrimaryButton(closeButton);
        closeButton.setOnAction(e -> dialog.close());

        pane.getChildren().addAll(titleLabel, scrollPane, closeButton);
        dialog.setScene(new Scene(pane, 400, 300));
        dialog.setTitle("Task History");
        dialog.show();
    }

    // Styling methods
    private void styleLabel(Label label, boolean isTitle) {
        label.setStyle("-fx-text-fill: #FFFFFF; -fx-font-family: 'Arial';" + (isTitle ? "-fx-font-size: 24;" : "-fx-font-size: 14;"));
    }

    private void styleTextField(TextField textField) {
        textField.setStyle("-fx-background-color: #283034; -fx-text-fill: #FFFFFF; -fx-prompt-text-fill: #A0A0A0; -fx-border-color: #3A4A4D; -fx-border-radius: 5; -fx-background-radius: 5;");
        textField.setPrefWidth(120);
    }

    private void styleTextArea(TextArea textArea) {
        textArea.setStyle("-fx-background-color: #283034; -fx-text-fill: #FFFFFF; -fx-prompt-text-fill: #A0A0A0; -fx-border-color: #3A4A4D; -fx-border-radius: 5; -fx-background-radius: 5;");
    }

    private void styleComboBox(ComboBox<?> comboBox) {
        comboBox.setStyle("-fx-background-color: #283034; -fx-text-fill: #FFFFFF; -fx-border-color: #3A4A4D; -fx-border-radius: 5; -fx-background-radius: 5;");
        comboBox.setPrefWidth(120);
    }

    private void stylePrimaryButton(Button button) {
        button.setPrefWidth(100);
        button.setStyle("-fx-background-color: #3A4A4D; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-radius: 5; -fx-background-radius: 5;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #4A5A5D; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-radius: 5; -fx-background-radius: 5;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #3A4A4D; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-radius: 5; -fx-background-radius: 5;"));
    }

    private void styleSecondaryButton(Button button) {
        button.setPrefWidth(100);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-color: #3A4A4D; -fx-border-radius: 5; -fx-background-radius: 5;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #3A4A4D; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-color: #3A4A4D; -fx-border-radius: 5; -fx-background-radius: 5;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: transparent; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-color: #3A4A4D; -fx-border-radius: 5; -fx-background-radius: 5;"));
    }

    private void styleSmallButton(Button button) {
        button.setPrefWidth(100);
        button.setStyle("-fx-background-color: #3A4A4D; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-radius: 5; -fx-background-radius: 5;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #4A5A5D; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-radius: 5; -fx-background-radius: 5;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #3A4A4D; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-radius: 5; -fx-background-radius: 5;"));
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
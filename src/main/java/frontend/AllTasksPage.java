package frontend;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AllTasksPage extends Application {
    private String currentUserRole;
    private String currentUsername;
    private VBox taskDisplay;
    private ComboBox<String> statusFilter;
    private ComboBox<String> priorityFilter;
    private TextField fromDateField;
    private TextField toDateField;

    public AllTasksPage(String userRole, String username) {
        this.currentUserRole = userRole;
        this.currentUsername = username != null ? username : "DefaultManager"; // Fallback to avoid null
    }

    @Override
    public void start(Stage primaryStage) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("All Tasks");
        titleLabel.setFont(Font.font("Arial", 24));

        HBox filterBox = new HBox(10);
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "To-Do", "In Progress", "Completed");
        statusFilter.setValue("All");
        priorityFilter = new ComboBox<>();
        priorityFilter.getItems().addAll("All", "Low", "Medium", "High");
        priorityFilter.setValue("All");
        fromDateField = new TextField();
        fromDateField.setPromptText("From (YYYY-MM-DD)");
        toDateField = new TextField();
        toDateField.setPromptText("To (YYYY-MM-DD)");
        Button filterButton = new Button("Filter");
        styleButton(filterButton);
        filterButton.setOnAction(e -> displayTasks(statusFilter.getValue(), priorityFilter.getValue(), fromDateField.getText(), toDateField.getText()));
        Button clearButton = new Button("Clear");
        styleButton(clearButton);
        clearButton.setOnAction(e -> {
            statusFilter.setValue("All");
            priorityFilter.setValue("All");
            fromDateField.clear();
            toDateField.clear();
            displayTasks("All", "All", "", "");
        });
        filterBox.getChildren().addAll(
                new Label("Status:"), statusFilter,
                new Label("Priority:"), priorityFilter,
                new Label("From Date:"), fromDateField,
                new Label("To Date:"), toDateField,
                filterButton, clearButton
        );

        ScrollPane scrollPane = new ScrollPane();
        taskDisplay = new VBox(10);
        scrollPane.setContent(taskDisplay);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        displayTasks("All", "All", "", "");
        showTaskNotifications();

        Button createButton = new Button("Create New Task");
        styleButton(createButton);
        createButton.setOnAction(e -> showCreateTaskDialog());
        content.getChildren().addAll(titleLabel, filterBox, scrollPane, createButton);

        Scene scene = new Scene(content, 800, 600);
        primaryStage.setTitle("TMS - All Tasks");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void displayTasks(String statusFilter, String priorityFilter, String fromDate, String toDate) {
        taskDisplay.getChildren().clear();
        List<Task> tasks = TaskData.getAllTasks();
        if (tasks.isEmpty()) {
            taskDisplay.getChildren().add(new Label("No tasks available."));
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate from = fromDate.isEmpty() ? null : LocalDate.parse(fromDate, formatter);
        LocalDate to = toDate.isEmpty() ? null : LocalDate.parse(toDate, formatter);

        for (Task task : tasks) {
            boolean matchesStatus = statusFilter.equals("All") || task.getStatus().equals(statusFilter);
            boolean matchesPriority = priorityFilter.equals("All") || task.getPriority().equals(priorityFilter);
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

            if (matchesStatus && matchesPriority && matchesDate) {
                HBox taskBox = new HBox(10);
                taskBox.setPadding(new Insets(10));
                taskBox.setStyle("-fx-border-color: #ccc;");

                String deadline = task.getDeadline() != null ? task.getDeadline() : "N/A";
                String assignedTo = task.getAssignedTo() != null ? task.getAssignedTo() : "Unassigned";
                Label taskInfo = new Label(String.format("%s - %s (Priority: %s, Due: %s, Assigned: %s)",
                        task.getTitle(), task.getStatus(), task.getPriority(), deadline, assignedTo));

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

                Button editButton = new Button("Edit");
                styleSmallButton(editButton);
                editButton.setOnAction(e -> showEditTaskDialog(task));
                Button deleteButton = new Button("Delete");
                styleSmallButton(deleteButton);
                deleteButton.setOnAction(e -> {
                    try {
                        TaskData.deleteTask(task.getId(), currentUsername);
                        displayTasks(statusFilter, priorityFilter, fromDate, toDate);
                        showAlert("Success", "Task deleted successfully.");
                    } catch (SQLException ex) {
                        showAlert("Error", "Failed to delete task: " + ex.getMessage());
                    }
                });
                Button historyButton = new Button("View History");
                styleSmallButton(historyButton);
                historyButton.setOnAction(e -> showTaskHistoryDialog(task));

                taskBox.getChildren().addAll(taskInfo, editButton, deleteButton, historyButton);
                taskDisplay.getChildren().add(taskBox);
            }
        }
    }

    private void showTaskNotifications() {
        List<Task> tasks = TaskData.getAllTasks();
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
            alert.setHeaderText("There are upcoming or overdue tasks!");
            alert.setContentText(notificationMessage.toString());
            alert.showAndWait();
        }
    }

    private void showCreateTaskDialog() {
        Stage dialog = new Stage();
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));

        TextField titleField = new TextField();
        titleField.setPromptText("Enter task title");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Enter task description");
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("To-Do", "In Progress", "Completed");
        statusBox.setValue("To-Do");
        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("Low", "Medium", "High");
        priorityBox.setValue("Medium");
        TextField deadlineField = new TextField();
        deadlineField.setPromptText("YYYY-MM-DD");
        TextField assignedToField = new TextField();
        assignedToField.setPromptText("Enter username");
        TextArea remarksArea = new TextArea();
        remarksArea.setPromptText("Enter remarks");

        Button createButton = new Button("Create");
        styleButton(createButton);
        createButton.setOnAction(e -> {
            if (titleField.getText().isEmpty() || assignedToField.getText().isEmpty()) {
                showAlert("Error", "Title and Assigned To fields are required.");
                return;
            }
            Task task = new Task(0, titleField.getText(), descArea.getText(), statusBox.getValue(),
                    priorityBox.getValue(), deadlineField.getText(), assignedToField.getText());
            task.setRemarks(remarksArea.getText());
            try {
                TaskData.addTask(task, currentUsername);
                displayTasks(statusFilter.getValue(), priorityFilter.getValue(), fromDateField.getText(), toDateField.getText());
                dialog.close();
                showAlert("Success", "Task created successfully.");
            } catch (SQLException ex) {
                showAlert("Error", "Failed to create task: " + ex.getMessage());
            }
        });

        pane.getChildren().addAll(
                new Label("Title:"), titleField, new Label("Description:"), descArea,
                new Label("Status:"), statusBox, new Label("Priority:"), priorityBox,
                new Label("Deadline:"), deadlineField, new Label("Assigned To:"), assignedToField,
                new Label("Remarks:"), remarksArea, createButton
        );
        dialog.setScene(new Scene(pane, 300, 500));
        dialog.show();
    }

    private void showEditTaskDialog(Task task) {
        Stage dialog = new Stage();
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));

        TextField titleField = new TextField(task.getTitle());
        TextArea descArea = new TextArea(task.getDescription());
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("To-Do", "In Progress", "Completed");
        statusBox.setValue(task.getStatus());
        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("Low", "Medium", "High");
        priorityBox.setValue(task.getPriority());
        TextField deadlineField = new TextField(task.getDeadline());
        TextField assignedToField = new TextField(task.getAssignedTo());
        TextArea remarksArea = new TextArea(task.getRemarks());

        Button saveButton = new Button("Save");
        styleButton(saveButton);
        saveButton.setOnAction(e -> {
            if (titleField.getText().isEmpty() || assignedToField.getText().isEmpty()) {
                showAlert("Error", "Title and Assigned To fields are required.");
                return;
            }
            Task updatedTask = new Task(task.getId(), titleField.getText(), descArea.getText(), statusBox.getValue(),
                    priorityBox.getValue(), deadlineField.getText(), assignedToField.getText());
            updatedTask.setRemarks(remarksArea.getText());
            try {
                TaskData.updateTask(updatedTask, currentUsername);
                displayTasks(statusFilter.getValue(), priorityFilter.getValue(), fromDateField.getText(), toDateField.getText());
                dialog.close();
                showAlert("Success", "Task updated successfully.");
            } catch (SQLException ex) {
                showAlert("Error", "Failed to update task: " + ex.getMessage());
            }
        });

        pane.getChildren().addAll(
                new Label("Title:"), titleField, new Label("Description:"), descArea,
                new Label("Status:"), statusBox, new Label("Priority:"), priorityBox,
                new Label("Deadline:"), deadlineField, new Label("Assigned To:"), assignedToField,
                new Label("Remarks:"), remarksArea, saveButton
        );
        dialog.setScene(new Scene(pane, 300, 500));
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
        button.setPrefWidth(80);
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
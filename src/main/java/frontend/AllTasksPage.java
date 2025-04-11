package frontend;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class AllTasksPage extends Application {
    private String currentUsername;
    private VBox taskDisplay;

    public AllTasksPage(String username) {
        this.currentUsername = username;
    }

    @Override
    public void start(Stage primaryStage) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: #1C2526;");

        Label titleLabel = new Label("All Tasks");
        styleLabel(titleLabel, true);

        Button addTaskButton = new Button("Add New Task");
        stylePrimaryButton(addTaskButton);
        addTaskButton.setOnAction(e -> showAddTaskDialog());

        ScrollPane scrollPane = new ScrollPane();
        taskDisplay = new VBox(10);
        scrollPane.setContent(taskDisplay);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background-color: #1C2526; -fx-border-color: #3A4A4D;");

        displayTasks();

        content.getChildren().addAll(titleLabel, addTaskButton, scrollPane);

        Scene scene = new Scene(content, 800, 600);
        primaryStage.setTitle("TMS - All Tasks");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void displayTasks() {
        taskDisplay.getChildren().clear();
        List<Task> tasks;
        tasks = TaskData.getAllTasks();

        if (tasks.isEmpty()) {
            Label noTasksLabel = new Label("No tasks available.");
            styleLabel(noTasksLabel, false);
            taskDisplay.getChildren().add(noTasksLabel);
            return;
        }

        for (Task task : tasks) {
            VBox taskCard = new VBox(5);
            taskCard.setPadding(new Insets(10));
            String cardColor = task.getPriority().equals("High") ? "#FF6F61" :
                    task.getPriority().equals("Medium") ? "#FFB347" : "#4CAF50";
            taskCard.setStyle("-fx-background-color: #283034; -fx-border-color: " + cardColor + "; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-width: 2;");
            taskCard.setOnMouseEntered(e -> taskCard.setStyle("-fx-background-color: #3A4A4D; -fx-border-color: " + cardColor + "; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-width: 2; -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
            taskCard.setOnMouseExited(e -> taskCard.setStyle("-fx-background-color: #283034; -fx-border-color: " + cardColor + "; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-width: 2;"));

            String deadline = task.getDeadline() != null ? task.getDeadline() : "N/A";
            Label taskInfo = new Label(String.format("%s - %s (Priority: %s, Due: %s, Assigned to: %s)",
                    task.getTitle(), task.getStatus(), task.getPriority(), deadline, task.getAssignedTo()));
            styleLabel(taskInfo, false);

            ProgressBar progressBar = new ProgressBar();
            progressBar.setProgress(task.getProgress() / 100.0);
            progressBar.setPrefWidth(200);
            progressBar.setStyle("-fx-accent: #4CAF50;");

            HBox buttonBox = new HBox(10);
            Button editButton = new Button("Edit");
            styleSmallButton(editButton);
            editButton.setOnAction(e -> showEditTaskDialog(task));

            Button deleteButton = new Button("Delete");
            styleSmallButton(deleteButton);
            deleteButton.setOnAction(e -> {
                try {
                    TaskData.deleteTask(task.getId(), currentUsername);
                    displayTasks();
                } catch (SQLException ex) {
                    showAlert("Error", "Failed to delete task: " + ex.getMessage());
                }
            });

            buttonBox.getChildren().addAll(editButton, deleteButton);
            taskCard.getChildren().addAll(taskInfo, progressBar, buttonBox);
            taskDisplay.getChildren().add(taskCard);
        }
    }

    private void showAddTaskDialog() {
        Stage dialog = new Stage();
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));
        pane.setStyle("-fx-background-color: #1C2526;");

        Label titleLabel = new Label("Add New Task");
        styleLabel(titleLabel, true);

        TextField titleField = new TextField();
        titleField.setPromptText("Task Title");
        styleTextField(titleField);

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Task Description");
        descriptionArea.setPrefHeight(100);
        styleTextArea(descriptionArea);

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("To-Do", "In Progress", "Completed");
        statusBox.setValue("To-Do");
        styleComboBox(statusBox);

        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("High", "Medium", "Low");
        priorityBox.setValue("Medium");
        styleComboBox(priorityBox);

        TextField deadlineField = new TextField();
        deadlineField.setPromptText("Deadline (YYYY-MM-DD)");
        styleTextField(deadlineField);

        TextField assignedToField = new TextField();
        assignedToField.setPromptText("Assigned To (Username)");
        styleTextField(assignedToField);

        Slider progressSlider = new Slider(0, 100, 0);
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
            Task task = new Task(0, titleField.getText(), descriptionArea.getText(),
                    statusBox.getValue(), priorityBox.getValue(), deadlineField.getText(),
                    assignedToField.getText());
            task.setProgress((int) progressSlider.getValue());
            try {
                TaskData.addTask(task, currentUsername);
                displayTasks();
                dialog.close();
            } catch (SQLException ex) {
                showAlert("Error", "Failed to add task: " + ex.getMessage());
            }
        });

        pane.getChildren().addAll(titleLabel, titleField, descriptionArea, statusBox, priorityBox,
                deadlineField, assignedToField, progressSlider, progressValueLabel, saveButton);
        dialog.setScene(new Scene(pane, 400, 500));
        dialog.show();
    }

    private void showEditTaskDialog(Task task) {
        Stage dialog = new Stage();
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));
        pane.setStyle("-fx-background-color: #1C2526;");

        Label titleLabel = new Label("Edit Task");
        styleLabel(titleLabel, true);

        TextField titleField = new TextField(task.getTitle());
        styleTextField(titleField);

        TextArea descriptionArea = new TextArea(task.getDescription());
        descriptionArea.setPrefHeight(100);
        styleTextArea(descriptionArea);

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("To-Do", "In Progress", "Completed");
        statusBox.setValue(task.getStatus());
        styleComboBox(statusBox);

        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("High", "Medium", "Low");
        priorityBox.setValue(task.getPriority());
        styleComboBox(priorityBox);

        TextField deadlineField = new TextField(task.getDeadline());
        styleTextField(deadlineField);

        TextField assignedToField = new TextField(task.getAssignedTo());
        styleTextField(assignedToField);

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
            task.setTitle(titleField.getText());
            task.setDescription(descriptionArea.getText());
            task.setStatus(statusBox.getValue());
            task.setPriority(priorityBox.getValue());
            task.setDeadline(deadlineField.getText());
            task.setAssignedTo(assignedToField.getText());
            task.setProgress((int) progressSlider.getValue());
            try {
                TaskData.updateTask(task, currentUsername);
                displayTasks();
                dialog.close();
            } catch (SQLException ex) {
                showAlert("Error", "Failed to update task: " + ex.getMessage());
            }
        });

        pane.getChildren().addAll(titleLabel, titleField, descriptionArea, statusBox, priorityBox,
                deadlineField, assignedToField, progressSlider, progressValueLabel, saveButton);
        dialog.setScene(new Scene(pane, 400, 500));
        dialog.show();
    }

    // Styling methods
    private void styleLabel(Label label, boolean isTitle) {
        label.setStyle("-fx-text-fill: #FFFFFF; -fx-font-family: 'Arial';" + (isTitle ? "-fx-font-size: 24;" : "-fx-font-size: 14;"));
    }

    private void styleTextField(TextField textField) {
        textField.setStyle("-fx-background-color: #283034; -fx-text-fill: #FFFFFF; -fx-prompt-text-fill: #A0A0A0; -fx-border-color: #3A4A4D; -fx-border-radius: 5; -fx-background-radius: 5;");
        textField.setPrefWidth(200);
    }

    private void styleTextArea(TextArea textArea) {
        textArea.setStyle("-fx-background-color: #283034; -fx-text-fill: #FFFFFF; -fx-prompt-text-fill: #A0A0A0; -fx-border-color: #3A4A4D; -fx-border-radius: 5; -fx-background-radius: 5;");
    }

    private void styleComboBox(ComboBox<?> comboBox) {
        comboBox.setStyle("-fx-background-color: #283034; -fx-text-fill: #FFFFFF; -fx-border-color: #3A4A4D; -fx-border-radius: 5; -fx-background-radius: 5;");
        comboBox.setPrefWidth(200);
    }

    private void stylePrimaryButton(Button button) {
        button.setPrefWidth(100);
        button.setStyle("-fx-background-color: #3A4A4D; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-radius: 5; -fx-background-radius: 5;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #4A5A5D; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-radius: 5; -fx-background-radius: 5;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #3A4A4D; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial'; -fx-border-radius: 5; -fx-background-radius: 5;"));
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
package frontend;

import javafx.application.Application;
import javafx.application.Platform;
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

public class AllTasksPage extends Application {

    private String currentUserRole = "Manager";  // Set to "Manager" for testing
    private List<Task> taskList = new ArrayList<>();
    private static final String DB_URL = "jdbc:mysql://localhost:3306/TaskManagementDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "zeenunew100";

    private VBox mainTaskDisplayVBox; // Store a reference to the main VBox
    private ComboBox<String> statusFilter;
    private ComboBox<String> priorityFilter;

    public static class Task {
        int id;
        String title;
        String description;
        String status;
        String priority;
        String deadline;
        String assignedTo;

        Task(int id, String title, String description, String status, String priority, String deadline, String assignedTo) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.status = status;
            this.priority = priority;
            this.deadline = deadline;
            this.assignedTo = assignedTo;
        }
    }

    // ADD THIS DEFAULT CONSTRUCTOR
    public AllTasksPage() {
        this("Manager"); // Call the other constructor with a default role
    }

    public AllTasksPage(String userRole) {
        this.currentUserRole = userRole;
    }

    @Override
    public void start(Stage primaryStage) {
        VBox allTasksContent = createContent();

        Scene scene = new Scene(allTasksContent, 800, 600);
        primaryStage.setTitle("Task Management System - All Tasks");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public VBox createContent() {
        loadTasksFromDatabase();

        VBox allTasksContent = new VBox(20);
        allTasksContent.setPadding(new Insets(20));
        allTasksContent.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("All Tasks");
        titleLabel.setFont(Font.font("Arial", 24));

        HBox filterBox = createFilterBox();

        ScrollPane scrollPane = new ScrollPane();
        mainTaskDisplayVBox = new VBox(10); // Initialize the main VBox
        scrollPane.setContent(mainTaskDisplayVBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        displayTasks(mainTaskDisplayVBox, "All", "All");

        if (currentUserRole.equals("Manager")) {
            Button createTaskButton = new Button("Create New Task");
            styleActionButton(createTaskButton);
            createTaskButton.setOnAction(e -> showCreateTaskDialog());
            allTasksContent.getChildren().add(createTaskButton);
        }

        allTasksContent.getChildren().addAll(titleLabel, filterBox, scrollPane);
        return allTasksContent;
    }

    private HBox createFilterBox() {
        HBox filterBox = new HBox(10);
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "To-Do", "In Progress", "Completed");
        statusFilter.setValue("All");
        priorityFilter = new ComboBox<>();
        priorityFilter.getItems().addAll("All", "Low", "Medium", "High");
        priorityFilter.setValue("All");
        Button filterButton = new Button("Filter");
        styleActionButton(filterButton);
        filterButton.setOnAction(e -> {
            displayTasks(mainTaskDisplayVBox, statusFilter.getValue(), priorityFilter.getValue());
        });

        filterBox.getChildren().addAll(
                new Label("Status:"), statusFilter,
                new Label("Priority:"), priorityFilter,
                filterButton
        );
        return filterBox;
    }

    private void loadTasksFromDatabase() {
        taskList.clear();
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

                taskList.add(new Task(id, title, description, status, priority, deadline, assignedTo));
            }
        } catch (SQLException e) {
            System.err.println("Error loading tasks from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayTasks(VBox taskDisplay, String statusFilter, String priorityFilter) {
        Platform.runLater(() -> {
            taskDisplay.getChildren().clear();

            for (Task task : taskList) {
                if ((statusFilter.equals("All") || task.status.equals(statusFilter)) &&
                        (priorityFilter.equals("All") || task.priority.equals(priorityFilter))) {
                    HBox taskBox = new HBox(10);
                    taskBox.setPadding(new Insets(10));
                    taskBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1;");
                    taskBox.setAlignment(Pos.CENTER_LEFT);

                    Label taskInfo = new Label(String.format("%s - %s (Priority: %s, Due: %s, Assigned to: %s)",
                            task.title, task.status, task.priority, task.deadline, task.assignedTo));
                    taskInfo.setFont(Font.font("Arial", 14));
                    HBox.setHgrow(taskInfo, Priority.ALWAYS); // Make label take up available space

                    Button viewButton = new Button("View");
                    styleSmallButton(viewButton);
                    viewButton.setOnAction(e -> showTaskDetails(task));

                    taskBox.getChildren().add(taskInfo);
                    taskBox.getChildren().add(viewButton);

                    if (currentUserRole.equals("Manager")) {
                        Button editButton = new Button("Edit");
                        styleSmallButton(editButton);
                        editButton.setPrefWidth(80); //ADDED
                        editButton.setOnAction(e -> showEditTaskDialog(task));

                        Button deleteButton = new Button("Delete");
                        styleSmallButton(deleteButton);
                        deleteButton.setPrefWidth(80); //ADDED
                        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                        deleteButton.setOnMouseEntered(e ->
                                deleteButton.setStyle("-fx-background-color: #da190b; -fx-text-fill: white;")
                        );
                        deleteButton.setOnMouseExited(e ->
                                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;")
                        );
                        deleteButton.setOnAction(e -> deleteTask(task));

                        taskBox.getChildren().addAll(editButton, deleteButton);
                    }

                    taskDisplay.getChildren().add(taskBox);
                }
            }
        });
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
                new Label("Assigned To: " + task.assignedTo)
        );

        Scene dialogScene = new Scene(dialogPane, 300, 250);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void showEditTaskDialog(Task task) {
        Stage dialog = new Stage();
        VBox dialogPane = new VBox(10);
        dialogPane.setPadding(new Insets(10));

        TextField titleField = new TextField(task.title);
        TextArea descArea = new TextArea(task.description);
        descArea.setPrefHeight(100);
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("To-Do", "In Progress", "Completed");
        statusBox.setValue(task.status);
        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("Low", "Medium", "High");
        priorityBox.setValue(task.priority);
        TextField deadlineField = new TextField(task.deadline);
        TextField assignedToField = new TextField(task.assignedTo);

        Button saveButton = new Button("Save");
        styleActionButton(saveButton);
        saveButton.setOnAction(e -> {
            javafx.concurrent.Task<Void> taskUpdate = new javafx.concurrent.Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                         PreparedStatement pstmt = conn.prepareStatement(
                                 "UPDATE tasks SET title = ?, description = ?, status = ?, priority = ?, deadline = ?, assigned_to = ? WHERE id = ?")) {

                        pstmt.setString(1, titleField.getText());
                        pstmt.setString(2, descArea.getText());
                        pstmt.setString(3, statusBox.getValue());
                        pstmt.setString(4, priorityBox.getValue());
                        pstmt.setString(5, deadlineField.getText());
                        pstmt.setString(6, assignedToField.getText());
                        pstmt.setInt(7, task.id);

                        int affectedRows = pstmt.executeUpdate();
                        if (affectedRows > 0) {
                            System.out.println("Task updated successfully.");
                            loadTasksFromDatabase();  // Refresh task list
                            return null;
                        } else {
                            System.out.println("Task update failed.");
                            return null;
                        }
                    } catch (SQLException ex) {
                        System.err.println("Error updating task in database: " + ex.getMessage());
                        ex.printStackTrace();
                        throw ex;
                    }
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        displayTasks(mainTaskDisplayVBox, statusFilter.getValue(), priorityFilter.getValue());
                        showAlert("Task Updated", "Task updated successfully.");
                        dialog.close();
                    });
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        showAlert("Database Error", "Error updating task in database: " + getException().getMessage());
                    });
                }
            };
            new Thread(taskUpdate).start();
        });

        dialogPane.getChildren().addAll(
                new Label("Edit Task"),
                new Label("Title:"), titleField,
                new Label("Description:"), descArea,
                new Label("Status:"), statusBox,
                new Label("Priority:"), priorityBox,
                new Label("Deadline:"), deadlineField,
                new Label("Assigned To:"), assignedToField,
                saveButton
        );

        Scene dialogScene = new Scene(dialogPane, 300, 450);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void deleteTask(Task task) {
        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Task");
        confirmAlert.setContentText("Are you sure you want to delete task: " + task.title + "?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                javafx.concurrent.Task<Void> taskDelete = new javafx.concurrent.Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM tasks WHERE id = ?")) {

                            pstmt.setInt(1, task.id);
                            int affectedRows = pstmt.executeUpdate();

                            if (affectedRows > 0) {
                                System.out.println("Task deleted successfully.");
                                loadTasksFromDatabase();  // Refresh task list
                                return null;
                            } else {
                                System.out.println("Task deletion failed.");
                                return null;
                            }
                        } catch (SQLException ex) {
                            System.err.println("Error deleting task from database: " + ex.getMessage());
                            ex.printStackTrace();
                            throw ex;
                        }
                    }

                    @Override
                    protected void succeeded() {
                        Platform.runLater(() -> {
                            displayTasks(mainTaskDisplayVBox, statusFilter.getValue(), priorityFilter.getValue());
                            showAlert("Task Deleted", "Task deleted successfully.");
                        });
                    }

                    @Override
                    protected void failed() {
                        Platform.runLater(() -> {
                            showAlert("Database Error", "Error deleting task from database: " + getException().getMessage());
                        });
                    }
                };
                new Thread(taskDelete).start();
            }
        });
    }

    private void showCreateTaskDialog() {
        Stage dialog = new Stage();
        VBox dialogPane = new VBox(10);
        dialogPane.setPadding(new Insets(10));

        TextField titleField = new TextField();
        titleField.setPromptText("Task Title");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Description");
        descArea.setPrefHeight(100);
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("To-Do", "In Progress", "Completed");
        statusBox.setValue("To-Do");
        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("Low", "Medium", "High");
        priorityBox.setValue("Medium");
        TextField deadlineField = new TextField();
        deadlineField.setPromptText("YYYY-MM-DD");
        TextField assignedToField = new TextField();
        assignedToField.setPromptText("Assigned To");

        Button createButton = new Button("Create");
        styleActionButton(createButton);
        createButton.setOnAction(e -> {
            javafx.concurrent.Task<Void> taskCreate = new javafx.concurrent.Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                         PreparedStatement pstmt = conn.prepareStatement(
                                 "INSERT INTO tasks (title, description, status, priority, deadline, assigned_to) VALUES (?, ?, ?, ?, ?, ?)")) {

                        pstmt.setString(1, titleField.getText());
                        pstmt.setString(2, descArea.getText());
                        pstmt.setString(3, statusBox.getValue());
                        pstmt.setString(4, priorityBox.getValue());
                        pstmt.setString(5, deadlineField.getText());
                        pstmt.setString(6, assignedToField.getText());

                        int affectedRows = pstmt.executeUpdate();
                        if (affectedRows > 0) {
                            System.out.println("Task created successfully.");
                            loadTasksFromDatabase();  // Refresh task list
                            return null;
                        } else {
                            System.out.println("Task creation failed.");
                            return null;
                        }
                    } catch (SQLException ex) {
                        System.err.println("Error creating task in database: " + ex.getMessage());
                        ex.printStackTrace();
                        throw ex;
                    }
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        displayTasks(mainTaskDisplayVBox, statusFilter.getValue(), priorityFilter.getValue());
                        showAlert("Task Created", "Task created successfully.");
                        dialog.close();
                    });
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        showAlert("Database Error", "Error creating task in database: " + getException().getMessage());
                    });
                }
            };
            new Thread(taskCreate).start();
        });

        dialogPane.getChildren().addAll(
                new Label("Create New Task"),
                new Label("Title:"), titleField,
                new Label("Description:"), descArea,
                new Label("Status:"), statusBox,
                new Label("Priority:"), priorityBox,
                new Label("Deadline:"), deadlineField,
                new Label("Assigned To:"), assignedToField,
                createButton
        );

        Scene dialogScene = new Scene(dialogPane, 300, 450);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package frontend;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskData {
    // Add a new task to the database
    public static void addTask(Task task, String createdBy) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO tasks (title, description, status, priority, deadline, assigned_to, remarks, progress) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getStatus());
            stmt.setString(4, task.getPriority());
            stmt.setString(5, task.getDeadline());
            stmt.setString(6, task.getAssignedTo());
            stmt.setString(7, task.getRemarks());
            stmt.setInt(8, task.getProgress());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                task.setId(rs.getInt(1)); // Set the generated ID for the task
            }

            // Log the task creation
            logTaskAction(task.getId(), createdBy, "Created task");
            createNotification(task.getAssignedTo(), "You have been assigned a new task: " + task.getTitle());
        }
    }

    // Update an existing task in the database
    public static void updateTask(Task task, String updatedBy) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE tasks SET title = ?, description = ?, status = ?, priority = ?, deadline = ?, assigned_to = ?, remarks = ?, progress = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getStatus());
            stmt.setString(4, task.getPriority());
            stmt.setString(5, task.getDeadline());
            stmt.setString(6, task.getAssignedTo());
            stmt.setString(7, task.getRemarks());
            stmt.setInt(8, task.getProgress());
            stmt.setInt(9, task.getId());
            stmt.executeUpdate();

            // Log the task update
            logTaskAction(task.getId(), updatedBy, "Updated task");
            if (!updatedBy.equals(task.getAssignedTo())) {
                createNotification(task.getAssignedTo(), "Task '" + task.getTitle() + "' has been updated by " + updatedBy);
            }
        }
    }

    // Delete a task from the database
    public static void deleteTask(int taskId, String deletedBy) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "DELETE FROM tasks WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, taskId);
            stmt.executeUpdate();

            // Log the task deletion
            logTaskAction(taskId, deletedBy, "Deleted task");
        }
    }

    // Retrieve all tasks from the database (for Managers)
    public static List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM tasks";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Task task = new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getString("priority"),
                        rs.getString("deadline"),
                        rs.getString("assigned_to")
                );
                task.setRemarks(rs.getString("remarks"));
                task.setProgress(rs.getInt("progress"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    // Retrieve tasks assigned to a specific user (for Employees)
    public static List<Task> getTasksByUser(String username) {
        List<Task> tasks = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM tasks WHERE assigned_to = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Task task = new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getString("priority"),
                        rs.getString("deadline"),
                        rs.getString("assigned_to")
                );
                task.setRemarks(rs.getString("remarks"));
                task.setProgress(rs.getInt("progress"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    // Retrieve the history of actions for a specific task
    public static List<String> getTaskHistory(int taskId) {
        List<String> history = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM task_logs WHERE task_id = ? ORDER BY created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                history.add(String.format("%s - %s: %s",
                        rs.getTimestamp("created_at"),
                        rs.getString("username"),
                        rs.getString("action")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    // Log a task action (e.g., creation, update, deletion)
    public static void logTaskAction(int taskId, String user, String action) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO task_logs (task_id, username, action) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, taskId);
            stmt.setString(2, user);
            stmt.setString(3, action);
            stmt.executeUpdate();
        }
    }

    // Create a notification for a user
    public static void createNotification(String username, String message) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO notifications (username, message) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, message);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Retrieve unread notifications for a user
    public static List<Notification> getNotifications(String username) {
        List<Notification> notifications = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM notifications WHERE username = ? AND is_read = 0 ORDER BY created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notifications.add(new Notification(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("message"),
                        rs.getTimestamp("created_at").toString(),
                        rs.getBoolean("is_read")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    // Mark all notifications as read for a user
    public static void markNotificationsAsRead(String username) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE notifications SET is_read = 1 WHERE username = ? AND is_read = 0";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.executeUpdate();
        }
    }
}
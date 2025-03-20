package frontend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TaskData {

    public static List<Task> getTaskList() {
        List<Task> taskList = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM tasks")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Task task = new Task(
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getString("deadline"),
                        rs.getString("assigned_to")
                );
                taskList.add(task);
            }
        } catch (SQLException e) {
            System.err.println("Error loading tasks from database: " + e.getMessage());
            e.printStackTrace();
        }
        return taskList;
    }

    public static class Task {
        private String title;
        private String description;
        private String status;
        private String deadline;
        private String assignedTo;

        public Task(String title, String description, String status, String deadline, String assignedTo) {
            this.title = title;
            this.description = description;
            this.status = status;
            this.deadline = deadline;
            this.assignedTo = assignedTo;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getStatus() {
            return status;
        }

        public String getDeadline() {
            return deadline;
        }

        public String getAssignedTo() {
            return assignedTo;
        }

        public void setAssignedTo(String assignedTo) {
            this.assignedTo = assignedTo;
        }

        @Override
        public String toString() {
            return "Task{" +
                    "title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", status='" + status + '\'' +
                    ", deadline='" + deadline + '\'' +
                    ", assignedTo='" + assignedTo + '\'' +
                    '}';
        }
    }
}
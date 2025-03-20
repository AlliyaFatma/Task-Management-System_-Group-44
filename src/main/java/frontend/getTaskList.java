package frontend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class getTaskList { // Remove the public modifier

    private static final String DB_URL = "jdbc:mysql://localhost:3306/TaskManagementDB"; // Move to the class level

    public static List<Task> getTaskList() {
        List<Task> tasks = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM tasks")) {
            while (rs.next()) {
                int id = rs.getInt("id"); // Get the id from the database
                tasks.add(new Task(
                        id, // Pass the id to the constructor
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getString("priority"),
                        rs.getString("deadline"),
                        rs.getString("assigned_to")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }
    static class Task {
        int id;
        String title;
        String description;
        String status;
        String priority;
        String deadline;
        String assignedTo;

        public Task(int id, String title, String description, String status,
                    String priority, String deadline, String assignedTo) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.status = status;
            this.priority = priority;
            this.deadline = deadline;
            this.assignedTo = assignedTo;
        }

        public Task(String title, String description, String status,
                    String priority, String deadline, String assignedTo) {
            this.id = -1; // Indicate no ID was loaded from the DB (for new Tasks)
            this.title = title;
            this.description = description;
            this.status = status;
            this.priority = priority;
            this.deadline = deadline;
            this.assignedTo = assignedTo;
        }
    }


}
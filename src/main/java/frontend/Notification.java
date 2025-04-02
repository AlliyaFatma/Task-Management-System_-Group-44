package frontend;

import java.sql.Timestamp;

public class Notification {
    private int id;
    private String recipientUsername;
    private String message;
    private Timestamp createdAt;
    private boolean isRead;

    // Constructor for creating a new notification (e.g., when inserting into the database)
    public Notification(int id, String recipientUsername, String message, String createdAt, boolean isRead) {
        this.id = id;
        this.recipientUsername = recipientUsername;
        this.message = message;
        this.createdAt = Timestamp.valueOf(createdAt);
        this.isRead = isRead;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    // Optional: toString method for debugging or logging
    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", recipientUsername='" + recipientUsername + '\'' +
                ", message='" + message + '\'' +
                ", createdAt=" + createdAt +
                ", isRead=" + isRead +
                '}';
    }
}
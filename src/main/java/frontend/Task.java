package frontend;

public class Task {
    private int id;
    String title;
    String description;
    String status;
    String priority;
    String deadline;
    String assignedTo;

    public Task(int id, String title, String description, String status, String priority, String deadline, String assignedTo) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.deadline = deadline;
        this.assignedTo = assignedTo;
    }

    public int getId() {
        return id;
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

    public String getPriority() {
        return priority;
    }

    public String getDeadline() {
        return deadline;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    // Optionally, include setters if you need to modify task properties
    // ...

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                ", deadline='" + deadline + '\'' +
                ", assignedTo='" + assignedTo + '\'' +
                '}';
    }
}
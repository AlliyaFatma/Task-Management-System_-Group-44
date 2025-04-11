package frontend;

public class Task {
    private int id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String deadline;
    private String assignedTo;
    private String remarks;
    private int progress; // New field for task progress (0-100)
    //Encapsulationn:
    //Constructorr overloading (Polymorphism)
    public Task(int id, String title, String description, String status, String priority, String deadline, String assignedTo) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.deadline = deadline;
        this.assignedTo = assignedTo;
        this.remarks = "";
        this.progress = 0; // Default progress
    }

    // Public Getters and seters methods
    public int getId() {
        return id;
    }

    public void setId(int id) { // Added setter for id
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
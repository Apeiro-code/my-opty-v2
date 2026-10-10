package com.myopty.workflow.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TodoTaskResponse {
    private Integer taskId;
    private Integer clientId;
    private String title;
    private String description;
    private String status;
    private LocalDate dueDate;
    private String priority;
    private Boolean reminderSent;
    private LocalDateTime createdAt;

    // Default constructor
    public TodoTaskResponse() {}

    // Constructor from Entity
    public TodoTaskResponse(Integer taskId, Integer clientId, String title, String description,
                            String status, LocalDate dueDate, String priority, Boolean reminderSent, LocalDateTime createdAt) {
        this.taskId = taskId;
        this.clientId = clientId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
        this.priority = priority;
        this.reminderSent = reminderSent;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Integer getTaskId() { return taskId; }
    public void setTaskId(Integer taskId) { this.taskId = taskId; }

    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Boolean getReminderSent() { return reminderSent; }
    public void setReminderSent(Boolean reminderSent) { this.reminderSent = reminderSent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
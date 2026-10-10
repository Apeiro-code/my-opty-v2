package com.myopty.workflow.dto;

import java.time.LocalDate;

public class TodoTaskRequest {
    private String title;
    private String description;
    private LocalDate dueDate;

    // Default constructor
    public TodoTaskRequest() {}

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
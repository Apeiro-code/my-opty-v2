package com.myopty.workflow.service;

import com.myopty.workflow.dto.TodoTaskRequest;
import com.myopty.workflow.dto.TodoTaskResponse;
import com.myopty.workflow.model.TodoTask;
import com.myopty.workflow.repository.TodoTaskRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TodoTaskService {

    private final TodoTaskRepository repository;

    public TodoTaskService(TodoTaskRepository repository) {
        this.repository = repository;
    }

    public TodoTaskResponse createTask(Integer clientId, TodoTaskRequest request) {
        TodoTask task = new TodoTask();
        task.setClientId(clientId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus("PENDING");
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setReminderSent(false);
        task.setCreatedAt(LocalDateTime.now());

        TodoTask saved = repository.save(task);
        return convertToResponse(saved);
    }

    public TodoTaskResponse updateStatus(Integer taskId, String newStatus) {
        TodoTask task = repository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        task.setStatus(newStatus);
        if ("DONE".equals(newStatus)) {
            task.setReminderSent(true);
        }
        TodoTask updated = repository.save(task);
        return convertToResponse(updated);
    }

    public java.util.List<TodoTaskResponse> getAllTasks(Integer clientId) {
        return repository.findAllByClientId(clientId).stream()
                .map(this::convertToResponse)
                .toList();
    }

    public java.util.List<TodoTaskResponse> getPendingQueue(Integer clientId) {
        return repository.findPendingTasksByClientId(clientId).stream()
                .map(this::convertToResponse)
                .toList();
    }

    public java.util.List<TodoTaskResponse> getOverdueTasks(Integer clientId) {
        return repository.findOverdueTasksByClientId(clientId).stream()
                .map(this::convertToResponse)
                .toList();
    }

    private TodoTaskResponse convertToResponse(TodoTask task) {
        return new TodoTaskResponse(
                task.getTaskId(),
                task.getClientId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDueDate(),
                task.getPriority(),
                task.getReminderSent(),
                task.getCreatedAt()
        );
    }
}
package com.myopty.workflow.service;

import com.myopty.workflow.dto.TodoTaskRequest;
import com.myopty.workflow.dto.TodoTaskResponse;
import com.myopty.workflow.model.TodoTask;
import com.myopty.workflow.repository.TodoTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TodoTaskService {

    private final TodoTaskRepository repository;

    public TodoTaskResponse createTask(Integer clientId, TodoTaskRequest request) {
        TodoTask task = new TodoTask();
        task.setClientId(clientId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus("PENDING");
        task.setDueDate(request.getDueDate());
        task.setCreatedAt(LocalDateTime.now());

        TodoTask saved = repository.save(task);
        return convertToResponse(saved);
    }

    public java.util.List<TodoTaskResponse> getAllTasks(Integer clientId) {
        return repository.findAllByClientId(clientId).stream()
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
                task.getCreatedAt()
        );
    }
}
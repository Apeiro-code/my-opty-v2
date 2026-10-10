package com.myopty.workflow.controller;

import com.myopty.workflow.dto.TodoTaskRequest;
import com.myopty.workflow.dto.TodoTaskResponse;
import com.myopty.workflow.service.TodoTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TodoTaskController {

    private final TodoTaskService service;

    public TodoTaskController(TodoTaskService service) {
        this.service = service;
    }

    @PostMapping
    public TodoTaskResponse createTask(@RequestBody TodoTaskRequest request,
                                       @RequestParam Integer clientId) {
        return service.createTask(clientId, request);
    }

    @GetMapping
    public List<TodoTaskResponse> getAllTasks(@RequestParam Integer clientId) {
        return service.getAllTasks(clientId);
    }

    @PutMapping("/{id}/status")
    public TodoTaskResponse updateTaskStatus(@PathVariable Integer id,
                                             @RequestParam String newStatus) {
        return service.updateStatus(id, newStatus);
    }

    @GetMapping("/queue")
    public List<TodoTaskResponse> getTaskQueue(@RequestParam Integer clientId) {
        return service.getPendingQueue(clientId);
    }

    @GetMapping("/overdue")
    public List<TodoTaskResponse> getOverdueTasks(@RequestParam Integer clientId) {
        return service.getOverdueTasks(clientId);
    }
}
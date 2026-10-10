package com.myopty.workflow.controller;

import com.myopty.workflow.dto.TodoTaskRequest;
import com.myopty.workflow.dto.TodoTaskResponse;
import com.myopty.workflow.service.TodoTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TodoTaskController {

    private final TodoTaskService service;

    @PostMapping
    public TodoTaskResponse createTask(@RequestBody TodoTaskRequest request,
                                       @RequestParam Integer clientId) {
        return service.createTask(clientId, request);
    }

    @GetMapping
    public List<TodoTaskResponse> getAllTasks(@RequestParam Integer clientId) {
        return service.getAllTasks(clientId);
    }
}
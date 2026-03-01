package org.assignment.taskmaster.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.assignment.taskmaster.dto.common.PagedResponse;
import org.assignment.taskmaster.dto.task.AssignTaskRequest;
import org.assignment.taskmaster.dto.task.CreateTaskRequest;
import org.assignment.taskmaster.dto.task.TaskFilterRequest;
import org.assignment.taskmaster.dto.task.TaskResponse;
import org.assignment.taskmaster.dto.task.UpdateTaskRequest;
import org.assignment.taskmaster.dto.task.UpdateTaskStatusRequest;
import org.assignment.taskmaster.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/teams/{teamId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@PathVariable Long teamId, @Valid @RequestBody CreateTaskRequest request) {
        return taskService.create(teamId, request);
    }

    @GetMapping("/teams/{teamId}/tasks")
    public PagedResponse<TaskResponse> listByTeam(@PathVariable Long teamId, @ModelAttribute TaskFilterRequest filter) {
        return taskService.listByTeam(teamId, filter);
    }

    @GetMapping("/tasks/{taskId}")
    public TaskResponse get(@PathVariable Long taskId) {
        return taskService.getById(taskId);
    }

    @PatchMapping("/tasks/{taskId}")
    public TaskResponse update(@PathVariable Long taskId, @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.update(taskId, request);
    }

    @DeleteMapping("/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long taskId) {
        taskService.delete(taskId);
    }

    @PatchMapping("/tasks/{taskId}/status")
    public TaskResponse status(@PathVariable Long taskId, @Valid @RequestBody UpdateTaskStatusRequest request) {
        return taskService.updateStatus(taskId, request);
    }

    @PatchMapping("/tasks/{taskId}/assign")
    public TaskResponse assign(@PathVariable Long taskId, @Valid @RequestBody AssignTaskRequest request) {
        return taskService.assign(taskId, request);
    }

    @GetMapping("/tasks/assigned-to-me")
    public PagedResponse<TaskResponse> assignedToMe(@ModelAttribute TaskFilterRequest filter) {
        return taskService.assignedToMe(filter);
    }
}

package org.assignment.taskmaster.service;

import org.assignment.taskmaster.dto.common.PagedResponse;
import org.assignment.taskmaster.dto.task.AssignTaskRequest;
import org.assignment.taskmaster.dto.task.CreateTaskRequest;
import org.assignment.taskmaster.dto.task.TaskFilterRequest;
import org.assignment.taskmaster.dto.task.TaskResponse;
import org.assignment.taskmaster.dto.task.UpdateTaskRequest;
import org.assignment.taskmaster.dto.task.UpdateTaskStatusRequest;

public interface TaskService {
    TaskResponse create(Long teamId, CreateTaskRequest request);

    PagedResponse<TaskResponse> listByTeam(Long teamId, TaskFilterRequest filter);

    TaskResponse getById(Long taskId);

    TaskResponse update(Long taskId, UpdateTaskRequest request);

    void delete(Long taskId);

    TaskResponse updateStatus(Long taskId, UpdateTaskStatusRequest request);

    TaskResponse assign(Long taskId, AssignTaskRequest request);

    PagedResponse<TaskResponse> assignedToMe(TaskFilterRequest filter);
}

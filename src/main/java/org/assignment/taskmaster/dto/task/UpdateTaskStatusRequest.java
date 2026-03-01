package org.assignment.taskmaster.dto.task;

import jakarta.validation.constraints.NotNull;
import org.assignment.taskmaster.entity.enums.TaskStatus;

public record UpdateTaskStatusRequest(@NotNull TaskStatus status) {
}

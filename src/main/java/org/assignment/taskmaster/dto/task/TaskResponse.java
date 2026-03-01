package org.assignment.taskmaster.dto.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.assignment.taskmaster.entity.enums.TaskPriority;
import org.assignment.taskmaster.entity.enums.TaskStatus;

public record TaskResponse(
    Long id,
    Long teamId,
    Long createdBy,
    Long assignedTo,
    String title,
    String description,
    LocalDate dueDate,
    TaskStatus status,
    TaskPriority priority,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime completedAt
) {
}

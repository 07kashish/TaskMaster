package org.assignment.taskmaster.dto.task;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.assignment.taskmaster.entity.enums.TaskPriority;

public record UpdateTaskRequest(
    @Size(max = 220) String title,
    @Size(max = 5000) String description,
    LocalDate dueDate,
    TaskPriority priority
) {
}

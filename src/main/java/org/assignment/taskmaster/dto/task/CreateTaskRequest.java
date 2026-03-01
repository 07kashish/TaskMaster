package org.assignment.taskmaster.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.assignment.taskmaster.entity.enums.TaskPriority;

public record CreateTaskRequest(
    @NotBlank @Size(max = 220) String title,
    @Size(max = 5000) String description,
    LocalDate dueDate,
    @NotNull TaskPriority priority,
    Long assignedTo
) {
}

package org.assignment.taskmaster.dto.task;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.assignment.taskmaster.entity.enums.TaskPriority;
import org.assignment.taskmaster.entity.enums.TaskStatus;

@Getter
@Setter
public class TaskFilterRequest {
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueBefore;
    private LocalDate dueAfter;
    private Long assignedTo;
    private Long createdBy;
    private String q;
    private String sortBy = "createdAt";
    private String sortDir = "desc";
    private int page = 0;
    private int size = 20;
}

package org.assignment.taskmaster.dto.team;

import java.time.LocalDateTime;

public record TeamResponse(
    Long id,
    String name,
    String description,
    Long ownerId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

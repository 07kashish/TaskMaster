package org.assignment.taskmaster.dto.user;

import java.time.LocalDateTime;
import java.util.Set;
import org.assignment.taskmaster.entity.enums.SystemRole;

public record UserResponse(
    Long id,
    String name,
    String email,
    String avatarUrl,
    Set<SystemRole> roles,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

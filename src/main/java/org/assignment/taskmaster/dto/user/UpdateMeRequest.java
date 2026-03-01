package org.assignment.taskmaster.dto.user;

import jakarta.validation.constraints.Size;

public record UpdateMeRequest(
    @Size(max = 120) String name,
    @Size(max = 500) String avatarUrl
) {
}

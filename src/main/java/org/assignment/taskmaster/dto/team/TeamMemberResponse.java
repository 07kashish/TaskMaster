package org.assignment.taskmaster.dto.team;

import java.time.LocalDateTime;
import org.assignment.taskmaster.entity.enums.TeamRole;

public record TeamMemberResponse(
    Long userId,
    String name,
    String email,
    TeamRole role,
    LocalDateTime joinedAt
) {
}

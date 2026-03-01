package org.assignment.taskmaster.dto.team;

import java.time.LocalDateTime;
import org.assignment.taskmaster.entity.enums.InvitationStatus;

public record InvitationResponse(
    Long id,
    Long teamId,
    String email,
    String token,
    InvitationStatus status,
    LocalDateTime expiresAt
) {
}

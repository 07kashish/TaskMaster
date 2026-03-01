package org.assignment.taskmaster.dto.team;

import jakarta.validation.constraints.NotNull;
import org.assignment.taskmaster.entity.enums.TeamRole;

public record UpdateMemberRoleRequest(@NotNull TeamRole role) {
}

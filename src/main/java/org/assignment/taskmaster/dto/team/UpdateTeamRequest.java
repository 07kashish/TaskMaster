package org.assignment.taskmaster.dto.team;

import jakarta.validation.constraints.Size;

public record UpdateTeamRequest(
    @Size(max = 150) String name,
    @Size(max = 2000) String description
) {
}

package org.assignment.taskmaster.dto.team;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTeamRequest(
    @NotBlank @Size(max = 150) String name,
    @Size(max = 2000) String description
) {
}

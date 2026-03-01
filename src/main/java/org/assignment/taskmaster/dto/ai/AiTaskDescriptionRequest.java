package org.assignment.taskmaster.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AiTaskDescriptionRequest(
    @NotBlank String title,
    @NotEmpty List<String> bulletPoints
) {
}

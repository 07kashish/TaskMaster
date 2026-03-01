package org.assignment.taskmaster.dto.ai;

import java.util.List;

public record AiTaskDescriptionResponse(
    String generatedDescription,
    List<String> acceptanceCriteria
) {
}

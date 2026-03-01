package org.assignment.taskmaster.dto.common;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record ApiErrorResponse(
    LocalDateTime timestamp,
    String path,
    String errorCode,
    String message,
    List<String> details
) {
}

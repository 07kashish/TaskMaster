package org.assignment.taskmaster.dto.comment;

import java.time.LocalDateTime;

public record CommentResponse(
    Long id,
    Long taskId,
    Long userId,
    String userName,
    String content,
    LocalDateTime createdAt
) {
}

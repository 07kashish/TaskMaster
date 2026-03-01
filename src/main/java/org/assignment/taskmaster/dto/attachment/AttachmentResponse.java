package org.assignment.taskmaster.dto.attachment;

import java.time.LocalDateTime;

public record AttachmentResponse(
    Long id,
    Long taskId,
    Long uploadedBy,
    String fileName,
    String contentType,
    Long size,
    String downloadUrl,
    LocalDateTime createdAt
) {
}

package org.assignment.taskmaster.service;

import java.util.List;
import org.assignment.taskmaster.dto.attachment.AttachmentResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentService {
    AttachmentResponse upload(Long taskId, MultipartFile file);

    List<AttachmentResponse> list(Long taskId);

    Resource download(Long attachmentId);
}

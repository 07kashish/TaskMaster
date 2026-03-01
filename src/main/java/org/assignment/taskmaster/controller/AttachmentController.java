package org.assignment.taskmaster.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.assignment.taskmaster.dto.attachment.AttachmentResponse;
import org.assignment.taskmaster.entity.Attachment;
import org.assignment.taskmaster.exception.AppException;
import org.assignment.taskmaster.repository.AttachmentRepository;
import org.assignment.taskmaster.service.AttachmentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final AttachmentRepository attachmentRepository;

    @PostMapping("/tasks/{taskId}/attachments")
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentResponse upload(@PathVariable Long taskId, @RequestPart("file") MultipartFile file) {
        return attachmentService.upload(taskId, file);
    }

    @GetMapping("/tasks/{taskId}/attachments")
    public List<AttachmentResponse> list(@PathVariable Long taskId) {
        return attachmentService.list(taskId);
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new AppException("NOT_FOUND", "Attachment not found", HttpStatus.NOT_FOUND));
        Resource resource = attachmentService.download(attachmentId);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(attachment.getContentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
            .body(resource);
    }
}

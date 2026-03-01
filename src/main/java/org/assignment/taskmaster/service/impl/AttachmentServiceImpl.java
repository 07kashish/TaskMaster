package org.assignment.taskmaster.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.assignment.taskmaster.dto.attachment.AttachmentResponse;
import org.assignment.taskmaster.entity.Attachment;
import org.assignment.taskmaster.entity.Task;
import org.assignment.taskmaster.entity.User;
import org.assignment.taskmaster.exception.AppException;
import org.assignment.taskmaster.repository.AttachmentRepository;
import org.assignment.taskmaster.repository.TaskRepository;
import org.assignment.taskmaster.service.AttachmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;
    private final AccessControlService accessControlService;

    @Value("${app.upload.base-dir:uploads}")
    private String uploadDir;

    @Override
    @Transactional
    public AttachmentResponse upload(Long taskId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new AppException("VALIDATION_ERROR", "File cannot be empty", HttpStatus.BAD_REQUEST);
        }
        User actor = currentUserService.requireCurrentUser();
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new AppException("NOT_FOUND", "Task not found", HttpStatus.NOT_FOUND));
        accessControlService.requireTeamMemberOrAdmin(task.getTeam(), actor);

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file.bin" : file.getOriginalFilename());
        String safeFileName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String storedName = UUID.randomUUID() + "_" + safeFileName;
        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path target = root.resolve(storedName).normalize();
        if (!target.startsWith(root)) {
            throw new AppException("VALIDATION_ERROR", "Invalid file name", HttpStatus.BAD_REQUEST);
        }

        try {
            Files.createDirectories(root);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new AppException("INTERNAL_ERROR", "Failed to store attachment", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Attachment attachment = new Attachment();
        attachment.setTask(task);
        attachment.setUploadedBy(actor);
        attachment.setFileName(safeFileName);
        attachment.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
        attachment.setSize(file.getSize());
        attachment.setStoragePath(target.toString());
        attachment.setCreatedAt(LocalDateTime.now());
        return DtoMapper.toAttachmentResponse(attachmentRepository.save(attachment));
    }

    @Override
    public List<AttachmentResponse> list(Long taskId) {
        User actor = currentUserService.requireCurrentUser();
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new AppException("NOT_FOUND", "Task not found", HttpStatus.NOT_FOUND));
        accessControlService.requireTeamMemberOrAdmin(task.getTeam(), actor);
        return attachmentRepository.findByTaskId(taskId).stream().map(DtoMapper::toAttachmentResponse).toList();
    }

    @Override
    public Resource download(Long attachmentId) {
        User actor = currentUserService.requireCurrentUser();
        Attachment attachment = attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new AppException("NOT_FOUND", "Attachment not found", HttpStatus.NOT_FOUND));
        accessControlService.requireTeamMemberOrAdmin(attachment.getTask().getTeam(), actor);

        try {
            Resource resource = new UrlResource(Paths.get(attachment.getStoragePath()).toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new AppException("NOT_FOUND", "Attachment file not found", HttpStatus.NOT_FOUND);
            }
            return resource;
        } catch (IOException e) {
            throw new AppException("INTERNAL_ERROR", "Failed to read file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

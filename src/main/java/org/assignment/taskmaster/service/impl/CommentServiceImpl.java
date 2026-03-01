package org.assignment.taskmaster.service.impl;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.assignment.taskmaster.dto.comment.CommentResponse;
import org.assignment.taskmaster.dto.comment.CreateCommentRequest;
import org.assignment.taskmaster.dto.common.PagedResponse;
import org.assignment.taskmaster.entity.Comment;
import org.assignment.taskmaster.entity.Task;
import org.assignment.taskmaster.entity.User;
import org.assignment.taskmaster.exception.AppException;
import org.assignment.taskmaster.repository.CommentRepository;
import org.assignment.taskmaster.repository.TaskRepository;
import org.assignment.taskmaster.service.CommentService;
import org.assignment.taskmaster.util.PageResponseMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;
    private final AccessControlService accessControlService;

    @Override
    @Transactional
    public CommentResponse create(Long taskId, CreateCommentRequest request) {
        User actor = currentUserService.requireCurrentUser();
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new AppException("NOT_FOUND", "Task not found", HttpStatus.NOT_FOUND));
        accessControlService.requireTeamMemberOrAdmin(task.getTeam(), actor);

        Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(actor);
        comment.setContent(request.content());
        comment.setCreatedAt(LocalDateTime.now());
        return DtoMapper.toCommentResponse(commentRepository.save(comment));
    }

    @Override
    public PagedResponse<CommentResponse> list(Long taskId, int page, int size) {
        User actor = currentUserService.requireCurrentUser();
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new AppException("NOT_FOUND", "Task not found", HttpStatus.NOT_FOUND));
        accessControlService.requireTeamMemberOrAdmin(task.getTeam(), actor);
        Page<CommentResponse> result = commentRepository.findByTaskId(taskId, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)))
            .map(DtoMapper::toCommentResponse);
        return PageResponseMapper.from(result);
    }
}

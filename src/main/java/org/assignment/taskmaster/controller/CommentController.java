package org.assignment.taskmaster.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.assignment.taskmaster.dto.comment.CommentResponse;
import org.assignment.taskmaster.dto.comment.CreateCommentRequest;
import org.assignment.taskmaster.dto.common.PagedResponse;
import org.assignment.taskmaster.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tasks/{taskId}/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(@PathVariable Long taskId, @Valid @RequestBody CreateCommentRequest request) {
        return commentService.create(taskId, request);
    }

    @GetMapping
    public PagedResponse<CommentResponse> list(
        @PathVariable Long taskId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return commentService.list(taskId, page, size);
    }
}

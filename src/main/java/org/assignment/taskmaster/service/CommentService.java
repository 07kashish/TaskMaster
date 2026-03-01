package org.assignment.taskmaster.service;

import org.assignment.taskmaster.dto.comment.CommentResponse;
import org.assignment.taskmaster.dto.comment.CreateCommentRequest;
import org.assignment.taskmaster.dto.common.PagedResponse;

public interface CommentService {
    CommentResponse create(Long taskId, CreateCommentRequest request);

    PagedResponse<CommentResponse> list(Long taskId, int page, int size);
}

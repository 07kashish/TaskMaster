package org.assignment.taskmaster.service.impl;

import org.assignment.taskmaster.dto.attachment.AttachmentResponse;
import org.assignment.taskmaster.dto.comment.CommentResponse;
import org.assignment.taskmaster.dto.task.TaskResponse;
import org.assignment.taskmaster.dto.team.InvitationResponse;
import org.assignment.taskmaster.dto.team.TeamMemberResponse;
import org.assignment.taskmaster.dto.team.TeamResponse;
import org.assignment.taskmaster.dto.user.UserResponse;
import org.assignment.taskmaster.entity.Attachment;
import org.assignment.taskmaster.entity.Comment;
import org.assignment.taskmaster.entity.Invitation;
import org.assignment.taskmaster.entity.Task;
import org.assignment.taskmaster.entity.Team;
import org.assignment.taskmaster.entity.TeamMember;
import org.assignment.taskmaster.entity.User;

public final class DtoMapper {
    private DtoMapper() {
    }

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAvatarUrl(),
            user.getRoles(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    public static TeamResponse toTeamResponse(Team team) {
        return new TeamResponse(
            team.getId(),
            team.getName(),
            team.getDescription(),
            team.getOwner().getId(),
            team.getCreatedAt(),
            team.getUpdatedAt()
        );
    }

    public static TeamMemberResponse toTeamMemberResponse(TeamMember member) {
        return new TeamMemberResponse(
            member.getUser().getId(),
            member.getUser().getName(),
            member.getUser().getEmail(),
            member.getRole(),
            member.getJoinedAt()
        );
    }

    public static InvitationResponse toInvitationResponse(Invitation invitation) {
        return new InvitationResponse(
            invitation.getId(),
            invitation.getTeam().getId(),
            invitation.getEmail(),
            invitation.getToken(),
            invitation.getStatus(),
            invitation.getExpiresAt()
        );
    }

    public static TaskResponse toTaskResponse(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getTeam().getId(),
            task.getCreatedBy().getId(),
            task.getAssignedTo() == null ? null : task.getAssignedTo().getId(),
            task.getTitle(),
            task.getDescription(),
            task.getDueDate(),
            task.getStatus(),
            task.getPriority(),
            task.getCreatedAt(),
            task.getUpdatedAt(),
            task.getCompletedAt()
        );
    }

    public static CommentResponse toCommentResponse(Comment comment) {
        return new CommentResponse(
            comment.getId(),
            comment.getTask().getId(),
            comment.getUser().getId(),
            comment.getUser().getName(),
            comment.getContent(),
            comment.getCreatedAt()
        );
    }

    public static AttachmentResponse toAttachmentResponse(Attachment attachment) {
        return new AttachmentResponse(
            attachment.getId(),
            attachment.getTask().getId(),
            attachment.getUploadedBy().getId(),
            attachment.getFileName(),
            attachment.getContentType(),
            attachment.getSize(),
            "/api/attachments/" + attachment.getId() + "/download",
            attachment.getCreatedAt()
        );
    }
}

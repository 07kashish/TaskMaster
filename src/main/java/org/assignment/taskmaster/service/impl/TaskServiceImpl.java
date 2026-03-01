package org.assignment.taskmaster.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.assignment.taskmaster.dto.common.PagedResponse;
import org.assignment.taskmaster.dto.task.AssignTaskRequest;
import org.assignment.taskmaster.dto.task.CreateTaskRequest;
import org.assignment.taskmaster.dto.task.TaskFilterRequest;
import org.assignment.taskmaster.dto.task.TaskResponse;
import org.assignment.taskmaster.dto.task.UpdateTaskRequest;
import org.assignment.taskmaster.dto.task.UpdateTaskStatusRequest;
import org.assignment.taskmaster.entity.Task;
import org.assignment.taskmaster.entity.Team;
import org.assignment.taskmaster.entity.User;
import org.assignment.taskmaster.entity.enums.TaskStatus;
import org.assignment.taskmaster.entity.enums.TeamRole;
import org.assignment.taskmaster.exception.AppException;
import org.assignment.taskmaster.repository.TaskRepository;
import org.assignment.taskmaster.repository.TeamMemberRepository;
import org.assignment.taskmaster.repository.TeamRepository;
import org.assignment.taskmaster.repository.UserRepository;
import org.assignment.taskmaster.service.TaskService;
import org.assignment.taskmaster.util.PageResponseMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final AccessControlService accessControlService;

    @Override
    @Transactional
    public TaskResponse create(Long teamId, CreateTaskRequest request) {
        User actor = currentUserService.requireCurrentUser();
        Team team = findTeam(teamId);
        accessControlService.requireTeamMemberOrAdmin(team, actor);

        Task task = new Task();
        task.setTeam(team);
        task.setCreatedBy(actor);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        task.setPriority(request.priority());
        task.setStatus(TaskStatus.OPEN);
        if (request.assignedTo() != null) {
            task.setAssignedTo(requireTeamMemberUser(team, request.assignedTo()));
        }
        return DtoMapper.toTaskResponse(taskRepository.save(task));
    }

    @Override
    public PagedResponse<TaskResponse> listByTeam(Long teamId, TaskFilterRequest filter) {
        User actor = currentUserService.requireCurrentUser();
        Team team = findTeam(teamId);
        accessControlService.requireTeamMemberOrAdmin(team, actor);
        Pageable pageable = page(filter);
        Page<TaskResponse> result = taskRepository.findAll(
            TaskSpecifications.byFilter(filter).and((root, query, cb) -> cb.equal(root.get("team").get("id"), teamId)),
            pageable
        ).map(DtoMapper::toTaskResponse);
        return PageResponseMapper.from(result);
    }

    @Override
    public TaskResponse getById(Long taskId) {
        User actor = currentUserService.requireCurrentUser();
        Task task = findTask(taskId);
        accessControlService.requireTeamMemberOrAdmin(task.getTeam(), actor);
        return DtoMapper.toTaskResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse update(Long taskId, UpdateTaskRequest request) {
        User actor = currentUserService.requireCurrentUser();
        Task task = findTask(taskId);
        validateTaskEditPermission(task, actor);

        if (StringUtils.hasText(request.title())) {
            task.setTitle(request.title());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        return DtoMapper.toTaskResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public void delete(Long taskId) {
        User actor = currentUserService.requireCurrentUser();
        Task task = findTask(taskId);
        boolean manager = accessControlService.isSystemAdmin(actor) || teamMemberRepository.existsByTeamIdAndUserIdAndRoleIn(
            task.getTeam().getId(), actor.getId(), List.of(TeamRole.OWNER, TeamRole.ADMIN)
        );
        if (!manager && !task.getCreatedBy().getId().equals(actor.getId())) {
            throw new AppException("FORBIDDEN", "Cannot delete this task", HttpStatus.FORBIDDEN);
        }
        taskRepository.delete(task);
    }

    @Override
    @Transactional
    public TaskResponse updateStatus(Long taskId, UpdateTaskStatusRequest request) {
        User actor = currentUserService.requireCurrentUser();
        Task task = findTask(taskId);
        validateTaskEditPermission(task, actor);

        task.setStatus(request.status());
        if (request.status() == TaskStatus.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
        } else {
            task.setCompletedAt(null);
        }
        return DtoMapper.toTaskResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse assign(Long taskId, AssignTaskRequest request) {
        User actor = currentUserService.requireCurrentUser();
        Task task = findTask(taskId);
        Team team = task.getTeam();
        accessControlService.requireTeamManagerOrAdmin(team, actor);

        if (request.assigneeUserId() == null) {
            task.setAssignedTo(null);
        } else {
            task.setAssignedTo(requireTeamMemberUser(team, request.assigneeUserId()));
        }
        return DtoMapper.toTaskResponse(taskRepository.save(task));
    }

    @Override
    public PagedResponse<TaskResponse> assignedToMe(TaskFilterRequest filter) {
        User actor = currentUserService.requireCurrentUser();
        filter.setAssignedTo(actor.getId());
        Pageable pageable = page(filter);
        Page<TaskResponse> result = taskRepository.findAll(
            TaskSpecifications.byFilter(filter).and((root, query, cb) -> cb.equal(root.get("assignedTo").get("id"), actor.getId())),
            pageable
        ).map(DtoMapper::toTaskResponse);
        return PageResponseMapper.from(result);
    }

    private Team findTeam(Long teamId) {
        return teamRepository.findById(teamId)
            .orElseThrow(() -> new AppException("NOT_FOUND", "Team not found", HttpStatus.NOT_FOUND));
    }

    private Task findTask(Long taskId) {
        return taskRepository.findById(taskId)
            .orElseThrow(() -> new AppException("NOT_FOUND", "Task not found", HttpStatus.NOT_FOUND));
    }

    private User requireTeamMemberUser(Team team, Long userId) {
        boolean member = teamMemberRepository.existsByTeamIdAndUserId(team.getId(), userId);
        if (!member) {
            throw new AppException("UNPROCESSABLE_ENTITY", "Assignee must be a team member", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return userRepository.findById(userId)
            .orElseThrow(() -> new AppException("NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
    }

    private Pageable page(TaskFilterRequest filter) {
        String sortBy = normalizeSortBy(filter.getSortBy());
        Sort.Direction direction = "asc".equalsIgnoreCase(filter.getSortDir()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        int size = Math.min(Math.max(filter.getSize(), 1), 100);
        int page = Math.max(filter.getPage(), 0);
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    private String normalizeSortBy(String sortBy) {
        if ("dueDate".equals(sortBy) || "priority".equals(sortBy) || "createdAt".equals(sortBy)) {
            return sortBy;
        }
        return "createdAt";
    }

    private void validateTaskEditPermission(Task task, User actor) {
        boolean manager = accessControlService.isSystemAdmin(actor) || teamMemberRepository.existsByTeamIdAndUserIdAndRoleIn(
            task.getTeam().getId(), actor.getId(), List.of(TeamRole.OWNER, TeamRole.ADMIN)
        );
        boolean creator = task.getCreatedBy().getId().equals(actor.getId());
        boolean assignee = task.getAssignedTo() != null && task.getAssignedTo().getId().equals(actor.getId());
        if (!manager && !creator && !assignee) {
            throw new AppException("FORBIDDEN", "Task update not permitted", HttpStatus.FORBIDDEN);
        }
    }
}

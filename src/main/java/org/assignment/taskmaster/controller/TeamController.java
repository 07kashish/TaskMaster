package org.assignment.taskmaster.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.assignment.taskmaster.dto.team.CreateTeamRequest;
import org.assignment.taskmaster.dto.team.InvitationResponse;
import org.assignment.taskmaster.dto.team.InviteRequest;
import org.assignment.taskmaster.dto.team.TeamMemberResponse;
import org.assignment.taskmaster.dto.team.TeamResponse;
import org.assignment.taskmaster.dto.team.UpdateMemberRoleRequest;
import org.assignment.taskmaster.dto.team.UpdateTeamRequest;
import org.assignment.taskmaster.service.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse create(@Valid @RequestBody CreateTeamRequest request) {
        return teamService.create(request);
    }

    @GetMapping
    public List<TeamResponse> myTeams() {
        return teamService.myTeams();
    }

    @GetMapping("/{teamId}")
    public TeamResponse get(@PathVariable Long teamId) {
        return teamService.getById(teamId);
    }

    @PatchMapping("/{teamId}")
    public TeamResponse update(@PathVariable Long teamId, @Valid @RequestBody UpdateTeamRequest request) {
        return teamService.update(teamId, request);
    }

    @PostMapping("/{teamId}/invite")
    @ResponseStatus(HttpStatus.CREATED)
    public InvitationResponse invite(@PathVariable Long teamId, @Valid @RequestBody InviteRequest request) {
        return teamService.invite(teamId, request);
    }

    @PostMapping("/invitations/accept")
    public TeamResponse accept(@RequestParam String token) {
        return teamService.acceptInvitation(token);
    }

    @GetMapping("/{teamId}/members")
    public List<TeamMemberResponse> members(@PathVariable Long teamId) {
        return teamService.members(teamId);
    }

    @PatchMapping("/{teamId}/members/{userId}")
    public TeamMemberResponse updateMemberRole(
        @PathVariable Long teamId,
        @PathVariable Long userId,
        @Valid @RequestBody UpdateMemberRoleRequest request
    ) {
        return teamService.updateMemberRole(teamId, userId, request);
    }
}

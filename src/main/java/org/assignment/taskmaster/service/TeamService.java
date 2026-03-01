package org.assignment.taskmaster.service;

import java.util.List;
import org.assignment.taskmaster.dto.team.CreateTeamRequest;
import org.assignment.taskmaster.dto.team.InvitationResponse;
import org.assignment.taskmaster.dto.team.InviteRequest;
import org.assignment.taskmaster.dto.team.TeamMemberResponse;
import org.assignment.taskmaster.dto.team.TeamResponse;
import org.assignment.taskmaster.dto.team.UpdateMemberRoleRequest;
import org.assignment.taskmaster.dto.team.UpdateTeamRequest;

public interface TeamService {
    TeamResponse create(CreateTeamRequest request);

    List<TeamResponse> myTeams();

    TeamResponse getById(Long teamId);

    TeamResponse update(Long teamId, UpdateTeamRequest request);

    InvitationResponse invite(Long teamId, InviteRequest request);

    TeamResponse acceptInvitation(String token);

    List<TeamMemberResponse> members(Long teamId);

    TeamMemberResponse updateMemberRole(Long teamId, Long userId, UpdateMemberRoleRequest request);
}

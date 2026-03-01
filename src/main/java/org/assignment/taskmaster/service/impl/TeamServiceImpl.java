package org.assignment.taskmaster.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.assignment.taskmaster.dto.team.CreateTeamRequest;
import org.assignment.taskmaster.dto.team.InvitationResponse;
import org.assignment.taskmaster.dto.team.InviteRequest;
import org.assignment.taskmaster.dto.team.TeamMemberResponse;
import org.assignment.taskmaster.dto.team.TeamResponse;
import org.assignment.taskmaster.dto.team.UpdateMemberRoleRequest;
import org.assignment.taskmaster.dto.team.UpdateTeamRequest;
import org.assignment.taskmaster.entity.Invitation;
import org.assignment.taskmaster.entity.Team;
import org.assignment.taskmaster.entity.TeamMember;
import org.assignment.taskmaster.entity.User;
import org.assignment.taskmaster.entity.enums.InvitationStatus;
import org.assignment.taskmaster.entity.enums.TeamRole;
import org.assignment.taskmaster.exception.AppException;
import org.assignment.taskmaster.repository.InvitationRepository;
import org.assignment.taskmaster.repository.TeamMemberRepository;
import org.assignment.taskmaster.repository.TeamRepository;
import org.assignment.taskmaster.repository.UserRepository;
import org.assignment.taskmaster.service.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final AccessControlService accessControlService;

    @Override
    @Transactional
    public TeamResponse create(CreateTeamRequest request) {
        User user = currentUserService.requireCurrentUser();
        Team team = new Team();
        team.setName(request.name());
        team.setDescription(request.description());
        team.setOwner(user);
        team = teamRepository.save(team);

        TeamMember ownerMember = new TeamMember();
        ownerMember.setTeam(team);
        ownerMember.setUser(user);
        ownerMember.setRole(TeamRole.OWNER);
        ownerMember.setJoinedAt(LocalDateTime.now());
        teamMemberRepository.save(ownerMember);
        return DtoMapper.toTeamResponse(team);
    }

    @Override
    public List<TeamResponse> myTeams() {
        User user = currentUserService.requireCurrentUser();
        List<Team> teams;
        if (accessControlService.isSystemAdmin(user)) {
            teams = teamRepository.findAll();
        } else {
            List<Long> teamIds = teamMemberRepository.findTeamIdsByUserId(user.getId());
            teams = teamRepository.findAllById(teamIds);
        }
        return teams.stream().map(DtoMapper::toTeamResponse).toList();
    }

    @Override
    public TeamResponse getById(Long teamId) {
        User user = currentUserService.requireCurrentUser();
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new AppException("NOT_FOUND", "Team not found", HttpStatus.NOT_FOUND));
        accessControlService.requireTeamMemberOrAdmin(team, user);
        return DtoMapper.toTeamResponse(team);
    }

    @Override
    @Transactional
    public TeamResponse update(Long teamId, UpdateTeamRequest request) {
        User user = currentUserService.requireCurrentUser();
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new AppException("NOT_FOUND", "Team not found", HttpStatus.NOT_FOUND));
        accessControlService.requireTeamManagerOrAdmin(team, user);
        if (StringUtils.hasText(request.name())) {
            team.setName(request.name());
        }
        if (request.description() != null) {
            team.setDescription(request.description());
        }
        return DtoMapper.toTeamResponse(teamRepository.save(team));
    }

    @Override
    @Transactional
    public InvitationResponse invite(Long teamId, InviteRequest request) {
        User user = currentUserService.requireCurrentUser();
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new AppException("NOT_FOUND", "Team not found", HttpStatus.NOT_FOUND));
        accessControlService.requireTeamManagerOrAdmin(team, user);

        Invitation invitation = new Invitation();
        invitation.setTeam(team);
        invitation.setEmail(request.email().toLowerCase());
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
        invitation = invitationRepository.save(invitation);
        return DtoMapper.toInvitationResponse(invitation);
    }

    @Override
    @Transactional
    public TeamResponse acceptInvitation(String token) {
        User user = currentUserService.requireCurrentUser();
        Invitation invitation = invitationRepository.findByTokenAndStatus(token, InvitationStatus.PENDING)
            .orElseThrow(() -> new AppException("NOT_FOUND", "Invitation not found", HttpStatus.NOT_FOUND));
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new AppException("UNPROCESSABLE_ENTITY", "Invitation expired", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (!invitation.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new AppException("FORBIDDEN", "Invitation email mismatch", HttpStatus.FORBIDDEN);
        }

        boolean exists = teamMemberRepository.existsByTeamIdAndUserId(invitation.getTeam().getId(), user.getId());
        if (!exists) {
            TeamMember member = new TeamMember();
            member.setTeam(invitation.getTeam());
            member.setUser(user);
            member.setRole(TeamRole.MEMBER);
            member.setJoinedAt(LocalDateTime.now());
            teamMemberRepository.save(member);
        }
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);
        return DtoMapper.toTeamResponse(invitation.getTeam());
    }

    @Override
    public List<TeamMemberResponse> members(Long teamId) {
        User user = currentUserService.requireCurrentUser();
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new AppException("NOT_FOUND", "Team not found", HttpStatus.NOT_FOUND));
        accessControlService.requireTeamMemberOrAdmin(team, user);
        return teamMemberRepository.findByTeamId(teamId).stream().map(DtoMapper::toTeamMemberResponse).toList();
    }

    @Override
    @Transactional
    public TeamMemberResponse updateMemberRole(Long teamId, Long userId, UpdateMemberRoleRequest request) {
        User actor = currentUserService.requireCurrentUser();
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new AppException("NOT_FOUND", "Team not found", HttpStatus.NOT_FOUND));
        accessControlService.requireTeamManagerOrAdmin(team, actor);

        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
            .orElseThrow(() -> new AppException("NOT_FOUND", "Member not found", HttpStatus.NOT_FOUND));
        if (member.getRole() == TeamRole.OWNER && !accessControlService.isSystemAdmin(actor)) {
            throw new AppException("FORBIDDEN", "Owner role cannot be changed", HttpStatus.FORBIDDEN);
        }
        if (request.role() == TeamRole.OWNER && !accessControlService.isSystemAdmin(actor)) {
            throw new AppException("FORBIDDEN", "Only system admin can set owner role", HttpStatus.FORBIDDEN);
        }
        member.setRole(request.role());
        return DtoMapper.toTeamMemberResponse(teamMemberRepository.save(member));
    }
}

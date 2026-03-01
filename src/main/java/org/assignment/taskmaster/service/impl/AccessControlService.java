package org.assignment.taskmaster.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.assignment.taskmaster.entity.Team;
import org.assignment.taskmaster.entity.TeamMember;
import org.assignment.taskmaster.entity.User;
import org.assignment.taskmaster.entity.enums.SystemRole;
import org.assignment.taskmaster.entity.enums.TeamRole;
import org.assignment.taskmaster.exception.AppException;
import org.assignment.taskmaster.repository.TeamMemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final TeamMemberRepository teamMemberRepository;

    public boolean isSystemAdmin(User user) {
        return user.getRoles().contains(SystemRole.ROLE_ADMIN);
    }

    public void requireTeamMemberOrAdmin(Team team, User user) {
        if (!isSystemAdmin(user) && !teamMemberRepository.existsByTeamIdAndUserId(team.getId(), user.getId())) {
            throw new AppException("FORBIDDEN", "Not a member of this team", HttpStatus.FORBIDDEN);
        }
    }

    public void requireTeamManagerOrAdmin(Team team, User user) {
        if (isSystemAdmin(user)) {
            return;
        }
        boolean allowed = teamMemberRepository.existsByTeamIdAndUserIdAndRoleIn(
            team.getId(), user.getId(), List.of(TeamRole.OWNER, TeamRole.ADMIN)
        );
        if (!allowed) {
            throw new AppException("FORBIDDEN", "Insufficient team permissions", HttpStatus.FORBIDDEN);
        }
    }

    public TeamRole getTeamRole(Team team, User user) {
        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(team.getId(), user.getId())
            .orElseThrow(() -> new AppException("FORBIDDEN", "Not a member of this team", HttpStatus.FORBIDDEN));
        return member.getRole();
    }
}

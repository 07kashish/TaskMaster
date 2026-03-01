package org.assignment.taskmaster.repository;

import java.util.List;
import java.util.Optional;
import org.assignment.taskmaster.entity.TeamMember;
import org.assignment.taskmaster.entity.enums.TeamRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);

    List<TeamMember> findByTeamId(Long teamId);

    @Query("select tm.team.id from TeamMember tm where tm.user.id = :userId")
    List<Long> findTeamIdsByUserId(@Param("userId") Long userId);

    boolean existsByTeamIdAndUserIdAndRoleIn(Long teamId, Long userId, List<TeamRole> roles);
}

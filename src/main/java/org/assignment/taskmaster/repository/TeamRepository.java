package org.assignment.taskmaster.repository;

import org.assignment.taskmaster.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}

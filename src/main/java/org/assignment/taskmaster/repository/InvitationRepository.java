package org.assignment.taskmaster.repository;

import java.util.Optional;
import org.assignment.taskmaster.entity.Invitation;
import org.assignment.taskmaster.entity.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByTokenAndStatus(String token, InvitationStatus status);
}

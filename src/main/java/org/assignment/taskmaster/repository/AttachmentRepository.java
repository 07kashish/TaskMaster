package org.assignment.taskmaster.repository;

import java.util.List;
import org.assignment.taskmaster.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByTaskId(Long taskId);
}

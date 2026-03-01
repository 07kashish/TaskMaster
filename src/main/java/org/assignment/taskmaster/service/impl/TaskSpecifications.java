package org.assignment.taskmaster.service.impl;

import java.util.Locale;
import org.assignment.taskmaster.dto.task.TaskFilterRequest;
import org.assignment.taskmaster.entity.Task;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class TaskSpecifications {
    private TaskSpecifications() {
    }

    public static Specification<Task> byFilter(TaskFilterRequest filter) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (filter.getStatus() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), filter.getStatus()));
            }
            if (filter.getPriority() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("priority"), filter.getPriority()));
            }
            if (filter.getDueBefore() != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("dueDate"), filter.getDueBefore()));
            }
            if (filter.getDueAfter() != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("dueDate"), filter.getDueAfter()));
            }
            if (filter.getAssignedTo() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("assignedTo").get("id"), filter.getAssignedTo()));
            }
            if (filter.getCreatedBy() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("createdBy").get("id"), filter.getCreatedBy()));
            }
            if (StringUtils.hasText(filter.getQ())) {
                String pattern = "%" + filter.getQ().toLowerCase(Locale.ROOT) + "%";
                predicates = cb.and(
                    predicates,
                    cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                    )
                );
            }
            return predicates;
        };
    }
}

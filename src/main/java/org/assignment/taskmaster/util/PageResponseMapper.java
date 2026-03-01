package org.assignment.taskmaster.util;

import org.assignment.taskmaster.dto.common.PagedResponse;
import org.springframework.data.domain.Page;

public final class PageResponseMapper {
    private PageResponseMapper() {
    }

    public static <T> PagedResponse<T> from(Page<T> page) {
        return PagedResponse.<T>builder()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .last(page.isLast())
            .build();
    }
}

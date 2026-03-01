package org.assignment.taskmaster.security;

import org.assignment.taskmaster.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static AppUserPrincipal currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new AppException("UNAUTHORIZED", "Authentication required", HttpStatus.UNAUTHORIZED);
        }
        return principal;
    }
}

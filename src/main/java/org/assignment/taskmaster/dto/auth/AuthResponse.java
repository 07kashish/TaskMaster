package org.assignment.taskmaster.dto.auth;

import org.assignment.taskmaster.dto.user.UserResponse;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresInSeconds,
    UserResponse user
) {
}

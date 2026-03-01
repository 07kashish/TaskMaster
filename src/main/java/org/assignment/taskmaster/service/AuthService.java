package org.assignment.taskmaster.service;

import org.assignment.taskmaster.dto.auth.AuthResponse;
import org.assignment.taskmaster.dto.auth.LoginRequest;
import org.assignment.taskmaster.dto.auth.LogoutRequest;
import org.assignment.taskmaster.dto.auth.RefreshRequest;
import org.assignment.taskmaster.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshRequest request);

    void logout(LogoutRequest request);
}

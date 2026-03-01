package org.assignment.taskmaster.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.assignment.taskmaster.dto.auth.AuthResponse;
import org.assignment.taskmaster.dto.auth.LoginRequest;
import org.assignment.taskmaster.dto.auth.LogoutRequest;
import org.assignment.taskmaster.dto.auth.RefreshRequest;
import org.assignment.taskmaster.dto.auth.RegisterRequest;
import org.assignment.taskmaster.entity.RefreshToken;
import org.assignment.taskmaster.entity.User;
import org.assignment.taskmaster.entity.enums.SystemRole;
import org.assignment.taskmaster.exception.AppException;
import org.assignment.taskmaster.repository.RefreshTokenRepository;
import org.assignment.taskmaster.repository.UserRepository;
import org.assignment.taskmaster.security.JwtService;
import org.assignment.taskmaster.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.refresh-expiry-seconds}")
    private long refreshExpirySeconds;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new AppException("CONFLICT", "Email already registered", HttpStatus.CONFLICT);
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.getRoles().add(SystemRole.ROLE_USER);
        user = userRepository.save(user);
        return issueTokens(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = userRepository.findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new AppException("UNAUTHORIZED", "Invalid credentials", HttpStatus.UNAUTHORIZED));
        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken token = refreshTokenRepository.findByTokenAndRevokedFalse(request.refreshToken())
            .orElseThrow(() -> new AppException("UNAUTHORIZED", "Invalid refresh token", HttpStatus.UNAUTHORIZED));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            throw new AppException("UNAUTHORIZED", "Refresh token expired", HttpStatus.UNAUTHORIZED);
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);
        return issueTokens(token.getUser());
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenRepository.findByTokenAndRevokedFalse(request.refreshToken())
            .ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = UUID.randomUUID().toString() + UUID.randomUUID();

        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setToken(refreshToken);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpirySeconds));
        entity.setRevoked(false);
        refreshTokenRepository.save(entity);

        return new AuthResponse(
            accessToken,
            refreshToken,
            "Bearer",
            jwtService.getAccessExpirySeconds(),
            DtoMapper.toUserResponse(user)
        );
    }
}

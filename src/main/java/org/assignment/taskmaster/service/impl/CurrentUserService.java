package org.assignment.taskmaster.service.impl;

import lombok.RequiredArgsConstructor;
import org.assignment.taskmaster.entity.User;
import org.assignment.taskmaster.exception.AppException;
import org.assignment.taskmaster.repository.UserRepository;
import org.assignment.taskmaster.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User requireCurrentUser() {
        Long userId = SecurityUtils.currentUser().getId();
        return userRepository.findById(userId)
            .orElseThrow(() -> new AppException("UNAUTHORIZED", "User not found", HttpStatus.UNAUTHORIZED));
    }
}

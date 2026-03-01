package org.assignment.taskmaster.security;

import lombok.RequiredArgsConstructor;
import org.assignment.taskmaster.entity.User;
import org.assignment.taskmaster.exception.AppException;
import org.assignment.taskmaster.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByEmailIgnoreCase(username)
            .orElseThrow(() -> new AppException("UNAUTHORIZED", "Invalid credentials", HttpStatus.UNAUTHORIZED));
        return new AppUserPrincipal(user);
    }

    public UserDetails loadUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException("UNAUTHORIZED", "Invalid token", HttpStatus.UNAUTHORIZED));
        return new AppUserPrincipal(user);
    }
}

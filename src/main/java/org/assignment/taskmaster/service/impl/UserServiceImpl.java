package org.assignment.taskmaster.service.impl;

import lombok.RequiredArgsConstructor;
import org.assignment.taskmaster.dto.user.UpdateMeRequest;
import org.assignment.taskmaster.dto.user.UserResponse;
import org.assignment.taskmaster.entity.User;
import org.assignment.taskmaster.repository.UserRepository;
import org.assignment.taskmaster.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    @Override
    public UserResponse me() {
        return DtoMapper.toUserResponse(currentUserService.requireCurrentUser());
    }

    @Override
    @Transactional
    public UserResponse updateMe(UpdateMeRequest request) {
        User user = currentUserService.requireCurrentUser();
        if (StringUtils.hasText(request.name())) {
            user.setName(request.name());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        return DtoMapper.toUserResponse(userRepository.save(user));
    }
}

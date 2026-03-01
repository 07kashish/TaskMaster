package org.assignment.taskmaster.service;

import org.assignment.taskmaster.dto.user.UpdateMeRequest;
import org.assignment.taskmaster.dto.user.UserResponse;

public interface UserService {
    UserResponse me();

    UserResponse updateMe(UpdateMeRequest request);
}

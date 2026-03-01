package org.assignment.taskmaster.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.assignment.taskmaster.dto.user.UpdateMeRequest;
import org.assignment.taskmaster.dto.user.UserResponse;
import org.assignment.taskmaster.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse me() {
        return userService.me();
    }

    @PatchMapping("/me")
    public UserResponse updateMe(@Valid @RequestBody UpdateMeRequest request) {
        return userService.updateMe(request);
    }
}

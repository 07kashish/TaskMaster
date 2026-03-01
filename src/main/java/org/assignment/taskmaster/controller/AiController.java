package org.assignment.taskmaster.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.assignment.taskmaster.dto.ai.AiTaskDescriptionRequest;
import org.assignment.taskmaster.dto.ai.AiTaskDescriptionResponse;
import org.assignment.taskmaster.service.AiService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    @PostMapping("/task-description")
    public AiTaskDescriptionResponse generate(@Valid @RequestBody AiTaskDescriptionRequest request) {
        return aiService.generateTaskDescription(request);
    }
}

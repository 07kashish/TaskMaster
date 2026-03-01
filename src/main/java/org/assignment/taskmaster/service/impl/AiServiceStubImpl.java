package org.assignment.taskmaster.service.impl;

import java.util.List;
import org.assignment.taskmaster.dto.ai.AiTaskDescriptionRequest;
import org.assignment.taskmaster.dto.ai.AiTaskDescriptionResponse;
import org.assignment.taskmaster.service.AiService;
import org.springframework.stereotype.Service;

@Service
public class AiServiceStubImpl implements AiService {

    @Override
    public AiTaskDescriptionResponse generateTaskDescription(AiTaskDescriptionRequest request) {
        String description = "Task: " + request.title() + "\n\nDetails:\n- " + String.join("\n- ", request.bulletPoints());
        List<String> criteria = List.of(
            "Given the task is implemented, expected behavior should be demonstrable.",
            "Given QA runs validation, all acceptance checks should pass.",
            "Given documentation review, implementation notes should be present."
        );
        return new AiTaskDescriptionResponse(description, criteria);
    }
}

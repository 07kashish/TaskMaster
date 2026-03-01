package org.assignment.taskmaster.service;

import org.assignment.taskmaster.dto.ai.AiTaskDescriptionRequest;
import org.assignment.taskmaster.dto.ai.AiTaskDescriptionResponse;

public interface AiService {
    AiTaskDescriptionResponse generateTaskDescription(AiTaskDescriptionRequest request);
}

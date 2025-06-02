package io.github.myuser.openmanusjava.core.service;

import io.github.myuser.openmanusjava.api.dto.TaskRequestDTO;
import io.github.myuser.openmanusjava.core.model.Task;

public interface TaskOrchestrationService {
    Task processNewTask(TaskRequestDTO request); // New primary method
    Task getTaskDetails(Long taskId);

    // Existing methods can be kept for more granular control or internal use,
    // or removed if processNewTask is the sole entry point.
    // For now, let's assume they might be used or phased out later.
    Task createTask(String objective); // This might become primarily internal or a simpler version
    void startTaskProcessing(Long taskId); // This logic will be largely inside processNewTask
}

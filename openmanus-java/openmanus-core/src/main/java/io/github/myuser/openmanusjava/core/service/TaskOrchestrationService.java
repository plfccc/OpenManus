package io.github.myuser.openmanusjava.core.service;

import io.github.myuser.openmanusjava.api.dto.TaskRequestDTO;
import io.github.myuser.openmanusjava.core.model.Task; // Assuming Task is in core.model

public interface TaskOrchestrationService {

    /**
     * Processes a new task request, orchestrates planning and execution.
     * This method might be asynchronous.
     * @param request The DTO containing the task description.
     * @return The created and initiated Task entity.
     */
    Task processNewTask(TaskRequestDTO request);

    /**
     * Retrieves the current status and details of a task.
     * @param taskId The ID of the task.
     * @return The Task entity, or null/throws exception if not found.
     */
    Task getTaskDetails(Long taskId);

    /**
     * (Potentially for future use or internal mechanics)
     * Manually triggers the execution of the next pending step for a task.
     * @param taskId The ID of the task.
     */
    // void executeNextStep(Long taskId);

    /**
     * (Potentially for future use)
     * Cancels a task if it's in a cancellable state.
     * @param taskId The ID of the task to cancel.
     * @return True if cancellation was successful, false otherwise.
     */
    // boolean cancelTask(Long taskId);
}

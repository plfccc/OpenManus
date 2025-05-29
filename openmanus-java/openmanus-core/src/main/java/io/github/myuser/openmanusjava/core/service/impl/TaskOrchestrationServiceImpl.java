package io.github.myuser.openmanusjava.core.service.impl;

import io.github.myuser.openmanusjava.api.dto.TaskRequestDTO;
import io.github.myuser.openmanusjava.core.exception.PlanningException;
import io.github.myuser.openmanusjava.core.model.Step;
import io.github.myuser.openmanusjava.core.model.Task;
import io.github.myuser.openmanusjava.core.model.TaskStatus;
import io.github.myuser.openmanusjava.core.repository.TaskRepository;
// StepRepository might not be explicitly needed here if steps are cascaded from Task
// import io.github.myuser.openmanusjava.core.repository.StepRepository;
import io.github.myuser.openmanusjava.core.service.PlannerService;
import io.github.myuser.openmanusjava.core.service.TaskOrchestrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Important for JPA operations

import java.util.List;

@Service
public class TaskOrchestrationServiceImpl implements TaskOrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(TaskOrchestrationServiceImpl.class);

    private final TaskRepository taskRepository;
    // private final StepRepository stepRepository; // Might not be needed if cascading
    private final PlannerService plannerService;

    @Autowired
    public TaskOrchestrationServiceImpl(TaskRepository taskRepository,
                                        PlannerService plannerService) {
        this.taskRepository = taskRepository;
        this.plannerService = plannerService;
        logger.info("TaskOrchestrationServiceImpl initialized with repositories and planner.");
    }

    @Override
    @Transactional // Ensures all DB operations are part of a single transaction
    public Task processNewTask(TaskRequestDTO request) {
        logger.info("Processing new task request: {}", request.getDescription());

        Task task = new Task(request.getDescription());
        task.setStatus(TaskStatus.PENDING);
        Task savedTask = taskRepository.save(task); // Save initial task to get an ID
        logger.info("Saved initial task with ID: {}", savedTask.getId());

        try {
            savedTask.setStatus(TaskStatus.PLANNING);
            taskRepository.save(savedTask); // Update status

            List<Step> plannedSteps = plannerService.createPlan(savedTask, request.getDescription());
            
            // Associate steps with the task. If CascadeType.ALL is on Task.plannedSteps,
            // saving the task will also save/update its steps.
            // Steps created by PlannerService should have the Task reference set.
            savedTask.getPlannedSteps().clear(); // Clear any existing (though unlikely for new task)
            for(Step step : plannedSteps) {
                // Ensure task is set on step if not already done by planner (it is in current PlannerServiceImpl)
                // step.setTask(savedTask); 
                savedTask.addStep(step); // addStep method in Task should handle bi-directional link
            }
            
            savedTask.setStatus(TaskStatus.EXECUTING); // Or PLANNED if execution is a separate trigger
            Task fullyPlannedTask = taskRepository.save(savedTask); // This should cascade and save steps

            logger.info("Task ID {} planned with {} steps. Current status: {}",
                        fullyPlannedTask.getId(), fullyPlannedTask.getPlannedSteps().size(), fullyPlannedTask.getStatus());
            return fullyPlannedTask;

        } catch (PlanningException e) {
            logger.error("Planning failed for task description '{}': {}", request.getDescription(), e.getMessage(), e);
            // Rollback transaction and set task to FAILED
            savedTask.setStatus(TaskStatus.FAILED);
            savedTask.setFinalResult("Planning failed: " + e.getMessage());
            taskRepository.save(savedTask);
            // Re-throw or handle as appropriate, perhaps with a custom runtime exception
            throw new RuntimeException("Task processing failed due to planning error.", e);
        } catch (Exception e) {
            logger.error("Unexpected error during task processing for description '{}': {}", request.getDescription(), e.getMessage(), e);
            savedTask.setStatus(TaskStatus.FAILED);
            savedTask.setFinalResult("Unexpected error: " + e.getMessage());
            taskRepository.save(savedTask);
            throw new RuntimeException("Unexpected error during task processing.", e);
        }
    }

    @Override
    @Transactional(readOnly = true) // Good practice for read-only operations
    public Task getTaskDetails(Long taskId) {
        logger.debug("Fetching details for task ID: {}", taskId);
        return taskRepository.findById(taskId)
            .orElse(null); // Or throw ResourceNotFoundException
    }
}

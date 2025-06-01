package io.github.myuser.openmanusjava.core.service.impl;

import io.github.myuser.openmanusjava.api.dto.TaskRequestDTO;
import io.github.myuser.openmanusjava.core.exception.PlanningException;
import io.github.myuser.openmanusjava.core.model.Step;
import io.github.myuser.openmanusjava.core.model.StepStatus; // Import StepStatus
import io.github.myuser.openmanusjava.core.model.Task;
import io.github.myuser.openmanusjava.core.model.TaskStatus;
import io.github.myuser.openmanusjava.core.repository.TaskRepository;
// StepRepository might not be explicitly needed here if steps are cascaded from Task
// import io.github.myuser.openmanusjava.core.repository.StepRepository;
import io.github.myuser.openmanusjava.core.service.PlannerService;
import io.github.myuser.openmanusjava.core.service.TaskOrchestrationService;
import io.github.myuser.openmanusjava.tool.exception.ToolNotFoundException; // Import
import io.github.myuser.openmanusjava.tool.service.ToolExecutorService;   // Import
import io.github.myuser.openmanusjava.tool.spec.ToolExecutionException;  // Import
import io.github.myuser.openmanusjava.tool.spec.ToolExecutionResult;    // Import
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map; // For step parameters if needed directly

@Service
public class TaskOrchestrationServiceImpl implements TaskOrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(TaskOrchestrationServiceImpl.class);

    private final TaskRepository taskRepository;
    private final PlannerService plannerService;
    private final ToolExecutorService toolExecutorService; // Added

    @Autowired
    public TaskOrchestrationServiceImpl(TaskRepository taskRepository,
                                        PlannerService plannerService,
                                        ToolExecutorService toolExecutorService) { // Added
        this.taskRepository = taskRepository;
        this.plannerService = plannerService;
        this.toolExecutorService = toolExecutorService; // Added
        logger.info("TaskOrchestrationServiceImpl initialized with repositories, planner, and tool executor.");
    }

    @Override
    @Transactional
    public Task processNewTask(TaskRequestDTO request) {
        logger.info("Processing new task request: {}", request.getDescription());

        Task task = new Task(request.getDescription());
        task.setStatus(TaskStatus.PENDING);
        Task savedTask = taskRepository.save(task);
        logger.info("Saved initial task with ID: {}", savedTask.getId());

        try {
            savedTask.setStatus(TaskStatus.PLANNING);
            taskRepository.save(savedTask);

            List<Step> plannedSteps = plannerService.createPlan(savedTask, request.getDescription());

            savedTask.getPlannedSteps().clear();
            for(Step step : plannedSteps) {
                // Ensure task is set on step if not already done by planner (it is in current PlannerServiceImpl)
                // step.setTask(savedTask);
                savedTask.addStep(step);
            }
            // Save task with planned steps (steps are PENDING & associated via cascade)
            Task taskWithSteps = taskRepository.save(savedTask);
            logger.info("Task ID {} planned with {} steps.", taskWithSteps.getId(), taskWithSteps.getPlannedSteps().size());

            // --- Begin Step Execution ---
            taskWithSteps.setStatus(TaskStatus.EXECUTING);
            taskRepository.save(taskWithSteps);
            logger.info("Task ID {} status set to EXECUTING.", taskWithSteps.getId());

            boolean allStepsSucceeded = executePlannedSteps(taskWithSteps);

            if (allStepsSucceeded) {
                taskWithSteps.setStatus(TaskStatus.COMPLETED);
                // Potentially aggregate results from steps into task.finalResult here
                // For now, just mark as completed.
                taskWithSteps.setFinalResult("All steps completed successfully.");
                logger.info("Task ID {} completed successfully.", taskWithSteps.getId());
            } else {
                taskWithSteps.setStatus(TaskStatus.FAILED);
                // Final result might already be set by the failing step or a general message here
                if (taskWithSteps.getFinalResult() == null || taskWithSteps.getFinalResult().isEmpty()) {
                    taskWithSteps.setFinalResult("One or more steps failed during execution.");
                }
                logger.warn("Task ID {} failed or partially completed due to step failures.", taskWithSteps.getId());
            }
            taskWithSteps.setCompletedAt(LocalDateTime.now()); // Set completion time regardless of status
            return taskRepository.save(taskWithSteps);

        } catch (PlanningException e) {
            logger.error("Planning failed for task ID {}: {}", savedTask.getId(), e.getMessage(), e);
            savedTask.setStatus(TaskStatus.FAILED);
            savedTask.setFinalResult("Planning failed: " + e.getMessage());
            savedTask.setCompletedAt(LocalDateTime.now());
            taskRepository.save(savedTask);
            // Re-throw or handle as appropriate, perhaps with a custom runtime exception
            throw new RuntimeException("Task processing failed due to planning error for task ID " + savedTask.getId(), e);
        } catch (Exception e) { // Catch-all for other unexpected errors during orchestration phase
            logger.error("Unexpected error during task processing for ID {}: {}", savedTask.getId(), e.getMessage(), e);
            savedTask.setStatus(TaskStatus.FAILED);
            savedTask.setFinalResult("Unexpected error during orchestration: " + e.getMessage());
            savedTask.setCompletedAt(LocalDateTime.now());
            taskRepository.save(savedTask);
            throw new RuntimeException("Unexpected error during task orchestration for task ID " + savedTask.getId(), e);
        }
    }

    private boolean executePlannedSteps(Task task) {
        // Iterate over the steps associated with the task.
        // These steps should be managed by the current JPA session.
        for (Step step : task.getPlannedSteps()) {
            // Double check if step was already processed (e.g. if resuming a task - future feature)
            if (step.getStatus() == StepStatus.COMPLETED || step.getStatus() == StepStatus.FAILED) {
                logger.info("Skipping already processed step ID {} with status {}", step.getId(), step.getStatus());
                continue;
            }

            logger.info("Executing step ID {} (Task ID {}): '{}' using tool '{}'",
                        step.getId(), task.getId(), step.getDescription(), step.getToolName());
            step.setStatus(StepStatus.IN_PROGRESS);
            // Task is saved before loop and after each step, or at the end of the loop if all successful
            // For now, saving task (and cascading to step) on each step status change
            taskRepository.save(task);

            try {
                // Step.parameters is Map<String, String>. ToolExecutorService expects Map<String, Object>.
                // This cast is a simplification.
                @SuppressWarnings("unchecked")
                Map<String, Object> toolParameters = (Map<String, Object>)(Map<?, ?>)step.getParameters();

                ToolExecutionResult toolResult = toolExecutorService.executeTool(step.getToolName(), toolParameters);

                step.setResult(toolResult.getOutput() != null ? toolResult.getOutput().toString() : "No output.");
                if ("SUCCESS".equals(toolResult.getStatus())) {
                    step.setStatus(StepStatus.COMPLETED);
                    logger.info("Step ID {} completed successfully. Result excerpt: {}", step.getId(),
                                step.getResult() != null ? step.getResult().substring(0, Math.min(step.getResult().length(), 100)) : "N/A");
                } else {
                    step.setStatus(StepStatus.FAILED);
                    String errorMsg = "Error: " + toolResult.getError() +
                                      (toolResult.getOutput() != null ? ". Output: " + toolResult.getOutput().toString() : "");
                    step.setResult(errorMsg.substring(0, Math.min(errorMsg.length(), 4000))); // Truncate if too long for DB
                    task.setFinalResult(errorMsg.substring(0, Math.min(errorMsg.length(), 4000))); // Set task's final result to the first error
                    logger.warn("Step ID {} failed. Error: {}", step.getId(), step.getResult());
                    taskRepository.save(task);
                    return false; // Stop further execution if one step fails
                }
            } catch (ToolNotFoundException e) {
                logger.error("ToolNotFoundException for step ID {}: {}", step.getId(), e.getMessage(), e);
                step.setStatus(StepStatus.FAILED);
                step.setResult("Tool not found: " + e.getMessage());
                task.setFinalResult("Tool not found for step: " + step.getDescription());
                taskRepository.save(task);
                return false;
            } catch (ToolExecutionException e) {
                logger.error("ToolExecutionException for step ID {}: {}", step.getId(), e.getMessage(), e);
                step.setStatus(StepStatus.FAILED);
                step.setResult("Tool execution error: " + e.getMessage());
                 task.setFinalResult("Tool execution error for step: " + step.getDescription());
                taskRepository.save(task);
                return false;
            } catch (Exception e) { // Catch any other unexpected error from tool execution
                logger.error("Unexpected exception during execution of step ID {}: {}", step.getId(), e.getMessage(), e);
                step.setStatus(StepStatus.FAILED);
                step.setResult("Unexpected error during step execution: " + e.getMessage());
                task.setFinalResult("Unexpected error during step: " + step.getDescription());
                taskRepository.save(task);
                return false;
            }
            taskRepository.save(task); // Save successful/failed step completion (cascades to step)
        }
        return true; // All steps succeeded
    }

    @Override
    @Transactional(readOnly = true)
    public Task getTaskDetails(Long taskId) {
        logger.debug("Fetching details for task ID: {}", taskId);
        return taskRepository.findById(taskId)
            .orElse(null);
    }
}

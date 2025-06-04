package io.github.myuser.openmanusjava.core.service.impl;


import io.github.myuser.openmanusjava.api.dto.TaskRequestDTO;
import io.github.myuser.openmanusjava.core.exception.PlanningException;
import io.github.myuser.openmanusjava.core.exception.ResourceNotFoundException;
import io.github.myuser.openmanusjava.core.model.Step;
import io.github.myuser.openmanusjava.core.model.StepStatus;
import io.github.myuser.openmanusjava.core.model.Task;
import io.github.myuser.openmanusjava.core.model.TaskStatus;
import io.github.myuser.openmanusjava.core.repository.TaskRepository;
import io.github.myuser.openmanusjava.core.service.PlannerService;
import io.github.myuser.openmanusjava.core.service.TaskOrchestrationService;
import io.github.myuser.openmanusjava.tool.exception.ToolNotFoundException;
import io.github.myuser.openmanusjava.tool.service.ToolExecutorService;
import io.github.myuser.openmanusjava.tool.spec.ToolExecutionException;
import io.github.myuser.openmanusjava.tool.spec.ToolExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class TaskOrchestrationServiceImpl implements TaskOrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(TaskOrchestrationServiceImpl.class);

    private final TaskRepository taskRepository;
    private final PlannerService plannerService;
    private final ToolExecutorService toolExecutorService;

    @Autowired
    public TaskOrchestrationServiceImpl(TaskRepository taskRepository,
                                        PlannerService plannerService,
                                        ToolExecutorService toolExecutorService) {
        this.taskRepository = taskRepository;
        this.plannerService = plannerService;
        this.toolExecutorService = toolExecutorService;
    }

    @Override
    @Transactional
    public Task processNewTask(TaskRequestDTO request) {
        logger.info("Processing new task request for description: {}", request.getDescription());
        Task task = new Task(request.getDescription());
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        Task savedTask = taskRepository.save(task); // Save initial task to get an ID

        MDC.put("taskId", "task-" + savedTask.getId());
        try {
            logger.info("Saved initial task. Now planning.");
            savedTask.setStatus(TaskStatus.PLANNING);
            taskRepository.save(savedTask);

            List<Step> plannedSteps = plannerService.createPlan(savedTask, request.getDescription());
            savedTask.getPlannedSteps().clear(); // Clear any existing (should be none for new task)
            for (Step step : plannedSteps) {
                savedTask.addStep(step); // This should set the back-reference from step to task
            }
            taskRepository.save(savedTask); // Save task with steps (cascading)
            logger.info("Planning complete with {} steps.", savedTask.getPlannedSteps().size());

            savedTask.setStatus(TaskStatus.EXECUTING);
            taskRepository.save(savedTask);
            logger.info("Beginning step execution.");

            boolean allStepsSucceeded = executePlannedSteps(savedTask);

            if (allStepsSucceeded) {
                savedTask.setStatus(TaskStatus.COMPLETED);
                savedTask.setFinalResult("All steps completed successfully.");
                savedTask.setCompletedAt(LocalDateTime.now());
                logger.info("Task completed successfully.");
            } else {
                savedTask.setStatus(TaskStatus.FAILED);
                if (savedTask.getFinalResult() == null || savedTask.getFinalResult().isEmpty()) {
                    savedTask.setFinalResult("One or more steps failed during execution.");
                }
                logger.warn("Task failed or partially completed.");
            }
            return taskRepository.save(savedTask);

        } catch (PlanningException e) {
            logger.error("Planning failed: {}", e.getMessage(), e);
            savedTask.setStatus(TaskStatus.FAILED);
            savedTask.setFinalResult("Planning failed: " + e.getMessage());
            taskRepository.save(savedTask);
            // Re-throw as a runtime exception to ensure transaction rollback and GlobalExceptionHandler can catch it
            throw new RuntimeException("Task processing failed due to planning error for task ID " + savedTask.getId(), e);
        } catch (Exception e) { // Catch broader exceptions that might occur outside defined ones
            logger.error("Unexpected error during task processing: {}", e.getMessage(), e);
            savedTask.setStatus(TaskStatus.FAILED);
            savedTask.setFinalResult("Unexpected error during orchestration: " + e.getMessage());
            taskRepository.save(savedTask);
            throw new RuntimeException("Unexpected error during task orchestration for task ID " + savedTask.getId(), e);
        } finally {
            MDC.remove("taskId");
        }
    }

    private boolean executePlannedSteps(Task task) {
        // MDC is already set by processNewTask
        boolean allSucceeded = true;
        for (Step step : task.getPlannedSteps()) {
            if (step.getStatus() == StepStatus.COMPLETED || step.getStatus() == StepStatus.SKIPPED) {
                continue; // Skip already completed or skipped steps
            }
            step.setStatus(StepStatus.IN_PROGRESS);
            step.setAttemptCount(step.getAttemptCount() + 1);
            step.setUpdatedAt(LocalDateTime.now());
            taskRepository.save(task); // Save step status update (via task cascade)

            logger.info("Executing step ID {} ('{}', attempt {}) using tool '{}' with params: {}",
                    step.getId(), step.getDescription(), step.getAttemptCount(), step.getToolName(), step.getParameters());
            try {
                ToolExecutionResult toolResult = toolExecutorService.executeTool(step.getToolName(), step.getParameters());
                step.setResult(toolResult.getOutput() != null ? toolResult.getOutput().toString() : null);
                step.setExecutedAt(LocalDateTime.now());

                if ("SUCCESS".equals(toolResult.getStatus())) {
                    step.setStatus(StepStatus.COMPLETED);
                    logger.info("Step ID {} completed successfully. Output: {}", step.getId(), summarized(step.getResult()));
                } else {
                    allSucceeded = false;
                    step.setStatus(StepStatus.FAILED);
                    String errorMessage = toolResult.getError() != null ? toolResult.getError() : "No specific error message from tool.";
                    step.setResult(String.format("Error: %s Output: %s", errorMessage, summarized(step.getResult())));
                    logger.warn("Step ID {} failed. Error: '{}', Output: '{}'", step.getId(), errorMessage, summarized(step.getResult()));
                    // If a step fails, we set the task's final result and stop further execution.
                    task.setFinalResult(String.format("Step %d ('%s') failed: %s", step.getSequence(), step.getDescription(), errorMessage));
                    break;
                }
            } catch (ToolNotFoundException e) {
                allSucceeded = false;
                step.setStatus(StepStatus.FAILED);
                step.setResult("Tool not found: " + e.getMessage());
                logger.error("ToolNotFoundException for step ID {}: {}", step.getId(), e.getMessage());
                task.setFinalResult(String.format("Step %d ('%s') failed: Tool '%s' not found.", step.getSequence(), step.getDescription(), step.getToolName()));
                break;
            } catch (ToolExecutionException e) {
                allSucceeded = false;
                step.setStatus(StepStatus.FAILED);
                step.setResult("Tool execution error: " + e.getMessage());
                logger.error("ToolExecutionException for step ID {}: {}", step.getId(), e.getMessage(), e);
                task.setFinalResult(String.format("Step %d ('%s') failed: Tool execution error for '%s'.", step.getSequence(), step.getDescription(), step.getToolName()));
                break;
            } finally {
                step.setUpdatedAt(LocalDateTime.now());
                taskRepository.save(task); // Persist step changes (via task cascade)
            }
        }
        return allSucceeded;
    }

    private String summarized(String text) {
        if (text == null) return null;
        return text.length() > 100 ? text.substring(0, 97) + "..." : text;
    }

    // createTask and startTaskProcessing can be simplified or made private if processNewTask is the main entry point.
    // For now, keeping them as per previous structure but with MDC.

    @Override
    @Transactional
    public Task createTask(String objective) {
        logger.info("Received request to create new task (simple) for objective: {}", objective);
        Task task = new Task(objective); // Use constructor
        task.setStatus(TaskStatus.PENDING);
        // ID, CreatedAt, UpdatedAt are set by @PrePersist in Task entity if that's configured,
        // otherwise set them manually:
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        Task savedTask = taskRepository.save(task);

        MDC.put("taskId", "task-" + savedTask.getId());
        try {
            logger.info("Simple task created and persisted with ID: {}", savedTask.getId());
            return savedTask;
        } finally {
            MDC.remove("taskId");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Task getTaskDetails(Long taskId) {
        MDC.put("taskId", "task-" + taskId);
        try {
            logger.debug("Fetching details for task.");
            return taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        } finally {
            MDC.remove("taskId");
        }
    }

    @Override
    @Transactional
    public void startTaskProcessing(Long taskId) {
        // This method's role is now significantly reduced if processNewTask handles the full flow.
        // It might be used to re-start a FAILED task, or trigger a specific part of processing.
        // For now, it just logs and sets to IN_PROGRESS if PENDING.
        MDC.put("taskId", "task-" + taskId);
        try {
            logger.info("Attempting to start/resume processing for task.");
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

            if (task.getStatus() == TaskStatus.PENDING || task.getStatus() == TaskStatus.RETRY) {
                task.setStatus(TaskStatus.IN_PROGRESS); // Or trigger planning/execution
                task.setUpdatedAt(LocalDateTime.now());
                taskRepository.save(task);
                logger.info("Task status set to IN_PROGRESS. Full processing should be handled by processNewTask or a similar orchestrator method.");
                // Potentially, you could call a re-planning or re-execution logic here.
                // For example:
                // if (task.getPlannedSteps().isEmpty()) {
                //    List<Step> plannedSteps = plannerService.createPlan(task, task.getObjective());
                //    task.getPlannedSteps().clear();
                //    plannedSteps.forEach(task::addStep);
                // }
                // executePlannedSteps(task);
                // ... update final status ...
                // taskRepository.save(task);

            } else {
                logger.warn("Task is not in a PENDING or RETRY state (current: {}). No action taken by startTaskProcessing.", task.getStatus());
            }
        } finally {
            MDC.remove("taskId");
        }
    }
}

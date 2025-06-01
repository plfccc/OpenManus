package io.github.myuser.openmanusjava.api.controller;

import io.github.myuser.openmanusjava.api.dto.StepDTO;
import io.github.myuser.openmanusjava.api.dto.TaskRequestDTO;
import io.github.myuser.openmanusjava.api.dto.TaskResponseDTO;
import io.github.myuser.openmanusjava.core.model.Step;
import io.github.myuser.openmanusjava.core.model.Task;
import io.github.myuser.openmanusjava.core.service.TaskOrchestrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.stream.Collectors;
import java.util.Collections;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    private final TaskOrchestrationService taskOrchestrationService;

    @Autowired
    public TaskController(TaskOrchestrationService taskOrchestrationService) {
        this.taskOrchestrationService = taskOrchestrationService;
    }

    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@Valid @RequestBody TaskRequestDTO taskRequestDTO) {
        logger.info("Received API request to create task: {}", taskRequestDTO.getDescription());
        try {
            Task processedTask = taskOrchestrationService.processNewTask(taskRequestDTO);
            TaskResponseDTO response = mapTaskToTaskResponseDTO(processedTask);

            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(processedTask.getId())
                    .toUri();

            return ResponseEntity.created(location).body(response);

        } catch (RuntimeException e) {
            logger.error("Error processing task: {}", e.getMessage(), e);
            // This should be handled by GlobalExceptionHandler eventually
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(null);
        }
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponseDTO> getTask(@PathVariable Long taskId) {
        logger.info("Received API request to get task details for ID: {}", taskId);
        Task task = taskOrchestrationService.getTaskDetails(taskId);
        if (task == null) {
            logger.warn("Task with ID {} not found.", taskId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Or proper error DTO
        }
        return ResponseEntity.ok(mapTaskToTaskResponseDTO(task));
    }

    // --- Mapper Methods ---
    // (Consider moving to a dedicated mapper class/component if it grows complex)
    private TaskResponseDTO mapTaskToTaskResponseDTO(Task task) {
        if (task == null) return null;
        return new TaskResponseDTO(
            task.getId(),
            task.getDescription(),
            task.getStatus() != null ? task.getStatus().name() : null,
            task.getCreatedAt(),
            task.getUpdatedAt(),
            task.getCompletedAt(),
            task.getPlannedSteps() != null ?
                task.getPlannedSteps().stream().map(this::mapStepToStepDTO).collect(Collectors.toList()) :
                Collections.emptyList(),
            task.getFinalResult()
        );
    }

    private StepDTO mapStepToStepDTO(Step step) {
        if (step == null) return null;
        return new StepDTO(
            step.getId(),
            step.getDescription(),
            step.getToolName(),
            step.getParameters(),
            step.getStatus() != null ? step.getStatus().name() : null,
            step.getResult(),
            step.getSequenceOrder()
        );
    }
}

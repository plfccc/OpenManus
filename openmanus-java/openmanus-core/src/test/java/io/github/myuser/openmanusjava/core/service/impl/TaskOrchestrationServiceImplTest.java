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
import io.github.myuser.openmanusjava.tool.exception.ToolNotFoundException;
import io.github.myuser.openmanusjava.tool.service.ToolExecutorService;
import io.github.myuser.openmanusjava.tool.spec.ToolExecutionException;
import io.github.myuser.openmanusjava.tool.spec.ToolExecutionResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskOrchestrationServiceImplTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private PlannerService plannerService;
    @Mock
    private ToolExecutorService toolExecutorService;

    @InjectMocks
    private TaskOrchestrationServiceImpl taskOrchestrationService;

    private TaskRequestDTO taskRequestDTO;
    private Task task; // Represents the task state as it's manipulated and saved

    @BeforeEach
    void setUp() {
        taskRequestDTO = new TaskRequestDTO("Test task description");

        // Task instance that will be returned by initial save and then modified
        task = new Task(taskRequestDTO.getDescription());
        task.setId(1L); // Simulate persisted task ID
        task.setPlannedSteps(new ArrayList<>()); // Initialize plannedSteps

        // Clear MDC before each test
        MDC.clear();
    }

    @Test
    void processNewTask_successPath() throws PlanningException, ToolNotFoundException, ToolExecutionException {
        // Arrange
        Step step1 = new Step(task, "Step 1", "tool1", Collections.emptyMap(), 1);
        step1.setId(10L);
        List<Step> plannedSteps = List.of(step1);

        // Capture and return the task instance during repository saves
        // Initial save (Task without ID -> Task with ID)
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            if (t.getId() == null) { // Simulating the first save that assigns an ID
                t.setId(task.getId()); // Assign the predefined ID
            }
            // Update the shared 'task' instance to reflect changes
            task.setStatus(t.getStatus());
            task.setPlannedSteps(t.getPlannedSteps());
            task.setFinalResult(t.getFinalResult());
            task.setCompletedAt(t.getCompletedAt());
            // ... copy other relevant fields if necessary
            return t; // Return the modified argument itself
        });

        when(plannerService.createPlan(any(Task.class), eq("Test task description"))).thenReturn(plannedSteps);
        when(toolExecutorService.executeTool(eq("tool1"), any())).thenReturn(new ToolExecutionResult("SUCCESS", "Step 1 output"));

        // Act
        Task resultTask = taskOrchestrationService.processNewTask(taskRequestDTO);

        // Assert
        assertNotNull(resultTask);
        assertEquals(TaskStatus.COMPLETED, resultTask.getStatus());
        assertEquals("All steps completed successfully.", resultTask.getFinalResult());
        assertNotNull(resultTask.getCompletedAt());
        assertEquals(1, resultTask.getPlannedSteps().size());

        Step executedStep = resultTask.getPlannedSteps().get(0);
        assertEquals(StepStatus.COMPLETED, executedStep.getStatus());
        assertEquals("Step 1 output", executedStep.getResult());

        // Verify interactions
        // Order of saves: 1. Initial PENDING, 2. PLANNING, 3. Steps added, 4. EXECUTING, 5. Step IN_PROGRESS, 6. Step COMPLETED, 7. Task COMPLETED
        verify(taskRepository, atLeast(5)).save(any(Task.class));
        verify(plannerService).createPlan(any(Task.class), eq("Test task description"));
        verify(toolExecutorService).executeTool(eq("tool1"), any());
    }

    @Test
    void processNewTask_planningFails() throws PlanningException {
        // Arrange
        // Initial save will set the ID on 'task' object.
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            if (t.getId() == null) t.setId(1L);
            // Update the shared 'task' instance to reflect changes
            task.setStatus(t.getStatus());
            task.setFinalResult(t.getFinalResult());
            return t;
        });
        when(plannerService.createPlan(any(Task.class), anyString())).thenThrow(new PlanningException("LLM unavailable"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskOrchestrationService.processNewTask(taskRequestDTO);
        });
        assertTrue(exception.getMessage().contains("planning error"));

        // 'task' instance should have been updated by the save mock
        assertEquals(TaskStatus.FAILED, task.getStatus());
        assertEquals("Planning failed: LLM unavailable", task.getFinalResult());

        // Initial save (PENDING), save (PLANNING), save (FAILED due to exception)
        verify(taskRepository, times(3)).save(any(Task.class));
    }

    @Test
    void processNewTask_toolExecutionFails_toolNotFound() throws PlanningException, ToolNotFoundException, ToolExecutionException {
        // Arrange
        Step step1 = new Step(task, "Step 1", "unknownTool", Collections.emptyMap(), 1);
        step1.setId(10L);
        List<Step> plannedSteps = List.of(step1);

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            if (t.getId() == null) t.setId(1L);
            task.setStatus(t.getStatus());
            task.setPlannedSteps(t.getPlannedSteps().isEmpty() ? task.getPlannedSteps() : t.getPlannedSteps()); // Keep steps if already set
            task.setFinalResult(t.getFinalResult());
             if (!t.getPlannedSteps().isEmpty() && task.getPlannedSteps().get(0) != null && t.getPlannedSteps().get(0) != null) {
                task.getPlannedSteps().get(0).setStatus(t.getPlannedSteps().get(0).getStatus());
                task.getPlannedSteps().get(0).setResult(t.getPlannedSteps().get(0).getResult());
            }
            return t;
        });

        when(plannerService.createPlan(any(Task.class), anyString())).thenReturn(plannedSteps);
        when(toolExecutorService.executeTool(eq("unknownTool"), any())).thenThrow(new ToolNotFoundException("Tool unknownTool not found"));

        // Act
        Task resultTask = taskOrchestrationService.processNewTask(taskRequestDTO);

        // Assert
        assertEquals(TaskStatus.FAILED, resultTask.getStatus());
        assertTrue(resultTask.getFinalResult().contains("Tool 'unknownTool' not found"));

        Step failedStep = resultTask.getPlannedSteps().get(0);
        assertEquals(StepStatus.FAILED, failedStep.getStatus());
        assertTrue(failedStep.getResult().contains("Tool not found: Tool unknownTool not found"));
    }

    @Test
    void processNewTask_toolExecutionFails_executionError() throws PlanningException, ToolNotFoundException, ToolExecutionException {
        // Arrange
        Step step1 = new Step(task, "Step 1", "errorTool", Collections.emptyMap(), 1);
        step1.setId(10L);
        List<Step> plannedSteps = List.of(step1);

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            if (t.getId() == null) t.setId(1L);
            task.setStatus(t.getStatus());
            task.setPlannedSteps(t.getPlannedSteps().isEmpty() ? task.getPlannedSteps() : t.getPlannedSteps());
            task.setFinalResult(t.getFinalResult());
            if (!t.getPlannedSteps().isEmpty() && task.getPlannedSteps().get(0) != null && t.getPlannedSteps().get(0) != null) {
                task.getPlannedSteps().get(0).setStatus(t.getPlannedSteps().get(0).getStatus());
                task.getPlannedSteps().get(0).setResult(t.getPlannedSteps().get(0).getResult());
            }
            return t;
        });
        when(plannerService.createPlan(any(Task.class), anyString())).thenReturn(plannedSteps);
        when(toolExecutorService.executeTool(eq("errorTool"), any())).thenThrow(new ToolExecutionException("Execution failed in tool"));

        // Act
        Task resultTask = taskOrchestrationService.processNewTask(taskRequestDTO);

        // Assert
        assertEquals(TaskStatus.FAILED, resultTask.getStatus());
        assertTrue(resultTask.getFinalResult().contains("Tool execution error for 'errorTool'"));
        Step failedStep = resultTask.getPlannedSteps().get(0);
        assertEquals(StepStatus.FAILED, failedStep.getStatus());
        assertTrue(failedStep.getResult().contains("Tool execution error: Execution failed in tool"));
    }

    @Test
    void processNewTask_toolExecutionReturnsFailureResult() throws PlanningException, ToolNotFoundException, ToolExecutionException {
        // Arrange
        Step step1 = new Step(task, "Step 1", "failingTool", Collections.emptyMap(), 1);
        step1.setId(10L);
        List<Step> plannedSteps = List.of(step1);

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            if (t.getId() == null) t.setId(1L);
            task.setStatus(t.getStatus());
            task.setPlannedSteps(t.getPlannedSteps().isEmpty() ? task.getPlannedSteps() : t.getPlannedSteps());
            task.setFinalResult(t.getFinalResult());
             if (!t.getPlannedSteps().isEmpty() && task.getPlannedSteps().get(0) != null && t.getPlannedSteps().get(0) != null) {
                task.getPlannedSteps().get(0).setStatus(t.getPlannedSteps().get(0).getStatus());
                task.getPlannedSteps().get(0).setResult(t.getPlannedSteps().get(0).getResult());
            }
            return t;
        });
        when(plannerService.createPlan(any(Task.class), anyString())).thenReturn(plannedSteps);
        when(toolExecutorService.executeTool(eq("failingTool"), any()))
            .thenReturn(new ToolExecutionResult("FAILURE", "Tool output on failure", "Tool specific error message"));

        // Act
        Task resultTask = taskOrchestrationService.processNewTask(taskRequestDTO);

        // Assert
        assertEquals(TaskStatus.FAILED, resultTask.getStatus());
        assertTrue(resultTask.getFinalResult().contains("failed: Tool specific error message"));
        Step failedStep = resultTask.getPlannedSteps().get(0);
        assertEquals(StepStatus.FAILED, failedStep.getStatus());
        assertTrue(failedStep.getResult().contains("Error: Tool specific error message Output: Tool output on failure"));
    }

    @Test
    void getTaskDetails_taskExists() {
        // Arrange
        Task existingTask = new Task("Existing Task");
        existingTask.setId(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));

        // Act
        Task foundTask = taskOrchestrationService.getTaskDetails(1L);

        // Assert
        assertNotNull(foundTask);
        assertEquals(1L, foundTask.getId());
    }

    @Test
    void getTaskDetails_taskNotFound() {
        // Arrange
        when(taskRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            taskOrchestrationService.getTaskDetails(2L);
        });
    }
}

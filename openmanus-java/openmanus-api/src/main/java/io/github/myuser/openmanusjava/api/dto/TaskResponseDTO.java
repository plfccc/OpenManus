package io.github.myuser.openmanusjava.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public class TaskResponseDTO {
    private Long id;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private List<StepDTO> plannedSteps;
    private String finalResult;
    // Could add links for HATEOAS if desired

    // Constructors, Getters, Setters
    public TaskResponseDTO() {}

    // Example full constructor
    public TaskResponseDTO(Long id, String description, String status, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime completedAt, List<StepDTO> plannedSteps, String finalResult) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
        this.plannedSteps = plannedSteps;
        this.finalResult = finalResult;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public List<StepDTO> getPlannedSteps() { return plannedSteps; }
    public void setPlannedSteps(List<StepDTO> plannedSteps) { this.plannedSteps = plannedSteps; }
    public String getFinalResult() { return finalResult; }
    public void setFinalResult(String finalResult) { this.finalResult = finalResult; }
}

package io.github.myuser.openmanusjava.core.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "om_tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Or GenerationType.UUID
    private Long id;

    @Column(nullable = false, length = 2000)
    private String description; // User's initial request

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("sequenceOrder ASC")
    private List<Step> plannedSteps = new ArrayList<>();

    @Column(length = 4000) // For storing final result or error summary
    private String finalResult;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TaskStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors, Getters, Setters
    public Task() {}

    public Task(String description) {
        this.description = description;
        this.status = TaskStatus.PENDING;
    }

    // Convenience method to add steps
    public void addStep(Step step) {
        plannedSteps.add(step);
        step.setTask(this);
    }

    // Getters and Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public List<Step> getPlannedSteps() { return plannedSteps; }
    public void setPlannedSteps(List<Step> plannedSteps) { this.plannedSteps = plannedSteps; }
    public String getFinalResult() { return finalResult; }
    public void setFinalResult(String finalResult) { this.finalResult = finalResult; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}

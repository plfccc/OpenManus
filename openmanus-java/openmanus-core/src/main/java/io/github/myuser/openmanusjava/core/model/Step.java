package io.github.myuser.openmanusjava.core.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "om_steps")
public class Step {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Or GenerationType.UUID for UUIDs
    private Long id;

    // private String taskId; // If Task is not an association but an ID reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String toolName;

    @ElementCollection(fetch = FetchType.EAGER) // Or a more complex mapping for parameters
    @CollectionTable(name = "om_step_parameters", joinColumns = @JoinColumn(name = "step_id"))
    @MapKeyColumn(name = "param_key", length = 255)
    @Column(name = "param_value", length = 2000) // Adjust length as needed
    private Map<String, String> parameters; // Simplified to String-String for now

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StepStatus status;

    @Column(length = 4000) // For storing execution result or error message
    private String result;

    private int sequenceOrder;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = StepStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors, Getters, Setters
    public Step() {}

    // Example constructor
    public Step(Task task, String description, String toolName, Map<String, String> parameters, int sequenceOrder) {
        this.task = task;
        this.description = description;
        this.toolName = toolName;
        this.parameters = parameters;
        this.sequenceOrder = sequenceOrder;
        this.status = StepStatus.PENDING;
    }

    // Getters and Setters... (generate them)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    public Map<String, String> getParameters() { return parameters; }
    public void setParameters(Map<String, String> parameters) { this.parameters = parameters; }
    public StepStatus getStatus() { return status; }
    public void setStatus(StepStatus status) { this.status = status; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public int getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(int sequenceOrder) { this.sequenceOrder = sequenceOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

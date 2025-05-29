package io.github.myuser.openmanusjava.api.dto;

import java.util.Map;

public class StepDTO {
    private Long id;
    private String description;
    private String toolName;
    private Map<String, String> parameters;
    private String status;
    private String result;
    private int sequenceOrder;

    // Constructors, Getters, Setters
    public StepDTO() {}

    public StepDTO(Long id, String description, String toolName, Map<String, String> parameters, String status, String result, int sequenceOrder) {
        this.id = id;
        this.description = description;
        this.toolName = toolName;
        this.parameters = parameters;
        this.status = status;
        this.result = result;
        this.sequenceOrder = sequenceOrder;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    public Map<String, String> getParameters() { return parameters; }
    public void setParameters(Map<String, String> parameters) { this.parameters = parameters; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public int getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(int sequenceOrder) { this.sequenceOrder = sequenceOrder; }
}

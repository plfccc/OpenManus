package io.github.myuser.openmanusjava.llm.dto;

public class LlmParameters {
    private String model;
    private Double temperature; // Use Double for flexibility, can be null
    private Integer maxTokens;  // Use Integer, can be null
    // Add other parameters as needed (e.g., stopSequences, topP)

    public LlmParameters() {}

    // Getters and Setters
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
}

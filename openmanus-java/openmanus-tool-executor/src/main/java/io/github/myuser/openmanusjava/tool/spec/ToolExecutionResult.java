package io.github.myuser.openmanusjava.tool.spec;

import java.util.HashMap;
import java.util.Map;

public class ToolExecutionResult {
    private String status; // e.g., "SUCCESS", "FAILURE"
    private Object output; // Primary output, could be String, Map, List, etc.
    private String error;  // Error message if status is FAILURE
    private Map<String, Object> attributes = new HashMap<>(); // For additional structured data

    public ToolExecutionResult(String status, Object output, String error) {
        this.status = status;
        this.output = output;
        this.error = error;
    }
     public ToolExecutionResult(String status, Object output) {
        this(status, output, null);
    }


    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Object getOutput() { return output; }
    public void setOutput(Object output) { this.output = output; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public Map<String, Object> getAttributes() { return attributes; }
    public void addAttribute(String key, Object value) { this.attributes.put(key, value); }
    public Object getAttribute(String key) { return this.attributes.get(key); }
}

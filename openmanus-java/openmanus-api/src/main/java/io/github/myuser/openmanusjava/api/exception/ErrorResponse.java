package io.github.myuser.openmanusjava.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL) // Don't include null fields in JSON
public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final int status;
    private final String error; // Short error description (e.g., HTTP status message)
    private final String message; // Detailed error message
    private final String path;
    private String errorCode; // Application-specific error code
    private List<String> details; // For validation errors or multiple error points

    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    // Getters
    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public String getErrorCode() { return errorCode; }
    public List<String> getDetails() { return details; }

    // Setters for optional fields
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public void setDetails(List<String> details) { this.details = details; }
}

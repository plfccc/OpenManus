package io.github.myuser.openmanusjava.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// While GlobalExceptionHandler will handle it, @ResponseStatus can be useful for clarity
// However, it's often better to let the handler define the response consistently.
// For now, let's not add @ResponseStatus here and rely on GlobalExceptionHandler.
public class ResourceNotFoundException extends OpenManusException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s not found with ID: %s", resourceType, resourceId));
    }
     public ResourceNotFoundException(String resourceType, Long resourceId) {
        super(String.format("%s not found with ID: %d", resourceType, resourceId));
    }
}

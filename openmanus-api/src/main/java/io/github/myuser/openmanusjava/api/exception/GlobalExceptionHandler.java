package io.github.myuser.openmanusjava.api.exception;

import io.github.myuser.openmanusjava.core.exception.OpenManusException;
import io.github.myuser.openmanusjava.core.exception.PlanningException;
import io.github.myuser.openmanusjava.core.exception.ResourceNotFoundException;
import io.github.myuser.openmanusjava.llm.exception.LlmInteractionException;
import io.github.myuser.openmanusjava.tool.exception.ToolNotFoundException;
import io.github.myuser.openmanusjava.tool.spec.ToolExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE) // Ensure this handler takes precedence
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private String getPath(WebRequest request) {
        if (request instanceof ServletWebRequest) {
            return ((ServletWebRequest) request).getRequest().getRequestURI();
        }
        return request.getDescription(false).replace("uri=", "");
    }

    // --- Specific Custom Exception Handlers ---

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        logger.warn("ResourceNotFoundException: {} (Path: {})", ex.getMessage(), getPath(request));
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage(),
            getPath(request)
        );
        errorResponse.setErrorCode("OM_RESOURCE_NOT_FOUND");
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PlanningException.class)
    public ResponseEntity<ErrorResponse> handlePlanningException(PlanningException ex, WebRequest request) {
        logger.error("PlanningException: {} (Path: {})", ex.getMessage(), getPath(request), ex);
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(), // Or a more specific 5xx code
            "Planning Error",
            "An error occurred during the task planning phase: " + ex.getMessage(),
            getPath(request)
        );
        errorResponse.setErrorCode("OM_PLANNING_ERROR");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(LlmInteractionException.class)
    public ResponseEntity<ErrorResponse> handleLlmInteractionException(LlmInteractionException ex, WebRequest request) {
        logger.error("LlmInteractionException: {} (Path: {})", ex.getMessage(), getPath(request), ex);
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.SERVICE_UNAVAILABLE.value(), // Or BAD_GATEWAY if we consider LLM external
            "LLM Interaction Error",
            "Failed to communicate with the LLM service: " + ex.getMessage(),
            getPath(request)
        );
        errorResponse.setErrorCode("OM_LLM_ERROR");
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(ToolNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleToolNotFoundException(ToolNotFoundException ex, WebRequest request) {
        logger.warn("ToolNotFoundException: {} (Path: {})", ex.getMessage(), getPath(request));
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(), // Or NOT_IMPLEMENTED if the tool is planned but not available
            "Tool Not Found",
            ex.getMessage(),
            getPath(request)
        );
        errorResponse.setErrorCode("OM_TOOL_NOT_FOUND");
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ToolExecutionException.class)
    public ResponseEntity<ErrorResponse> handleToolExecutionException(ToolExecutionException ex, WebRequest request) {
        logger.error("ToolExecutionException: {} (Path: {})", ex.getMessage(), getPath(request), ex);
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(), // Or a more specific 5xx if tool is external
            "Tool Execution Error",
            "An error occurred while executing a tool: " + ex.getMessage(),
            getPath(request)
        );
        errorResponse.setErrorCode("OM_TOOL_EXECUTION_ERROR");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // --- Standard Spring MVC Exception Handlers ---

    @ExceptionHandler(MethodArgumentNotValidException.class) // For @Valid validation failures on DTOs
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        List<String> details = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());

        logger.warn("MethodArgumentNotValidException: {} (Path: {})", String.join(", ", details), getPath(request));
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Validation Failed",
            getPath(request)
        );
        errorResponse.setErrorCode("OM_VALIDATION_ERROR");
        errorResponse.setDetails(details);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // --- Generic OpenManusException Handler (if specific ones are not caught) ---
    @ExceptionHandler(OpenManusException.class)
    public ResponseEntity<ErrorResponse> handleOpenManusException(OpenManusException ex, WebRequest request) {
        logger.error("OpenManusException: {} (Path: {})", ex.getMessage(), getPath(request), ex);
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(), // Default for unspecific app errors
            "Application Error",
            ex.getMessage(),
            getPath(request)
        );
        errorResponse.setErrorCode("OM_APPLICATION_ERROR");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    // --- Fallback Generic Exception Handler ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("Unhandled Exception: {} (Path: {})", ex.getMessage(), getPath(request), ex);
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "An unexpected internal server error occurred.",
            getPath(request)
        );
        errorResponse.setErrorCode("OM_UNEXPECTED_ERROR");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

package io.github.myuser.openmanusjava.core.exception;

public class PlanningException extends Exception {
    public PlanningException(String message) {
        super(message);
    }

    public PlanningException(String message, Throwable cause) {
        super(message, cause);
    }
}

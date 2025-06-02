package io.github.myuser.openmanusjava.core.exception;

public class OpenManusException extends RuntimeException { // Or extend Exception if checked is preferred for some
    public OpenManusException(String message) {
        super(message);
    }

    public OpenManusException(String message, Throwable cause) {
        super(message, cause);
    }
}

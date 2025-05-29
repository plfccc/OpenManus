package io.github.myuser.openmanusjava.llm.exception;

public class LlmInteractionException extends Exception {
    public LlmInteractionException(String message) {
        super(message);
    }

    public LlmInteractionException(String message, Throwable cause) {
        super(message, cause);
    }
}

package io.github.myuser.openmanusjava.core.model;

public enum StepStatus {
    PENDING,      // Step is awaiting execution
    IN_PROGRESS,  // Step is currently executing
    COMPLETED,    // Step completed successfully
    FAILED        // Step failed
}

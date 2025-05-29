package io.github.myuser.openmanusjava.core.model;

public enum TaskStatus {
    PENDING,      // Task received, awaiting planning
    PLANNING,     // Task plan is being generated
    EXECUTING,    // Task steps are being executed
    COMPLETED,    // Task finished successfully
    FAILED,       // Task failed during planning or execution
    CANCELED      // Task was canceled
}

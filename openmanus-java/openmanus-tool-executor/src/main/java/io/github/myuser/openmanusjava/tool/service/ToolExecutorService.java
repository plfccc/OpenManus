package io.github.myuser.openmanusjava.tool.service;

import io.github.myuser.openmanusjava.tool.spec.ToolExecutionResult;
import io.github.myuser.openmanusjava.tool.exception.ToolNotFoundException;
import io.github.myuser.openmanusjava.tool.spec.ToolExecutionException; // Re-use from tool.spec
import java.util.Map;

public interface ToolExecutorService {

    /**
     * Executes a tool identified by its name with the given parameters.
     * @param toolName The unique name of the tool to execute.
     * @param parameters A map of parameters for the tool.
     * @return The result of the tool execution.
     * @throws ToolNotFoundException if no tool with the given name is registered.
     * @throws ToolExecutionException if the tool itself encounters an error during execution.
     */
    ToolExecutionResult executeTool(String toolName, Map<String, Object> parameters)
        throws ToolNotFoundException, ToolExecutionException;
}

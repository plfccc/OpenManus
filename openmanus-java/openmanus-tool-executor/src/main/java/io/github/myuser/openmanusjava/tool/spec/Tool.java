package io.github.myuser.openmanusjava.tool.spec;

import java.util.List;
import java.util.Map;

public interface Tool {
    /**
     * Gets the unique name of the tool.
     * e.g., "browser.navigate", "code.executePython"
     */
    String getName();

    /**
     * Executes the tool with the given parameters.
     * @param parameters A map of parameter names to values.
     * @return The result of the tool execution.
     * @throws ToolExecutionException if execution fails.
     */
    ToolExecutionResult execute(Map<String, Object> parameters) throws ToolExecutionException;

    /**
     * Describes the parameters that this tool accepts.
     * @return A list of parameter definitions.
     */
    List<ToolParameterDefinition> getParameterDefinitions();
}

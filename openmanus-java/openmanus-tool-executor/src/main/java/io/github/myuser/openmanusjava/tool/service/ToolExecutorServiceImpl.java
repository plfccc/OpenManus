package io.github.myuser.openmanusjava.tool.service;

import io.github.myuser.openmanusjava.tool.exception.ToolNotFoundException;
import io.github.myuser.openmanusjava.tool.spec.Tool;
import io.github.myuser.openmanusjava.tool.spec.ToolExecutionException;
import io.github.myuser.openmanusjava.tool.spec.ToolExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class ToolExecutorServiceImpl implements ToolExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(ToolExecutorServiceImpl.class);
    private final ToolRegistry toolRegistry;

    @Autowired
    public ToolExecutorServiceImpl(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
        logger.info("ToolExecutorServiceImpl initialized.");
    }

    @Override
    public ToolExecutionResult executeTool(String toolName, Map<String, Object> parameters)
            throws ToolNotFoundException, ToolExecutionException {

        logger.info("Attempting to execute tool: '{}' with parameters: {}", toolName, parameters);

        Optional<Tool> toolOptional = toolRegistry.getTool(toolName);
        if (toolOptional.isEmpty()) {
            logger.error("Tool not found: {}", toolName);
            throw new ToolNotFoundException("Tool with name '" + toolName + "' not found in registry.");
        }

        Tool tool = toolOptional.get();
        logger.debug("Found tool '{}'. Proceeding with execution.", tool.getName());

        // Basic validation of parameters against tool.getParameterDefinitions() could go here.
        // This is a simplified version for now.

        try {
            ToolExecutionResult result = tool.execute(parameters);
            logger.info("Tool '{}' executed successfully. Status: {}", tool.getName(), result.getStatus());
            return result;
        } catch (ToolExecutionException e) {
            logger.error("ToolExecutionException from tool '{}': {}", tool.getName(), e.getMessage(), e);
            throw e; // Re-throw the specific exception
        } catch (Exception e) {
            // Catch any other unexpected exceptions from the tool's execute method
            logger.error("Unexpected exception during execution of tool '{}': {}", tool.getName(), e.getMessage(), e);
            throw new ToolExecutionException("Unexpected error during execution of tool '" + tool.getName() + "': " + e.getMessage(), e);
        }
    }
}

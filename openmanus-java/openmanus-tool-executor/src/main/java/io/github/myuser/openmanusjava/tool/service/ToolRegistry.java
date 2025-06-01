package io.github.myuser.openmanusjava.tool.service; // Or .registry

import io.github.myuser.openmanusjava.tool.spec.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

@Service
public class ToolRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ToolRegistry.class);
    private final Map<String, Tool> toolMap = new ConcurrentHashMap<>();

    /**
     * Constructs the ToolRegistry and auto-registers all Tool beans found in the application context.
     * @param tools A list of Tool implementations automatically injected by Spring.
     */
    @Autowired
    public ToolRegistry(List<Tool> tools) {
        if (tools == null || tools.isEmpty()) {
            logger.warn("No tools found for auto-registration.");
        } else {
            tools.forEach(this::registerTool);
        }
    }

    /**
     * Registers a single tool.
     * If a tool with the same name already exists, it will be replaced.
     * @param tool The tool to register.
     */
    public void registerTool(Tool tool) {
        if (tool == null || tool.getName() == null || tool.getName().trim().isEmpty()) {
            logger.warn("Attempted to register a null tool or a tool with an empty name.");
            return;
        }
        String toolName = tool.getName().toLowerCase(); // Normalize to lowercase
        logger.info("Registering tool: {}", toolName);
        toolMap.put(toolName, tool);
    }

    /**
     * Retrieves a tool by its name.
     * @param toolName The name of the tool (case-insensitive).
     * @return An Optional containing the tool if found, otherwise Optional.empty().
     */
    public Optional<Tool> getTool(String toolName) {
        if (toolName == null || toolName.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(toolMap.get(toolName.toLowerCase()));
    }

    /**
     * Returns an unmodifiable view of all registered tools.
     * @return A map of tool names to Tool instances.
     */
    public Map<String, Tool> getAllTools() {
        return Map.copyOf(toolMap); // Return an unmodifiable copy
    }
}

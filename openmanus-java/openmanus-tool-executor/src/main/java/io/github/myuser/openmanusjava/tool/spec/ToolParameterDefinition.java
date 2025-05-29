package io.github.myuser.openmanusjava.tool.spec;

public class ToolParameterDefinition {
    private String name;
    private String type; // e.g., "String", "Integer", "Boolean", "Map", "List"
    private String description;
    private boolean isRequired;

    public ToolParameterDefinition(String name, String type, String description, boolean isRequired) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.isRequired = isRequired;
    }

    // Getters
    public String getName() { return name; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public boolean isRequired() { return isRequired; }
}

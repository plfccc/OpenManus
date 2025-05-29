package io.github.myuser.openmanusjava.api.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class TaskRequestDTO {

    @NotBlank(message = "Task description cannot be blank.")
    @Size(min = 5, max = 2000, message = "Description must be between 5 and 2000 characters.")
    private String description;

    // Optional: priority, specific agent config, etc.
    // private String priority;

    public TaskRequestDTO() {}

    public TaskRequestDTO(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

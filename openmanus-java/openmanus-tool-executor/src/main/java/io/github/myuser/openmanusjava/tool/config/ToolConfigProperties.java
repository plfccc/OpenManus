package io.github.myuser.openmanusjava.tool.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@ConfigurationProperties(prefix = "openmanus.tools")
@Validated
public class ToolConfigProperties {

    private final CodeExecution codeExecution = new CodeExecution();
    private final Browser browser = new Browser();

    public CodeExecution getCodeExecution() {
        return codeExecution;
    }

    public Browser getBrowser() {
        return browser;
    }

    public static class CodeExecution {
        /**
         * Docker image to use for Python code execution.
         */
        @NotBlank(message = "Python Docker image cannot be blank")
        private String pythonDockerImage = "python:3.10-slim";

        /**
         * Default timeout in seconds for code execution.
         */
        @Positive(message = "Code execution timeout must be positive")
        private int defaultTimeoutSeconds = 60;

        // Getters and Setters
        public String getPythonDockerImage() {
            return pythonDockerImage;
        }
        public void setPythonDockerImage(String pythonDockerImage) {
            this.pythonDockerImage = pythonDockerImage;
        }
        public int getDefaultTimeoutSeconds() {
            return defaultTimeoutSeconds;
        }
        public void setDefaultTimeoutSeconds(int defaultTimeoutSeconds) {
            this.defaultTimeoutSeconds = defaultTimeoutSeconds;
        }
    }

    public static class Browser {
        /**
         * Whether to run the browser in headless mode by default.
         */
        private boolean defaultHeadless = true;

        /**
         * Default timeout in seconds for browser operations.
         */
        @Positive(message = "Browser operation timeout must be positive")
        private int defaultTimeoutSeconds = 30;

        // Getters and Setters
        public boolean isDefaultHeadless() {
            return defaultHeadless;
        }
        public void setDefaultHeadless(boolean defaultHeadless) {
            this.defaultHeadless = defaultHeadless;
        }
        public int getDefaultTimeoutSeconds() {
            return defaultTimeoutSeconds;
        }
        public void setDefaultTimeoutSeconds(int defaultTimeoutSeconds) {
            this.defaultTimeoutSeconds = defaultTimeoutSeconds;
        }
    }
}

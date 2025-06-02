package io.github.myuser.openmanusjava.tool.impl;

import io.github.myuser.openmanusjava.tool.config.ToolConfigProperties;
import io.github.myuser.openmanusjava.tool.spec.ToolExecutionResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.DockerClientFactory; // For checking Docker availability

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


@SpringBootTest(classes = {CodeExecutionTool.class, ToolConfigProperties.class})
class CodeExecutionToolIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(CodeExecutionToolIntegrationTest.class);

    @Autowired
    private CodeExecutionTool codeExecutionTool;

    @BeforeAll
    static void checkDocker() {
        boolean dockerAvailable = false;
        try {
            DockerClientFactory.instance().client(); // This will throw an exception if Docker is not available
            dockerAvailable = true;
            logger.info("Docker is available. CodeExecutionTool integration tests will run.");
        } catch (Exception e) {
            logger.warn("Docker is not available or not configured correctly. CodeExecutionTool integration tests will be skipped. Error: {}", e.getMessage());
        }
        // Set a system property that @EnabledIfSystemProperty can use
        System.setProperty("docker.available", String.valueOf(dockerAvailable));
    }

    @AfterAll
    static void cleanupSystemProperty() {
        System.clearProperty("docker.available");
    }


    @Test
    @EnabledIfSystemProperty(named = "docker.available", matches = "true")
    void executePythonScript_success() throws Exception {
        assumeTrue(Boolean.parseBoolean(System.getProperty("docker.available", "false")), "Docker not available, skipping test");

        String script = "print('Hello from Python!')\n" +
                        "import sys\n" +
                        "print('Python version:', sys.version)";
        Map<String, Object> parameters = Map.of(
            "language", "python",
            "script", script
        );

        ToolExecutionResult result = codeExecutionTool.execute(parameters);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus(), "Script execution should succeed.");
        assertTrue(result.getOutput().toString().contains("Hello from Python!"), "Stdout should contain expected output.");
        assertTrue(result.getOutput().toString().contains("Python version:"), "Stdout should contain Python version.");
        assertNull(result.getError(), "Error should be null on success.");
        assertEquals(0, result.getAttributes().get("exitCode"), "Exit code should be 0 on success.");
        logger.info("Python script stdout: {}", result.getOutput());
    }

    @Test
    @EnabledIfSystemProperty(named = "docker.available", matches = "true")
    void executePythonScript_withError() throws Exception {
         assumeTrue(Boolean.parseBoolean(System.getProperty("docker.available", "false")), "Docker not available, skipping test");

        String script = "print('About to error')\n" +
                        "raise ValueError('This is a test error')";
        Map<String, Object> parameters = Map.of(
            "language", "python",
            "script", script
        );

        ToolExecutionResult result = codeExecutionTool.execute(parameters);

        assertNotNull(result);
        assertEquals("FAILURE", result.getStatus(), "Script execution should fail.");
        assertNotNull(result.getError(), "Error message should be present on failure.");
        assertTrue(result.getError().contains("ValueError: This is a test error"), "Error message should contain the ValueError.");
        assertTrue(((String)result.getAttributes().get("stderr")).contains("ValueError: This is a test error"), "Stderr should contain the ValueError.");
        assertNotEquals(0, result.getAttributes().get("exitCode"), "Exit code should be non-zero on failure.");
        logger.info("Python script stderr: {}", result.getAttributes().get("stderr"));
    }
}

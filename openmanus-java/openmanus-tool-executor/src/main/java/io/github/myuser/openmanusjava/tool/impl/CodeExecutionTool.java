package io.github.myuser.openmanusjava.tool.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;

import io.github.myuser.openmanusjava.tool.config.ToolConfigProperties;
import io.github.myuser.openmanusjava.tool.spec.Tool;
import io.github.myuser.openmanusjava.tool.spec.ToolExecutionException;
import io.github.myuser.openmanusjava.tool.spec.ToolExecutionResult;
import io.github.myuser.openmanusjava.tool.spec.ToolParameterDefinition;

import org.apache.commons.io.FileUtils; // Added for directory cleanup
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component // Spring bean, will be registered by ToolRegistry
public class CodeExecutionTool implements Tool {

    private static final Logger logger = LoggerFactory.getLogger(CodeExecutionTool.class);
    private final ToolConfigProperties.CodeExecution codeExecConfig;
    private DockerClient dockerClient;

    public static final String LANG_PYTHON = "python";
    // Could add other languages later e.g. LANG_JAVASCRIPT

    @Autowired
    public CodeExecutionTool(ToolConfigProperties toolConfigProperties) {
        this.codeExecConfig = toolConfigProperties.getCodeExecution();
    }

    @PostConstruct
    public void initialize() {
        try {
            logger.info("Initializing Docker client for CodeExecutionTool...");
            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
            DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();
            dockerClient = DockerClientImpl.getInstance(config, httpClient);
            dockerClient.pingCmd().exec(); // Verify connection
            logger.info("Docker client initialized successfully. Version: {}", dockerClient.versionCmd().exec().getApiVersion());
        } catch (Exception e) {
            logger.error("Failed to initialize Docker client: {}. CodeExecutionTool will be non-functional.", e.getMessage(), e);
            dockerClient = null; // Ensure it's null if initialization fails
        }
    }

    @PreDestroy
    public void cleanup() {
        if (dockerClient != null) {
            try {
                dockerClient.close();
                logger.info("Docker client closed.");
            } catch (IOException e) {
                logger.error("Failed to close Docker client: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public String getName() {
        return "code.execute"; // Generic name, language specified in parameters
    }

    @Override
    public List<ToolParameterDefinition> getParameterDefinitions() {
        return Arrays.asList(
            new ToolParameterDefinition("language", "String", "The programming language of the script (e.g., 'python').", true),
            new ToolParameterDefinition("script", "String", "The script content to execute.", true),
            new ToolParameterDefinition("inputData", "String", "Input data as a string/JSON to be passed to the script (e.g., via stdin or a file). (Optional)", false),
            new ToolParameterDefinition("timeoutSeconds", "Integer", "Execution timeout in seconds. (Optional, defaults to config)", false)
        );
    }

    @Override
    public ToolExecutionResult execute(Map<String, Object> parameters) throws ToolExecutionException {
        if (dockerClient == null) {
            throw new ToolExecutionException("Docker client is not initialized. Cannot execute code.");
        }

        String language = (String) parameters.get("language");
        String scriptContent = (String) parameters.get("script");
        String inputData = (String) parameters.get("inputData"); // Optional
        int timeout = ((Number) parameters.getOrDefault("timeoutSeconds", codeExecConfig.getDefaultTimeoutSeconds())).intValue();

        if (scriptContent == null || scriptContent.trim().isEmpty()) {
            throw new ToolExecutionException("Missing required parameter: 'script'");
        }
        if (language == null || !LANG_PYTHON.equalsIgnoreCase(language)) { // Currently only support python
            throw new ToolExecutionException("Unsupported language: " + language + ". Only 'python' is currently supported.");
        }

        Path hostTempDir = null;
        String containerId = null;
        String uniqueExecutionId = UUID.randomUUID().toString().substring(0, 8);
        String containerScriptPath = "/sandbox/script_to_execute.py";
        String containerInputPath = "/sandbox/input_data.txt"; // If using input file

        try {
            hostTempDir = Files.createTempDirectory("openmanus_code_exec_" + uniqueExecutionId + "_");
            Path hostScriptFile = hostTempDir.resolve("script_to_execute.py");
            Files.writeString(hostScriptFile, scriptContent, StandardCharsets.UTF_8);

            if (inputData != null) {
                Path hostInputFile = hostTempDir.resolve("input_data.txt");
                Files.writeString(hostInputFile, inputData, StandardCharsets.UTF_8);
            }

            logger.info("Executing Python script in Docker container. Host temp dir: {}", hostTempDir);

            HostConfig hostConfig = new HostConfig()
                .withBinds(new Bind(hostTempDir.toAbsolutePath().toString(), new Volume("/sandbox")))
                .withNetworkMode("none") // Security: Disable network unless specifically required and configured
                .withReadonlyRootfs(true); // Security Hardening: Mount root FS as read-only

            CreateContainerResponse containerResponse = dockerClient.createContainerCmd(codeExecConfig.getPythonDockerImage())
                .withHostConfig(hostConfig)
                .withWorkingDir("/sandbox")
                .withCmd("python", containerScriptPath) // Command to run
                .withAttachStdin(inputData != null) // Attach stdin only if inputData is provided
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withTty(false) // No TTY for non-interactive script
                .exec();
            containerId = containerResponse.getId();

            dockerClient.startContainerCmd(containerId).exec();

            // If inputData is provided, write it to container's stdin - this is complex with docker-java
            // A more common approach is to mount inputData as a file, which we did above.
            // Script will read from /sandbox/input_data.txt

            WaitContainerResultCallback waitCallback = new WaitContainerResultCallback();
            Integer exitCode = dockerClient.waitContainerCmd(containerId)
                .exec(waitCallback)
                .awaitStatusCode(timeout, TimeUnit.SECONDS); // Wait for completion with timeout

            final StringBuilder logsStdout = new StringBuilder();
            final StringBuilder logsStderr = new StringBuilder();
            LogContainerResultCallback logCallback = new LogContainerResultCallback() {
                @Override
                public void onNext(Frame item) {
                    if (StreamType.STDOUT.equals(item.getStreamType())) {
                        logsStdout.append(new String(item.getPayload(), StandardCharsets.UTF_8));
                    } else if (StreamType.STDERR.equals(item.getStreamType())) {
                        logsStderr.append(new String(item.getPayload(), StandardCharsets.UTF_8));
                    }
                }
            };
            dockerClient.logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withTimestamps(false) // Don't include timestamps in log lines from Docker
                .exec(logCallback)
                .awaitCompletion(5, TimeUnit.SECONDS); // Timeout for log collection


            ToolExecutionResult result = new ToolExecutionResult(
                (exitCode == 0 ? "SUCCESS" : "FAILURE"),
                logsStdout.toString() // Primary output is stdout
            );
            result.addAttribute("stdout", logsStdout.toString());
            result.addAttribute("stderr", logsStderr.toString());
            result.addAttribute("exitCode", exitCode);

            if (exitCode != 0) {
                result.setError("Script exited with code: " + exitCode + ". Stderr: " + logsStderr);
                logger.warn("Code execution failed. Exit code: {}, Stderr: {}", exitCode, logsStderr);
            } else {
                logger.info("Code execution successful. Stdout length: {}", logsStdout.length());
            }
            return result;

        } catch (IOException e) {
            logger.error("IOException during code execution setup/file handling: {}", e.getMessage(), e);
            throw new ToolExecutionException("File I/O error during code execution: " + e.getMessage(), e);
        } catch (com.github.dockerjava.api.exception.DockerException e) {
             logger.error("DockerException during code execution: {}", e.getMessage(), e);
            throw new ToolExecutionException("Docker operation failed during code execution: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error("Code execution interrupted (likely timeout): {}", e.getMessage(), e);
            Thread.currentThread().interrupt(); // Restore interrupted status
            throw new ToolExecutionException("Code execution timed out or was interrupted.", e);
        }
        finally {
            if (containerId != null) {
                try {
                    dockerClient.removeContainerCmd(containerId).withForce(true).withRemoveVolumes(true).exec();
                    logger.debug("Removed container: {}", containerId);
                } catch (com.github.dockerjava.api.exception.DockerException e) {
                    logger.warn("Failed to remove container {}: {}", containerId, e.getMessage());
                }
            }
            if (hostTempDir != null) {
                try {
                    FileUtils.deleteDirectory(hostTempDir.toFile()); // Use Apache Commons IO for robust deletion
                    logger.debug("Deleted host temp directory: {}", hostTempDir);
                } catch (IOException e) {
                    logger.warn("Failed to delete host temp directory {}: {}", hostTempDir, e.getMessage());
                }
            }
        }
    }
}

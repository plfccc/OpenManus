package io.github.myuser.openmanusjava.llm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@ConfigurationProperties(prefix = "openmanus.llm")
@Validated // Enable validation on these properties
public class LlmConfigProperties {

    /**
     * The LLM provider to use (e.g., "openai", "anthropic", "mock").
     */
    @NotBlank(message = "LLM provider cannot be blank")
    private String provider = "mock"; // Default to mock initially

    /**
     * API key for the LLM provider.
     * Should be externalized in production (e.g., via environment variable).
     * Example: OPENMANUS_LLM_API_KEY
     */
    private String apiKey; // Not @NotBlank to allow it to be optional if provider is "mock"

    /**
     * Base URL for the LLM API.
     */
    private String baseUrl; // e.g., https://api.openai.com/v1

    /**
     * Default LLM model to use if not specified per request.
     */
    @NotBlank(message = "Default LLM model cannot be blank")
    private String defaultModel = "default-mock-model";

    /**
     * Connection and request timeout in seconds for LLM API calls.
     */
    @Positive(message = "LLM timeout must be positive")
    private int timeoutSeconds = 60;

    // Getters and Setters
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}

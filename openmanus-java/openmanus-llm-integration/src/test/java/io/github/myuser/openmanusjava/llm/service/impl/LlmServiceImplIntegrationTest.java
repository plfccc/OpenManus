package io.github.myuser.openmanusjava.llm.service.impl;

import io.github.myuser.openmanusjava.llm.config.LlmConfigProperties;
import io.github.myuser.openmanusjava.llm.dto.ChatMessage;
import io.github.myuser.openmanusjava.llm.dto.LlmParameters;
import io.github.myuser.openmanusjava.llm.exception.LlmInteractionException;
import io.github.myuser.openmanusjava.llm.service.LlmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper; // Required by LlmServiceImpl constructor

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


@SpringBootTest(classes = {LlmServiceImpl.class, LlmConfigProperties.class, ObjectMapper.class})
@ActiveProfiles("test")
public class LlmServiceImplIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(LlmServiceImplIntegrationTest.class);

    @Autowired
    private LlmService llmService;

    @Autowired
    private LlmConfigProperties configProperties;

    private String openAiApiKey;

    @BeforeEach
    void setUp() {
        openAiApiKey = System.getenv("OPENAI_API_KEY");
        // If OPENAI_API_KEY is not set via env, LlmConfigProperties might pick it up from application-test.properties
        // if it's configured there (e.g., openmanus.llm.api-key=${OPENAI_API_KEY_FROM_PROPERTIES})
        // For this test, we primarily rely on the OPENAI_API_KEY env var for the actual key.
        if (openAiApiKey == null || openAiApiKey.trim().isEmpty()) {
            if (configProperties != null && "openai".equalsIgnoreCase(configProperties.getProvider())) {
                 openAiApiKey = configProperties.getApiKey();
            }
        }
        logger.info("OpenAI API Key for test: {}", (openAiApiKey != null && !openAiApiKey.isEmpty() ? "Present" : "Not found/empty"));
        logger.info("Configured LLM provider for test: {}", configProperties != null ? configProperties.getProvider() : "N/A");
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY_INTEGRATION_TEST", matches = ".+")
    void testGenerateText_withRealOpenAI_whenApiKeyPresentAndConfigured() throws LlmInteractionException {
        assumeTrue(openAiApiKey != null && !openAiApiKey.isEmpty(), "OpenAI API key not found or empty. Skipping real API call test.");
        // Ensure LlmServiceImpl is configured to use "openai" provider for this test to run against real API
        // This might require setting openmanus.llm.provider=openai in application-test.properties or via test setup
        assumeTrue("openai".equalsIgnoreCase(configProperties.getProvider()), "LLM provider is not 'openai' in config. Skipping real API call test. Provider is: " + configProperties.getProvider());

        LlmParameters params = new LlmParameters();
        params.setModel(configProperties.getDefaultModel());
        if (params.getModel() == null || params.getModel().contains("mock")) {
            params.setModel("gpt-3.5-turbo");
        }
        params.setMaxTokens(50);
        params.setTemperature(0.7);

        String prompt = "Translate the following English text to French: 'Hello, world!'";
        logger.info("Sending real request to OpenAI (generateText): '{}' with model {}", prompt, params.getModel());

        String response = llmService.generateText(prompt, params);

        assertNotNull(response, "Response from OpenAI should not be null.");
        assertFalse(response.trim().isEmpty(), "Response from OpenAI should not be empty.");
        logger.info("Received real response from OpenAI (generateText): {}", response);
        assertTrue(response.toLowerCase().contains("bonjour") || response.toLowerCase().contains("salut"),
           "Response should contain a French greeting. Actual: " + response);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY_INTEGRATION_TEST", matches = ".+")
    void testChat_withRealOpenAI_whenApiKeyPresentAndConfigured() throws LlmInteractionException {
        assumeTrue(openAiApiKey != null && !openAiApiKey.isEmpty(), "OpenAI API key not found or empty. Skipping real API call test.");
        assumeTrue("openai".equalsIgnoreCase(configProperties.getProvider()), "LLM provider is not 'openai' in config. Skipping real API call test. Provider is: " + configProperties.getProvider());


        LlmParameters params = new LlmParameters();
        params.setModel(configProperties.getDefaultModel());
         if (params.getModel() == null || params.getModel().contains("mock")) {
            params.setModel("gpt-3.5-turbo");
        }
        params.setMaxTokens(50);
        params.setTemperature(0.7);

        List<ChatMessage> messages = Collections.singletonList(
            new ChatMessage("user", "What is the capital of France?")
        );
        logger.info("Sending real request to OpenAI (chat): '{}' with model {}", messages.get(0).getContent(), params.getModel());

        String response = llmService.chat(messages, params);

        assertNotNull(response, "Chat response from OpenAI should not be null.");
        assertFalse(response.trim().isEmpty(), "Chat response from OpenAI should not be empty.");
        logger.info("Received real response from OpenAI (chat): {}", response);
        assertTrue(response.toLowerCase().contains("paris"), "Response should contain 'Paris'. Actual: " + response);
    }

    @Test
    void testGenerateText_withMockProvider_whenConfigured() throws LlmInteractionException {
        // This test relies on the application-test.properties setting the provider to "mock"
        // or the absence of OPENAI_API_KEY_INTEGRATION_TEST env var.
        if (!"mock".equalsIgnoreCase(configProperties.getProvider())) {
             logger.warn("Skipping mock test as provider is not 'mock' (it's '{}'). This test is intended for mock configuration.", configProperties.getProvider());
             assumeTrue(false, "LLM provider is not 'mock'. This test is intended for mock configuration.");
        }

        LlmParameters params = new LlmParameters();
        params.setModel("mock-model-for-plan"); // Explicitly use a mock model
        String prompt = "This is a test prompt for mock plan.";
        logger.info("Sending request to MOCK LlmService (generateText): '{}'", prompt);
        String response = llmService.generateText(prompt, params);
        assertNotNull(response);
        assertTrue(response.contains("Mock Step 1"), "Mocked response should contain plan steps.");
        logger.info("Received mocked plan as expected: {}", response);
    }
}

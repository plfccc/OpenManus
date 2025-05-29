package io.github.myuser.openmanusjava.llm.service.impl;

import io.github.myuser.openmanusjava.llm.config.LlmConfigProperties;
import io.github.myuser.openmanusjava.llm.dto.ChatMessage;
import io.github.myuser.openmanusjava.llm.dto.LlmParameters;
import io.github.myuser.openmanusjava.llm.exception.LlmInteractionException;
import io.github.myuser.openmanusjava.llm.service.LlmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LlmServiceImpl implements LlmService {

    private static final Logger logger = LoggerFactory.getLogger(LlmServiceImpl.class);
    private final LlmConfigProperties configProperties;

    @Autowired
    public LlmServiceImpl(LlmConfigProperties configProperties) {
        this.configProperties = configProperties;
        logger.info("LlmServiceImpl initialized with provider: {} and model: {}",
            configProperties.getProvider(), configProperties.getDefaultModel());
        if ("mock".equalsIgnoreCase(configProperties.getProvider())) {
            logger.warn("LLM Service is running in MOCK mode. No actual API calls will be made.");
        }
    }

    @Override
    public String generateText(String prompt, LlmParameters params) throws LlmInteractionException {
        logger.info("generateText called with prompt: '{}', params: (model: {}, temp: {})",
            prompt, params != null ? params.getModel() : "N/A", params !=null ? params.getTemperature() : "N/A");

        if ("mock".equalsIgnoreCase(configProperties.getProvider())) {
            // Simulate a plan generation for now, this will be used by PlannerService
            // This mocked response should be a JSON string representing a list of steps.
            String mockedPlanJson = "[\n" +
                "  {\n" +
                "    \"description\": \"Mock Step 1: Navigate to example.com\",\n" +
                "    \"toolName\": \"browser.action\",\n" +
                "    \"parameters\": {\"action\": \"NAVIGATE\", \"url\": \"https://example.com\"}\n" +
                "  },\n" +
                "  {\n" +
                "    \"description\": \"Mock Step 2: Extract text from body\",\n" +
                "    \"toolName\": \"browser.action\",\n" +
                "    \"parameters\": {\"action\": \"EXTRACT_TEXT\", \"selector\": \"body\"}\n" +
                "  },\n" +
                "  {\n" +
                "    \"description\": \"Mock Step 3: Summarize extracted text (mocked)\",\n" +
                "    \"toolName\": \"llm.ask\",\n" +
                "    \"parameters\": {\"question\": \"Summarize the following text: ${previous_step_output}\"}\n" +
                "  }\n" +
                "]";
            logger.info("Returning mocked plan JSON response for prompt: {}", prompt);
            return mockedPlanJson;
        } else {
            // Actual LLM client implementation will go here in a future step
            logger.error("Actual LLM provider '{}' not implemented yet in LlmServiceImpl.", configProperties.getProvider());
            throw new LlmInteractionException("LLM provider '" + configProperties.getProvider() + "' not implemented yet.");
        }
    }

    @Override
    public String chat(List<ChatMessage> messages, LlmParameters params) throws LlmInteractionException {
        logger.info("chat called with {} messages, params: (model: {}, temp: {})",
            messages.size(), params != null ? params.getModel() : "N/A", params != null ? params.getTemperature() : "N/A");

        if ("mock".equalsIgnoreCase(configProperties.getProvider())) {
            String responseContent = "This is a mocked chat response from " +
                                     (params != null && params.getModel() != null ? params.getModel() : configProperties.getDefaultModel()) +
                                     ". Query was: '" + messages.get(messages.size()-1).getContent() + "'";
            logger.info("Returning mocked chat response: {}", responseContent);
            return responseContent;
        } else {
            logger.error("Actual LLM provider '{}' not implemented yet for chat in LlmServiceImpl.", configProperties.getProvider());
            throw new LlmInteractionException("LLM provider '" + configProperties.getProvider() + "' chat not implemented yet.");
        }
    }
}

package io.github.myuser.openmanusjava.llm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
// Using FQN for com.theokanning.openai.completion.chat.ChatMessage will be done in the code where it's used.
import com.theokanning.openai.service.OpenAiService;

import io.github.myuser.openmanusjava.llm.config.LlmConfigProperties;
import io.github.myuser.openmanusjava.llm.dto.ChatMessage; // Our DTO
import io.github.myuser.openmanusjava.llm.dto.LlmParameters;
import io.github.myuser.openmanusjava.llm.exception.LlmInteractionException;
import io.github.myuser.openmanusjava.llm.service.LlmService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LlmServiceImpl implements LlmService {

    private static final Logger logger = LoggerFactory.getLogger(LlmServiceImpl.class);
    private final LlmConfigProperties configProperties;
    private final ObjectMapper objectMapper; // For parsing our plan JSON, if needed
    private OpenAiService openAiService; // OpenAI client

    @Autowired
    public LlmServiceImpl(LlmConfigProperties configProperties, ObjectMapper objectMapper) {
        this.configProperties = configProperties;
        this.objectMapper = objectMapper;

        logger.info("LlmServiceImpl initializing with provider: {} and model: {}",
            configProperties.getProvider(), configProperties.getDefaultModel());

        if ("openai".equalsIgnoreCase(configProperties.getProvider())) {
            if (configProperties.getApiKey() == null || configProperties.getApiKey().trim().isEmpty()) {
                logger.error("OpenAI API key is not configured. Real OpenAI calls will fail.");
                // Not throwing an exception here, to allow app to start, but calls will fail.
            } else {
                Duration timeout = Duration.ofSeconds(configProperties.getTimeoutSeconds());
                // The OpenAiService constructor uses createDefaultClient which already configures Retrofit.
                // It applies the timeout to OkHttpClient used by Retrofit.
                this.openAiService = new OpenAiService(configProperties.getApiKey(), timeout);
                logger.info("OpenAI client initialized with timeout: {} seconds.", configProperties.getTimeoutSeconds());
            }
        } else if ("mock".equalsIgnoreCase(configProperties.getProvider())) {
            logger.warn("LLM Service is running in MOCK mode. No actual API calls will be made.");
        } else {
            logger.warn("Unsupported LLM provider configured: {}. LlmService may not function correctly.", configProperties.getProvider());
        }
    }

    @Override
    public String generateText(String prompt, LlmParameters params) throws LlmInteractionException {
        String effectiveModel = (params != null && params.getModel() != null) ? params.getModel() : configProperties.getDefaultModel();
        Double effectiveTemperature = (params != null && params.getTemperature() != null) ? params.getTemperature() : null;
        Integer effectiveMaxTokens = (params != null && params.getMaxTokens() != null) ? params.getMaxTokens() : null;

        logger.info("generateText called for provider '{}'. Prompt: '{}...', Params: (model: {}, temp: {}, maxTokens: {})",
            configProperties.getProvider(),
            prompt.length() > 100 ? prompt.substring(0,100) : prompt,
            effectiveModel,
            effectiveTemperature,
            effectiveMaxTokens);

        if ("openai".equalsIgnoreCase(configProperties.getProvider())) {
            if (openAiService == null) {
                throw new LlmInteractionException("OpenAI client not initialized. Check API key configuration.");
            }
            try {
                List<com.theokanning.openai.completion.chat.ChatMessage> messages = List.of(
                    new com.theokanning.openai.completion.chat.ChatMessage("user", prompt)
                );

                ChatCompletionRequest.ChatCompletionRequestBuilder requestBuilder = ChatCompletionRequest.builder()
                    .messages(messages)
                    .model(effectiveModel);

                if (effectiveTemperature != null) {
                    requestBuilder.temperature(effectiveTemperature);
                }
                if (effectiveMaxTokens != null) {
                    requestBuilder.maxTokens(effectiveMaxTokens);
                }

                ChatCompletionRequest request = requestBuilder.build();
                logger.debug("OpenAI ChatCompletionRequest (generateText): {}", request);

                com.theokanning.openai.completion.chat.ChatMessage responseMessage = openAiService.createChatCompletion(request)
                    .getChoices().get(0).getMessage();

                logger.info("Received response from OpenAI for generateText.");
                return responseMessage.getContent();

            } catch (Exception e) {
                logger.error("Error interacting with OpenAI for generateText: {}", e.getMessage(), e);
                throw new LlmInteractionException("OpenAI API call failed for generateText: " + e.getMessage(), e);
            }
        } else if ("mock".equalsIgnoreCase(configProperties.getProvider())) {
            String mockedPlanJson = "[\n" +
                "  {\n" +
                "    \"description\": \"Mock Step 1 (LLM provider is 'mock'): Navigate to example.com\",\n" +
                "    \"toolName\": \"browser.action\",\n" +
                "    \"parameters\": {\"action\": \"NAVIGATE\", \"url\": \"https://example.com\"}\n" +
                "  },\n" +
                "  {\n" +
                "    \"description\": \"Mock Step 2 (LLM provider is 'mock'): Extract text from body\",\n" +
                "    \"toolName\": \"browser.action\",\n" +
                "    \"parameters\": {\"action\": \"EXTRACT_TEXT\", \"selector\": \"body\"}\n" +
                "  }\n" +
                "]";
            logger.info("Returning mocked plan JSON response because provider is 'mock'.");
            return mockedPlanJson;
        } else {
            logger.error("Unsupported LLM provider '{}' for generateText.", configProperties.getProvider());
            throw new LlmInteractionException("Unsupported LLM provider: " + configProperties.getProvider());
        }
    }

    @Override
    public String chat(List<ChatMessage> messages, LlmParameters params) throws LlmInteractionException {
        String effectiveModel = (params != null && params.getModel() != null) ? params.getModel() : configProperties.getDefaultModel();
        Double effectiveTemperature = (params != null && params.getTemperature() != null) ? params.getTemperature() : null;
        Integer effectiveMaxTokens = (params != null && params.getMaxTokens() != null) ? params.getMaxTokens() : null;

        logger.info("chat called for provider '{}'. Messages count: {}, Params: (model: {}, temp: {}, maxTokens: {})",
            configProperties.getProvider(), messages.size(),
            effectiveModel,
            effectiveTemperature,
            effectiveMaxTokens);

        if ("openai".equalsIgnoreCase(configProperties.getProvider())) {
             if (openAiService == null) {
                throw new LlmInteractionException("OpenAI client not initialized. Check API key configuration.");
            }
            try {
                List<com.theokanning.openai.completion.chat.ChatMessage> openAiMessages = messages.stream()
                    .map(m -> new com.theokanning.openai.completion.chat.ChatMessage(m.getRole(), m.getContent()))
                    .collect(Collectors.toList());

                ChatCompletionRequest.ChatCompletionRequestBuilder requestBuilder = ChatCompletionRequest.builder()
                    .messages(openAiMessages)
                    .model(effectiveModel);

                if (effectiveTemperature != null) {
                    requestBuilder.temperature(effectiveTemperature);
                }
                if (effectiveMaxTokens != null) {
                    requestBuilder.maxTokens(effectiveMaxTokens);
                }

                ChatCompletionRequest request = requestBuilder.build();
                logger.debug("OpenAI ChatCompletionRequest (chat): {}", request);

                com.theokanning.openai.completion.chat.ChatMessage responseMessage = openAiService.createChatCompletion(request)
                    .getChoices().get(0).getMessage();

                logger.info("Received response from OpenAI for chat.");
                return responseMessage.getContent();

            } catch (Exception e) {
                logger.error("Error interacting with OpenAI for chat: {}", e.getMessage(), e);
                throw new LlmInteractionException("OpenAI API call failed for chat: " + e.getMessage(), e);
            }
        } else if ("mock".equalsIgnoreCase(configProperties.getProvider())) {
            String lastUserMessage = messages.isEmpty() ? "No message provided" : messages.get(messages.size()-1).getContent();
            String responseContent = "This is a MOCKED chat response from " +
                                     effectiveModel +
                                     ". Last query was: '" + lastUserMessage + "'";
            logger.info("Returning mocked chat response because provider is 'mock'.");
            return responseContent;
        } else {
            logger.error("Unsupported LLM provider '{}' for chat.", configProperties.getProvider());
            throw new LlmInteractionException("Unsupported LLM provider for chat: " + configProperties.getProvider());
        }
    }
}

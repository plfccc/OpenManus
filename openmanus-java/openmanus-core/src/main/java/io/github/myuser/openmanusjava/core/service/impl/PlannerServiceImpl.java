package io.github.myuser.openmanusjava.core.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.myuser.openmanusjava.core.exception.PlanningException;
import io.github.myuser.openmanusjava.core.model.Step;
import io.github.myuser.openmanusjava.core.model.Task;
import io.github.myuser.openmanusjava.core.service.PlannerService;
import io.github.myuser.openmanusjava.llm.dto.LlmParameters;
import io.github.myuser.openmanusjava.llm.exception.LlmInteractionException;
import io.github.myuser.openmanusjava.llm.service.LlmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
// import java.util.stream.Collectors; // Not currently used

@Service
public class PlannerServiceImpl implements PlannerService {

    private static final Logger logger = LoggerFactory.getLogger(PlannerServiceImpl.class);
    private final LlmService llmService;
    private final ObjectMapper objectMapper; // For parsing the JSON plan from LLM

    @Autowired
    public PlannerServiceImpl(LlmService llmService, ObjectMapper objectMapper) {
        this.llmService = llmService;
        this.objectMapper = objectMapper;
        logger.info("PlannerServiceImpl initialized");
    }

    @Override
    public List<Step> createPlan(Task task, String userGoal) throws PlanningException {
        logger.info("Creating plan for task ID {} with goal: {}", task.getId(), userGoal);

        // Construct a prompt for the LLM (even if mocked, this shows the intent)
        String prompt = "Given the user goal: '" + userGoal + "', " +
                        "break it down into a series of executable steps. " +
                        "Each step should specify a toolName and its parameters. " +
                        "Respond with a JSON list of steps. Available tools: [browser.action, code.executePython, llm.ask].";

        LlmParameters params = new LlmParameters();
        // Potentially set a specific model for planning if LlmConfigProperties supports it
        // params.setModel("planning-model");

        try {
            String llmResponseJson = llmService.generateText(prompt, params);
            logger.debug("Received JSON plan from LlmService: {}", llmResponseJson);

            // Parse the JSON response into a list of Step-like structures
            // Using a temporary DTO or Map structure for parsing before converting to Step entities
            List<Map<String, Object>> rawSteps = objectMapper.readValue(llmResponseJson, new TypeReference<List<Map<String, Object>>>() {});

            if (rawSteps == null || rawSteps.isEmpty()) {
                logger.warn("LLM returned an empty or null plan for task ID {}.", task.getId());
                throw new PlanningException("LLM returned an empty or null plan.");
            }

            List<Step> plannedSteps = new ArrayList<>();
            int sequenceOrder = 1;
            for (Map<String, Object> rawStep : rawSteps) {
                String description = (String) rawStep.get("description");
                String toolName = (String) rawStep.get("toolName");
                @SuppressWarnings("unchecked") // Assuming parameters is always Map<String, String> from JSON
                Map<String, String> stepParameters = (Map<String, String>) rawStep.get("parameters");

                if (description == null || toolName == null) {
                    logger.error("Invalid step structure from LLM for task ID {}: {}", task.getId(), rawStep);
                    throw new PlanningException("LLM returned an invalid step structure (missing description or toolName).");
                }

                Step step = new Step(task, description, toolName, stepParameters, sequenceOrder++);
                plannedSteps.add(step);
            }

            logger.info("Successfully created {} steps for task ID {}", plannedSteps.size(), task.getId());
            return plannedSteps;

        } catch (LlmInteractionException e) {
            logger.error("LlmInteractionException while creating plan for task ID {}: {}", task.getId(), e.getMessage(), e);
            throw new PlanningException("Failed to interact with LLM during planning: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("IOException while parsing LLM plan response for task ID {}: {}", task.getId(), e.getMessage(), e);
            throw new PlanningException("Failed to parse plan from LLM response: " + e.getMessage(), e);
        }
    }
}

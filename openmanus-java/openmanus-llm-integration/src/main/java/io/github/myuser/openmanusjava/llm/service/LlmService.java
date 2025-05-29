package io.github.myuser.openmanusjava.llm.service;

import io.github.myuser.openmanusjava.llm.dto.LlmParameters;
import io.github.myuser.openmanusjava.llm.dto.ChatMessage;
import io.github.myuser.openmanusjava.llm.exception.LlmInteractionException; // Corrected import
import java.util.List;

public interface LlmService {

    /**
     * Generates text based on a single prompt.
     * @param prompt The input prompt.
     * @param params LLM parameters (e.g., model, temperature).
     * @return The generated text.
     * @throws LlmInteractionException if interaction with LLM fails.
     */
    String generateText(String prompt, LlmParameters params) throws LlmInteractionException;

    /**
     * Engages in a chat-like conversation with the LLM.
     * @param messages A list of chat messages forming the conversation history.
     * @param params LLM parameters.
     * @return The LLM's response message.
     * @throws LlmInteractionException if interaction with LLM fails.
     */
    String chat(List<ChatMessage> messages, LlmParameters params) throws LlmInteractionException;

    // Potentially:
    // List<Embedding> embed(List<String> texts, LlmParameters params) throws LlmInteractionException;
}

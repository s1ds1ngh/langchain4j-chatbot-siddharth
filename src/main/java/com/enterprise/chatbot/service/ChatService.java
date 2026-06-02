package com.enterprise.chatbot.service;

import com.enterprise.chatbot.model.ChatRequest;
import com.enterprise.chatbot.model.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * ChatService — Orchestration, Validation & Error Handling layer.
 *
 * Responsibilities:
 *   1. Validate incoming user input before calling the AI.
 *   2. Delegate to AssistantService for AI response generation.
 *   3. Catch any AI failures and return a graceful fallback response.
 *   4. Support a simulated failure mode for demonstrating error handling.
 *
 * Error Handling Strategy:
 *   - Empty / null messages   → 400-style validation error (never hits AI).
 *   - AI model exception      → Caught here, returns ChatResponse.error().
 *   - Simulated failure mode  → Throws intentionally to demo the fallback.
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    /** Maximum allowed message length (characters) */
    private static final int MAX_MESSAGE_LENGTH = 2000;

    /** Minimum meaningful message length */
    private static final int MIN_MESSAGE_LENGTH = 1;

    private final AssistantService assistantService;

    public ChatService(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Processes a normal chat request with full validation and error handling.
     *
     * @param request The validated chat request DTO.
     * @return ChatResponse — always returns a response (never throws to caller).
     */
    public ChatResponse chat(ChatRequest request) {
        String sessionId = request.getSessionId();
        String message   = request.getMessage();

        log.info("Processing chat | session={} | messageLength={}", sessionId, message.length());

        // ── Step 1: Additional business-layer validation ───────────────────────
        // (Bean Validation on the DTO handles @NotBlank and @Size,
        //  but we add domain-specific checks here.)
        String validationError = validateMessage(message);
        if (validationError != null) {
            log.warn("Validation failed | session={} | reason={}", sessionId, validationError);
            return ChatResponse.error(validationError, sessionId);
        }

        // ── Step 2: Call the AI and handle failures gracefully ────────────────
        try {
            String aiReply = assistantService.chat(sessionId, message);
            log.info("AI responded | session={} | replyLength={}", sessionId, aiReply.length());
            return ChatResponse.ok(aiReply, sessionId);

        } catch (Exception e) {
            // AI call failed — log details but return a friendly fallback
            log.error("AI call failed | session={} | error={}", sessionId, e.getMessage(), e);
            return ChatResponse.error(
                    "AI model is temporarily unavailable: " + e.getMessage(),
                    sessionId
            );
        }
    }

    /**
     * Simulates a failure scenario to demonstrate error handling in action.
     *
     * This method deliberately throws an exception mid-processing, which is
     * then caught by the controller's exception handler, returning a structured
     * error response rather than a raw stack trace.
     *
     * Use case: Demo / assignment submission to prove robust error handling.
     */
    public ChatResponse simulateFailure(String sessionId) {
        log.warn("SIMULATED FAILURE triggered | session={}", sessionId);

        // Attempt to call AI — but we deliberately corrupt the input
        try {
            // Simulating a scenario where the AI model throws an exception
            // (e.g., network timeout, quota exceeded, malformed request)
            throw new RuntimeException(
                    "Simulated AI provider failure: Connection timeout after 30s. " +
                    "This demonstrates how the application handles AI model errors gracefully."
            );
        } catch (Exception e) {
            log.error("Caught simulated failure: {}", e.getMessage());
            // Return a structured error response — no stack trace leaks to the client
            return ChatResponse.error(e.getMessage(), sessionId);
        }
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    /**
     * Validates the message content beyond what Bean Validation covers.
     *
     * @return An error message string if invalid, or null if valid.
     */
    private String validateMessage(String message) {
        if (message == null || message.trim().length() < MIN_MESSAGE_LENGTH) {
            return "Message cannot be empty or whitespace only.";
        }

        if (message.length() > MAX_MESSAGE_LENGTH) {
            return "Message too long. Maximum allowed length is " + MAX_MESSAGE_LENGTH + " characters.";
        }

        // Reject messages that are purely special characters (no alphanumeric content)
        if (!message.matches(".*[a-zA-Z0-9].*")) {
            return "Message must contain at least one alphanumeric character.";
        }

        return null; // valid
    }
}

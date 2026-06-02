package com.enterprise.chatbot.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents an incoming chat request from the client.
 *
 * Fields:
 *   - message    : The user's question or statement (required, max 2000 chars).
 *   - sessionId  : Identifies the conversation session for memory continuity.
 *                  Defaults to "default" if not provided.
 */
public class ChatRequest {

    @NotBlank(message = "Message cannot be empty")
    @Size(max = 2000, message = "Message cannot exceed 2000 characters")
    private String message;

    private String sessionId = "default";

    // ── Constructors ──────────────────────────────────────────────────────────

    public ChatRequest() {}

    public ChatRequest(String message, String sessionId) {
        this.message = message;
        this.sessionId = sessionId;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = (sessionId == null || sessionId.isBlank()) ? "default" : sessionId;
    }

    @Override
    public String toString() {
        return "ChatRequest{sessionId='" + sessionId + "', message='" + message + "'}";
    }
}

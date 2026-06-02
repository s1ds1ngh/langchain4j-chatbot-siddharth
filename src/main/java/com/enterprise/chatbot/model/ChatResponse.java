package com.enterprise.chatbot.model;

import java.time.LocalDateTime;

/**
 * Represents the chatbot's response to a user message.
 *
 * Fields:
 *   - reply      : The AI-generated response text.
 *   - sessionId  : Echo of the session ID for client-side tracking.
 *   - success    : True if the AI responded normally, false on fallback/error.
 *   - timestamp  : When the response was generated.
 *   - errorMessage : Populated only when success = false.
 */
public class ChatResponse {

    private String reply;
    private String sessionId;
    private boolean success;
    private LocalDateTime timestamp;
    private String errorMessage;

    // ── Static factory methods ────────────────────────────────────────────────

    public static ChatResponse ok(String reply, String sessionId) {
        ChatResponse r = new ChatResponse();
        r.reply = reply;
        r.sessionId = sessionId;
        r.success = true;
        r.timestamp = LocalDateTime.now();
        return r;
    }

    public static ChatResponse error(String errorMessage, String sessionId) {
        ChatResponse r = new ChatResponse();
        r.reply = "I'm sorry, I encountered an issue processing your request. Please try again.";
        r.sessionId = sessionId;
        r.success = false;
        r.timestamp = LocalDateTime.now();
        r.errorMessage = errorMessage;
        return r;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}

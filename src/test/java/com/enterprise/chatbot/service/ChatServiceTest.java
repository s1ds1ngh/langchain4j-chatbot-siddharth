package com.enterprise.chatbot.service;

import com.enterprise.chatbot.model.ChatRequest;
import com.enterprise.chatbot.model.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChatService.
 *
 * These tests verify:
 *   1. Valid messages are passed to the AI and responses are returned.
 *   2. Invalid / empty messages fail validation without calling the AI.
 *   3. AI exceptions are caught and a fallback response is returned.
 *   4. The simulate-failure mode returns a structured error response.
 *
 * The AssistantService is mocked — no real API key is required to run these.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatService Unit Tests")
class ChatServiceTest {

    @Mock
    private AssistantService assistantService;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(assistantService);
    }

    // ── Happy Path ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Valid message → AI is called and reply is returned")
    void whenValidMessage_thenReturnsAiReply() {
        // Arrange
        when(assistantService.chat(anyString(), anyString()))
                .thenReturn("Hello! I'm Nova. How can I help you?");

        ChatRequest request = new ChatRequest("Hello!", "session-001");

        // Act
        ChatResponse response = chatService.chat(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getReply()).isEqualTo("Hello! I'm Nova. How can I help you?");
        assertThat(response.getSessionId()).isEqualTo("session-001");
        assertThat(response.getErrorMessage()).isNull();
        verify(assistantService, times(1)).chat("session-001", "Hello!");
    }

    @Test
    @DisplayName("Session ID defaults to 'default' when not provided")
    void whenNoSessionId_thenDefaultSessionUsed() {
        when(assistantService.chat(anyString(), anyString())).thenReturn("Hi there!");

        ChatRequest request = new ChatRequest("Hi!", null);
        request.setSessionId(null); // triggers default

        ChatResponse response = chatService.chat(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getSessionId()).isEqualTo("default");
    }

    // ── Validation Tests ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Whitespace-only message → validation error, AI NOT called")
    void whenWhitespaceMessage_thenValidationError() {
        ChatRequest request = new ChatRequest("   ", "session-002");

        ChatResponse response = chatService.chat(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).containsIgnoringCase("empty");
        verify(assistantService, never()).chat(anyString(), anyString());
    }

    @Test
    @DisplayName("Special characters only → validation error, AI NOT called")
    void whenSpecialCharsOnlyMessage_thenValidationError() {
        ChatRequest request = new ChatRequest("!@#$%^&*()", "session-003");

        ChatResponse response = chatService.chat(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).containsIgnoringCase("alphanumeric");
        verify(assistantService, never()).chat(anyString(), anyString());
    }

    @Test
    @DisplayName("Message exceeding 2000 characters → validation error")
    void whenMessageTooLong_thenValidationError() {
        String longMessage = "a".repeat(2001);
        ChatRequest request = new ChatRequest(longMessage, "session-004");

        ChatResponse response = chatService.chat(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).containsIgnoringCase("2000");
        verify(assistantService, never()).chat(anyString(), anyString());
    }

    // ── Error Handling Tests ──────────────────────────────────────────────────

    @Test
    @DisplayName("AI model throws exception → fallback response returned, no crash")
    void whenAiThrowsException_thenFallbackResponseReturned() {
        when(assistantService.chat(anyString(), anyString()))
                .thenThrow(new RuntimeException("Connection timeout: Gemini API unreachable"));

        ChatRequest request = new ChatRequest("Tell me the weather", "session-005");

        // Act — must NOT throw
        ChatResponse response = chatService.chat(request);

        // Assert — graceful fallback
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getReply()).contains("sorry");          // user-friendly message
        assertThat(response.getErrorMessage()).contains("timeout"); // technical details in error field
        verify(assistantService, times(1)).chat(anyString(), anyString());
    }

    @Test
    @DisplayName("Simulate-failure → structured error response, no exception leaks")
    void whenSimulateFailure_thenStructuredErrorReturned() {
        // Act
        ChatResponse response = chatService.simulateFailure("demo-session");

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getReply()).isNotNull();
        assertThat(response.getErrorMessage()).containsIgnoringCase("simulated");
        assertThat(response.getSessionId()).isEqualTo("demo-session");
    }

    // ── Edge Cases ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Exactly 2000 character message → accepted (boundary value)")
    void whenMessageIsExactly2000Chars_thenAccepted() {
        when(assistantService.chat(anyString(), anyString())).thenReturn("Response");

        String boundary = "a".repeat(1999) + "b"; // exactly 2000
        ChatRequest request = new ChatRequest(boundary, "session-006");

        ChatResponse response = chatService.chat(request);

        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("AI returns empty string → still a successful response")
    void whenAiReturnsEmptyString_thenSuccessResponse() {
        when(assistantService.chat(anyString(), anyString())).thenReturn("");

        ChatRequest request = new ChatRequest("Hello", "session-007");

        ChatResponse response = chatService.chat(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getReply()).isEqualTo("");
    }
}

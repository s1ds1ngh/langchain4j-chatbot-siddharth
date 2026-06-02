package com.enterprise.chatbot.controller;

import com.enterprise.chatbot.model.ChatRequest;
import com.enterprise.chatbot.model.ChatResponse;
import com.enterprise.chatbot.service.ChatService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller — exposes the chatbot as a web API.
 *
 * Endpoints:
 *   POST   /api/chat              → Send a message, receive an AI response
 *   GET    /api/chat/health       → Health check (no AI key needed)
 *   GET    /api/chat/demo         → Runs a pre-built demo conversation
 *   POST   /api/chat/simulate-error → Demonstrates error handling in action
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // ── POST /api/chat ────────────────────────────────────────────────────────

    /**
     * Main chat endpoint. Accepts a user message and returns the AI response.
     *
     * Request body:
     *   {
     *     "message":   "What is 25 * 48?",
     *     "sessionId": "user-session-123"   ← optional, defaults to "default"
     *   }
     *
     * Response:
     *   {
     *     "reply":        "25 * 48 = 1200",
     *     "sessionId":    "user-session-123",
     *     "success":      true,
     *     "timestamp":    "2025-01-01T12:00:00",
     *     "errorMessage": null
     *   }
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            BindingResult bindingResult) {

        // Handle Bean Validation errors (@NotBlank, @Size)
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldErrors().stream()
                    .map(e -> e.getField() + ": " + e.getDefaultMessage())
                    .findFirst()
                    .orElse("Validation failed");
            log.warn("Request validation failed: {}", errorMsg);
            return ResponseEntity
                    .badRequest()
                    .body(ChatResponse.error(errorMsg, request.getSessionId()));
        }

        ChatResponse response = chatService.chat(request);
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }

    // ── GET /api/chat/health ──────────────────────────────────────────────────

    /**
     * Health check endpoint — verifies the application is running.
     * Does not call the AI model, so no API key is needed.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "UP");
        status.put("service", "LangChain4j AI Chatbot");
        status.put("model", "gemini-2.0-flash");
        status.put("timestamp", LocalDateTime.now().toString());
        status.put("endpoints", List.of(
                "POST /api/chat",
                "GET  /api/chat/health",
                "GET  /api/chat/demo",
                "POST /api/chat/simulate-error"
        ));
        return ResponseEntity.ok(status);
    }

    // ── GET /api/chat/demo ────────────────────────────────────────────────────

    /**
     * Demo endpoint — fires three pre-built questions through the chatbot
     * and returns all responses in one call. Great for quick demonstrations.
     *
     * Questions covered:
     *   1. General greeting
     *   2. Math calculation (triggers CalculatorTool)
     *   3. Product lookup (triggers ProductTool)
     */
    @GetMapping("/demo")
    public ResponseEntity<Map<String, Object>> demo() {
        log.info("Running demo conversation");

        String demoSession = "demo-session-" + System.currentTimeMillis();

        List<String> demoQuestions = List.of(
                "Hello! What can you help me with today?",
                "Can you calculate 144 * 25 + (500 / 4) for me?",
                "Tell me about the laptop product and whether it's in stock."
        );

        List<Map<String, Object>> exchanges = demoQuestions.stream()
                .map(question -> {
                    ChatRequest req = new ChatRequest(question, demoSession);
                    ChatResponse res = chatService.chat(req);
                    return Map.<String, Object>of(
                            "question", question,
                            "answer",   res.getReply(),
                            "success",  res.isSuccess()
                    );
                })
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("demoSession", demoSession);
        result.put("exchanges", exchanges);
        result.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(result);
    }

    // ── POST /api/chat/simulate-error ─────────────────────────────────────────

    /**
     * Simulates an AI provider failure to demonstrate error handling.
     *
     * This endpoint deliberately triggers a RuntimeException mid-processing,
     * showing that:
     *   1. The exception is caught — no raw stack trace is returned.
     *   2. A structured fallback ChatResponse is returned to the client.
     *   3. The application remains stable and handles the next request normally.
     */
    @PostMapping("/simulate-error")
    public ResponseEntity<ChatResponse> simulateError(
            @RequestParam(defaultValue = "error-demo-session") String sessionId) {

        log.info("Simulate-error endpoint called | session={}", sessionId);
        ChatResponse response = chatService.simulateFailure(sessionId);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    // ── Global exception handler for this controller ──────────────────────────

    /**
     * Catches any unexpected exceptions that escape the service layer,
     * ensuring the API always returns a structured JSON response.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ChatResponse> handleUnexpectedException(Exception e) {
        log.error("Unhandled exception in ChatController: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ChatResponse.error("An unexpected error occurred: " + e.getMessage(), "unknown"));
    }
}

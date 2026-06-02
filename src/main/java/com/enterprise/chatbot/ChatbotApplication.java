package com.enterprise.chatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the LangChain4j AI Chatbot application.
 *
 * This application demonstrates:
 *   - AI Services via LangChain4j (@AiService)
 *   - Prompt Engineering (system messages)
 *   - Model Parameter Control (temperature, max tokens)
 *   - Function Calling via @Tool annotations
 *   - Error Handling with fallback responses
 */
@SpringBootApplication
public class ChatbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatbotApplication.class, args);
    }
}

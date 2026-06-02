package com.enterprise.chatbot.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * AI Service Interface — the heart of the LangChain4j integration.
 *
 * LangChain4j reads this interface at startup and generates a fully
 * functional implementation backed by the configured AI model. No
 * manual HTTP calls, JSON parsing, or streaming logic required.
 *
 * Key annotations:
 *
 *   @SystemMessage  — Defines the AI's persona, behavior, and rules.
 *                     This is injected as a system-level prompt before
 *                     every conversation turn.
 *
 *   @MemoryId       — Scopes conversation history to a session ID.
 *                     Each unique ID maintains its own independent memory.
 *
 *   @UserMessage    — Marks the parameter that carries the user's text.
 *
 * The AI will automatically use @Tool-annotated methods (ProductTool,
 * CalculatorTool) when the user's question warrants their use.
 */
public interface AssistantService {

    @SystemMessage("""
            You are Nova, a professional and friendly enterprise AI assistant.
            
            Your capabilities:
            - Answer general knowledge questions concisely and accurately.
            - Help users with product information using the product catalogue tool.
            - Perform mathematical calculations using the calculator tool.
            - Assist with business queries and enterprise topics.
            
            Behavioral guidelines:
            - Always be polite, professional, and concise.
            - When asked about products, inventory, or pricing — ALWAYS use the product tools.
            - When asked to perform math or calculations — ALWAYS use the calculator tool.
            - If you do not know something, say "I don't have that information" honestly.
            - Do not make up facts, prices, or product details.
            - Keep responses under 200 words unless a detailed explanation is explicitly requested.
            - Format lists with bullet points for readability.
            - Begin responses naturally without announcing yourself every time.
            """)
    String chat(@MemoryId String sessionId, @UserMessage String userMessage);
}

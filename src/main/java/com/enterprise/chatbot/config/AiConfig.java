package com.enterprise.chatbot.config;

import com.enterprise.chatbot.service.AssistantService;
import com.enterprise.chatbot.service.tools.CalculatorTool;
import com.enterprise.chatbot.service.tools.ProductTool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI Configuration — wires the LangChain4j components into Spring's context.
 *
 * This class is responsible for:
 *   1. Configuring the Gemini chat model with specific parameters.
 *   2. Building the AiServices proxy that implements AssistantService.
 *   3. Attaching chat memory (sliding window of last 20 messages per session).
 *   4. Registering @Tool-annotated beans (ProductTool, CalculatorTool).
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * MODEL PARAMETERS EXPLAINED:
 *
 *   temperature (0.0 – 2.0)
 *     Controls the randomness / creativity of the AI's output.
 *     • Low (0.1): Very focused, deterministic, predictable — ideal for
 *       factual Q&A, calculations, structured tasks.
 *     • Medium (0.7): Balanced creativity and coherence — good for general
 *       conversation and enterprise assistants.
 *     • High (1.5+): Highly creative but may ramble or be less accurate —
 *       good for brainstorming or creative writing.
 *
 *   maxOutputTokens
 *     Hard cap on the number of tokens the model generates per response.
 *     • ~100 tokens  ≈ 75 words  (short, punchy answers)
 *     • ~512 tokens  ≈ 385 words (balanced, detailed answers — our default)
 *     • ~1024 tokens ≈ 768 words (long-form, thorough explanations)
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Configuration
public class AiConfig {

    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

    /** Gemini API key — injected from environment variable GEMINI_API_KEY */
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    /**
     * Temperature — controls creativity vs. determinism.
     * Default: 0.7 | Override via: app.ai.temperature in application.properties
     */
    @Value("${app.ai.temperature:0.7}")
    private Double temperature;

    /**
     * Max output tokens — caps response length.
     * Default: 512 | Override via: app.ai.max-tokens in application.properties
     */
    @Value("${app.ai.max-tokens:512}")
    private Integer maxOutputTokens;

    /**
     * Builds and configures the Google Gemini chat model.
     *
     * Changing temperature demonstrates different AI behaviours:
     *   0.1 → precise, conservative, repeatable
     *   0.7 → natural, conversational (default)
     *   1.5 → creative, varied, sometimes surprising
     */
    @Bean
    public GoogleAiGeminiChatModel geminiChatModel() {
        log.info("Initialising Gemini model | temperature={} | maxTokens={}", temperature, maxOutputTokens);

        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName("gemini-3-flash-preview")
                .temperature(temperature)
                .maxOutputTokens(maxOutputTokens)
                .build();
    }

    /**
     * Builds the AssistantService proxy via LangChain4j AiServices.
     *
     * AiServices reads the @SystemMessage, @MemoryId, and @UserMessage
     * annotations on the AssistantService interface and generates a full
     * implementation at runtime — including tool dispatching.
     *
     * Memory: Sliding window of 20 messages per session ID. When the window
     * is full, the oldest messages are dropped to stay within context limits.
     */
    @Bean
    public AssistantService assistantService(
            GoogleAiGeminiChatModel model,
            ProductTool productTool,
            CalculatorTool calculatorTool) {

        return AiServices.builder(AssistantService.class)
                .chatLanguageModel(model)
                .chatMemoryProvider(sessionId ->
                        MessageWindowChatMemory.withMaxMessages(20))
                .tools(productTool, calculatorTool)
                .build();
    }
}

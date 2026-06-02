# 🤖 LangChain4j AI Chatbot

An enterprise AI chatbot built with **LangChain4j** and **Spring Boot**, powered by **Google Gemini**. Demonstrates AI Services, Prompt Engineering, Model Parameter Control, Function Calling, and Error Handling.

---

## 📋 Table of Contents

- [Project Architecture](#-project-architecture)
- [Tech Stack](#-tech-stack)
- [Features](#-features)
- [Setup & Installation](#-setup--installation)
- [Running the Application](#-running-the-application)
- [API Reference](#-api-reference)
- [Feature Deep-Dive](#-feature-deep-dive)
  - [AI Service Layer](#1-ai-service-layer)
  - [Prompt Strategy](#2-prompt-strategy)
  - [Tool / Function Calling](#3-tool--function-calling)
  - [Model Parameter Configuration](#4-model-parameter-configuration)
  - [Error Handling](#5-error-handling)
- [Demo Walkthrough](#-demo-walkthrough)
- [Visual Proof (Screenshots)](#-visual-proof-screenshots-required)
- [Running Tests](#-running-tests)
- [Project Structure](#-project-structure)

---

## 🏗 Project Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                          CLIENT                                 │
│                   (curl / Postman / Browser)                    │
└───────────────────────────┬─────────────────────────────────────┘
                            │ HTTP Request
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    ChatController                               │
│          (REST API — POST /api/chat, GET /api/chat/demo)        │
│          Handles: routing, bean validation, HTTP status codes   │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                     ChatService                                 │
│     Handles: business validation, orchestration, fallback       │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│               AssistantService (@AiService)                     │
│    LangChain4j-generated proxy — handles prompts + memory       │
│                                                                 │
│   ┌─────────────────┐    ┌───────────────────────────────┐      │
│   │  System Prompt  │    │  Conversation Memory          │      │
│   │  (Nova persona) │    │  (20-message sliding window)  │      │
│   └─────────────────┘    └───────────────────────────────┘      │
└───────────────────────────┬─────────────────────────────────────┘
                            │
              ┌─────────────┼──────────────┐
              │             │              │
              ▼             ▼              ▼
    ┌──────────────┐ ┌─────────────┐ ┌─────────────────────────┐
    │ ProductTool  │ │ Calculator  │ │  Google Gemini API       │
    │ @Tool methods│ │ Tool @Tool  │ │  (gemini-2.0-flash)      │
    └──────────────┘ └─────────────┘ └─────────────────────────┘
```

**Request flow:**
1. Client sends `POST /api/chat` with a user message.
2. `ChatController` validates the request using Bean Validation.
3. `ChatService` applies business-layer validation and calls `AssistantService`.
4. LangChain4j sends the message to Gemini, with the system prompt prepended.
5. If Gemini decides a tool is needed, it calls `ProductTool` or `CalculatorTool`.
6. The final response travels back through the layers to the client.

---

## ⚙️ Tech Stack

| Component    | Technology                               |
| --------------| ------------------------------------------|
| Framework    | Spring Boot 3.3.5                        |
| AI Framework | LangChain4j 0.36.2                       |
| AI Provider  | Google Gemini (`gemini-2-flash-preview`) |
| Build Tool   | Maven                                    |
| Language     | Java 17                                  |
| Testing      | JUnit 5 + Mockito + AssertJ              |

---

## ✨ Features

| # | Feature | Status |
|---|---|---|
| 1 | AI chatbot with dynamic responses | ✅ |
| 2 | AI Service Layer (`@AiService`) | ✅ |
| 3 | System prompt with persona ("Nova") | ✅ |
| 4 | Conversation memory (per-session) | ✅ |
| 5 | Model parameter control (temperature, max tokens) | ✅ |
| 6 | `@Tool` — Product lookup & inventory check | ✅ |
| 7 | `@Tool` — Math calculator | ✅ |
| 8 | Input validation (empty, too-long, invalid) | ✅ |
| 9 | Fallback response on AI failure | ✅ |
| 10 | Simulated failure demo endpoint | ✅ |
| 11 | Unit tests (no API key required) | ✅ |

---

## 🚀 Setup & Installation

### Prerequisites

- Java 17+
- Maven 3.8+
- A free **Gemini API key** from [https://aistudio.google.com](https://aistudio.google.com)

### 1. Clone / navigate to the project

```bash
cd d:\exterprise-java-ai
```

### 2. Set your Gemini API key

**Windows PowerShell:**
```powershell
$env:GEMINI_API_KEY = "your_actual_api_key_here"
```

**Windows CMD:**
```cmd
set GEMINI_API_KEY=your_actual_api_key_here
```

**Linux / macOS:**
```bash
export GEMINI_API_KEY=your_actual_api_key_here
```

> ⚠️ **Never hardcode your API key in source files.** The application reads it from the environment variable.

---

## ▶️ Running the Application

```bash
mvn spring-boot:run
```

The app starts on **http://localhost:8080**.

You should see:
```
HH:mm:ss [main] INFO  c.e.c.config.AiConfig - Initialising Gemini model | temperature=0.7 | maxTokens=512
HH:mm:ss [main] INFO  c.e.c.ChatbotApplication - Started ChatbotApplication in X.XXX seconds
```

---

## 📡 API Reference

### `POST /api/chat` — Send a message

**Request:**
```json
{
  "message": "What is 144 multiplied by 25?",
  "sessionId": "my-session-123"
}
```

**Success Response (200 OK):**
```json
{
  "reply": "144 * 25 = 3600",
  "sessionId": "my-session-123",
  "success": true,
  "timestamp": "2025-01-01T12:00:00",
  "errorMessage": null
}
```

**Error Response (400 Bad Request — empty message):**
```json
{
  "reply": "I'm sorry, I encountered an issue processing your request. Please try again.",
  "sessionId": "my-session-123",
  "success": false,
  "timestamp": "2025-01-01T12:00:00",
  "errorMessage": "Message cannot be empty or whitespace only."
}
```

---

### `GET /api/chat/health` — Health check

```bash
curl http://localhost:8080/api/chat/health
```

```json
{
  "status": "UP",
  "service": "LangChain4j AI Chatbot",
  "model": "gemini-2.0-flash",
  "timestamp": "2025-01-01T12:00:00",
  "endpoints": ["POST /api/chat", "GET /api/chat/health", "GET /api/chat/demo", "POST /api/chat/simulate-error"]
}
```

---

### `GET /api/chat/demo` — Pre-built demo conversation

Runs 3 questions automatically (greeting → calculation → product lookup):

```bash
curl http://localhost:8080/api/chat/demo
```

---

### `POST /api/chat/simulate-error` — Demonstrate error handling

```bash
curl -X POST "http://localhost:8080/api/chat/simulate-error?sessionId=test"
```

**Response (503 Service Unavailable):**
```json
{
  "reply": "I'm sorry, I encountered an issue processing your request. Please try again.",
  "success": false,
  "errorMessage": "Simulated AI provider failure: Connection timeout after 30s..."
}
```

---

## 🔍 Feature Deep-Dive

### 1. AI Service Layer

The AI service layer uses LangChain4j's **`@AiService`** annotation to define a plain Java interface, and LangChain4j generates the full implementation at runtime.

```java
// src/main/java/.../service/AssistantService.java
public interface AssistantService {

    @SystemMessage("You are Nova, a professional enterprise AI assistant...")
    String chat(@MemoryId String sessionId, @UserMessage String userMessage);
}
```

LangChain4j wires this in `AiConfig.java`:

```java
AiServices.builder(AssistantService.class)
    .chatLanguageModel(model)                                       // ← Gemini
    .chatMemoryProvider(id -> MessageWindowChatMemory.withMaxMessages(20)) // ← memory
    .tools(productTool, calculatorTool)                             // ← function calling
    .build();
```

**Why this is powerful:** You write zero boilerplate HTTP code. LangChain4j handles API calls, streaming, memory, and tool dispatch automatically from the interface definition.

---

### 2. Prompt Strategy

The system prompt is defined directly on the interface method using `@SystemMessage`:

```java
@SystemMessage("""
    You are Nova, a professional and friendly enterprise AI assistant.

    Your capabilities:
    - Answer general knowledge questions concisely and accurately.
    - Help users with product information using the product catalogue tool.
    - Perform mathematical calculations using the calculator tool.

    Behavioral guidelines:
    - When asked about products or pricing — ALWAYS use the product tools.
    - When asked to calculate — ALWAYS use the calculator tool.
    - If you don't know something, say so honestly.
    - Keep responses under 200 words unless detailed explanation is requested.
    """)
```

**Prompt engineering choices:**
- **Explicit tool instruction** (`ALWAYS use the calculator tool`) reduces tool-calling failures.
- **Length constraint** (`under 200 words`) prevents verbose, unfocused responses.
- **Honesty directive** (`say so honestly`) improves trust and reduces hallucinations.
- **Persona naming** (`Nova`) gives the assistant a consistent identity.

---

### 3. Tool / Function Calling

Tools are regular Spring beans annotated with `@Tool`. LangChain4j registers them with the Gemini model, which decides at runtime whether to call them.

**ProductTool** — fetches product details and checks inventory:
```java
@Tool("Fetches detailed information about a product from the enterprise catalogue.")
public String getProductDetails(String productName) { ... }

@Tool("Checks the current inventory / stock level for a product.")
public String checkInventory(String productName) { ... }
```

**CalculatorTool** — evaluates arithmetic:
```java
@Tool("Evaluates a mathematical expression. Supports +, -, *, /, ^, parentheses.")
public String calculate(String expression) { ... }
```

**How it works end-to-end:**
1. User asks: *"What is 25 × 48?"*
2. Gemini recognises this as a calculation request.
3. Gemini calls `calculate("25 * 48")` automatically.
4. The tool returns `"25 * 48 = 1200"`.
5. Gemini incorporates the result into its final reply.

> No routing logic, switch statements, or keyword matching needed — the AI makes the call decision.

---

### 4. Model Parameter Configuration

Parameters are set in `application.properties` and injected into `AiConfig.java`:

| Parameter | Property | Default | Effect |
|---|---|---|---|
| **Temperature** | `app.ai.temperature` | `0.7` | Controls creativity vs. precision |
| **Max Tokens** | `app.ai.max-tokens` | `512` | Controls maximum response length |

#### Temperature Demo

**Prompt:** *"Describe what an AI assistant is in one sentence."*

| Temperature | Sample Output |
|---|---|
| **0.1** | "An AI assistant is a software program that uses artificial intelligence to help users complete tasks through natural language interaction." |
| **0.7** | "An AI assistant is a smart digital companion that understands your questions and helps you get things done — from answering queries to running calculations." |
| **1.5** | "An AI assistant is like a brilliant, tireless colleague who lives inside your device, ready to fetch facts, crunch numbers, and brainstorm ideas at a moment's notice!" |

To change temperature, edit `application.properties`:
```properties
app.ai.temperature=0.1   # More focused / deterministic
app.ai.temperature=1.5   # More creative / varied
```

---

### 5. Error Handling

Three layers of protection ensure the API never crashes or leaks stack traces:

#### Layer 1 — Bean Validation (DTO level)
```java
@NotBlank(message = "Message cannot be empty")
@Size(max = 2000, message = "Message cannot exceed 2000 characters")
private String message;
```

#### Layer 2 — Business Validation (ChatService)
```java
// Rejects messages with no alphanumeric content
if (!message.matches(".*[a-zA-Z0-9].*")) {
    return ChatResponse.error("Message must contain at least one alphanumeric character.", sessionId);
}
```

#### Layer 3 — AI Exception Catch (ChatService)
```java
try {
    String aiReply = assistantService.chat(sessionId, message);
    return ChatResponse.ok(aiReply, sessionId);
} catch (Exception e) {
    log.error("AI call failed: {}", e.getMessage(), e);
    return ChatResponse.error("AI model temporarily unavailable: " + e.getMessage(), sessionId);
}
```

#### Simulated Failure Demo

```bash
curl -X POST "http://localhost:8080/api/chat/simulate-error?sessionId=demo"
```

This endpoint deliberately throws a `RuntimeException` to show that:
- The exception is caught at the service level.
- A structured JSON response is returned (no raw stack trace).
- The application continues processing subsequent requests normally.

---

## 📸 Visual Proof (Screenshots Required)

> **Note to student/developer:** Add your screenshots in the placeholders below to complete the documentation requirement.

### 1. Chatbot Conversation
*(Multi-turn conversation showing context retention)*

**Here I am telling the chatbot my name, and sending the request with a `session_id`.**
![Session_1_conversaion_inital](src\assests\screenshots\image.png)
**And below we can see that the chatbot still remembers my name.**
![Session_2_conversation_continuation](src\assests\screenshots\image-1.png)

### 2. Tool / Function Execution
*(Screenshot showing the chatbot calling a custom tool/function like the Calculator or Product lookup)*

**I sent a simple calcuation request.**
![tool_request_send](src\assests\screenshots\image-2.png)
**And then AI used the calculator tool to calculate the result.**
![tool_call](src\assests\screenshots\image-3.png)

### 3. Error Handling / Fallback
*(Screenshot showing invalid input or AI failure with a graceful fallback response)*
**I sent an inapporiate request that the agent refused to answer.**
![invalid_input](src\assests\screenshots\image-4.png)

---

## 🧪 Running Tests

Tests use Mockito to mock the `AssistantService` — **no Gemini API key required**.

```bash
mvn test
```

**Test coverage:**
- ✅ Valid messages → AI called, response returned
- ✅ Empty/whitespace messages → validation error (AI never called)
- ✅ Special-character-only messages → validation error
- ✅ Messages > 2000 chars → validation error
- ✅ AI throws exception → fallback response returned, no crash
- ✅ Simulated failure → structured error response
- ✅ Calculator expressions (parameterized: 7 expressions)
- ✅ Division by zero handling
- ✅ Boundary value: exactly 2000 chars accepted

---

## 📁 Project Structure

```
d:\exterprise-java-ai\
├── pom.xml                                          # Maven build file
├── README.md                                        # This file
└── src/
    ├── main/
    │   ├── java/com/enterprise/chatbot/
    │   │   ├── ChatbotApplication.java              # Spring Boot entry point
    │   │   ├── config/
    │   │   │   └── AiConfig.java                   # Gemini model + AiService wiring
    │   │   ├── controller/
    │   │   │   └── ChatController.java              # REST API endpoints
    │   │   ├── service/
    │   │   │   ├── AssistantService.java            # @AiService interface (prompts)
    │   │   │   ├── ChatService.java                 # Validation + orchestration
    │   │   │   └── tools/
    │   │   │       ├── ProductTool.java             # @Tool - product catalogue
    │   │   │       └── CalculatorTool.java          # @Tool - math calculator
    │   │   └── model/
    │   │       ├── ChatRequest.java                 # Request DTO
    │   │       └── ChatResponse.java                # Response DTO
    │   └── resources/
    │       └── application.properties               # Config (temperature, max tokens)
    └── test/
        └── java/com/enterprise/chatbot/
            ├── service/
            │   └── ChatServiceTest.java             # Unit tests (8 test cases)
            └── service/tools/
                └── CalculatorToolTest.java          # Unit tests (parameterized)
```

---

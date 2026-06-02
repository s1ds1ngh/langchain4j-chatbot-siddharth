package com.enterprise.chatbot.service.tools;

import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Stack;

/**
 * LangChain4j Tool: Mathematical Calculator
 *
 * The AI will automatically call this tool when users ask math questions.
 * It evaluates arithmetic expressions safely without using eval() or
 * scripting engines — implemented with a recursive descent parser.
 *
 * Supported operations: +, -, *, /, ^ (power), parentheses, decimals.
 */
@Component
public class CalculatorTool {

    private static final Logger log = LoggerFactory.getLogger(CalculatorTool.class);

    /**
     * Evaluates a mathematical expression and returns the result.
     *
     * @param expression A mathematical expression string (e.g., "25 * 48 + 100 / 4")
     * @return The computed result as a string, or an error message if invalid.
     */
    @Tool("Evaluates a mathematical expression and returns the numerical result. " +
          "Use this when the user asks to calculate, compute, or solve any arithmetic. " +
          "Supports: +, -, *, /, ^ (power), and parentheses. " +
          "Examples: '25 * 48', '(100 + 50) / 3', '2 ^ 10'.")
    public String calculate(String expression) {
        log.info("[TOOL CALLED] calculate(\"{}\")", expression);

        if (expression == null || expression.isBlank()) {
            return "Error: No expression provided.";
        }

        try {
            // Sanitize: allow only digits, operators, spaces, dots, parentheses
            String sanitized = expression.replaceAll("[^0-9+\\-*/^().\\s]", "").trim();
            if (sanitized.isEmpty()) {
                return "Error: Expression contains no valid mathematical characters.";
            }

            double result = new ExpressionParser(sanitized).parse();

            // Format nicely: no trailing .0 for whole numbers
            if (result == Math.floor(result) && !Double.isInfinite(result)) {
                return expression + " = " + (long) result;
            } else {
                return expression + " = " + String.format("%.4f", result).replaceAll("0*$", "").replaceAll("\\.$", "");
            }
        } catch (ArithmeticException e) {
            return "Error: Division by zero is undefined.";
        } catch (Exception e) {
            log.warn("Calculator failed to evaluate '{}': {}", expression, e.getMessage());
            return "Error: Could not evaluate '" + expression + "'. Please check the expression format.";
        }
    }

    // ── Recursive Descent Parser ──────────────────────────────────────────────

    /**
     * A simple recursive-descent parser for arithmetic expressions.
     * Grammar:
     *   expr   -> term ( ('+' | '-') term )*
     *   term   -> power ( ('*' | '/') power )*
     *   power  -> factor ('^' factor)*
     *   factor -> '(' expr ')' | number
     */
    private static class ExpressionParser {
        private final String input;
        private int pos = 0;

        ExpressionParser(String input) {
            this.input = input.replaceAll("\\s+", "");
        }

        double parse() {
            double result = parseExpression();
            if (pos < input.length()) {
                throw new IllegalArgumentException("Unexpected character: " + input.charAt(pos));
            }
            return result;
        }

        private double parseExpression() {
            double result = parseTerm();
            while (pos < input.length()) {
                char op = input.charAt(pos);
                if (op == '+') { pos++; result += parseTerm(); }
                else if (op == '-') { pos++; result -= parseTerm(); }
                else break;
            }
            return result;
        }

        private double parseTerm() {
            double result = parsePower();
            while (pos < input.length()) {
                char op = input.charAt(pos);
                if (op == '*') { pos++; result *= parsePower(); }
                else if (op == '/') {
                    pos++;
                    double divisor = parsePower();
                    if (divisor == 0) throw new ArithmeticException("Division by zero");
                    result /= divisor;
                } else break;
            }
            return result;
        }

        private double parsePower() {
            double base = parseFactor();
            if (pos < input.length() && input.charAt(pos) == '^') {
                pos++;
                double exp = parsePower(); // right-associative
                return Math.pow(base, exp);
            }
            return base;
        }

        private double parseFactor() {
            if (pos < input.length() && input.charAt(pos) == '(') {
                pos++; // consume '('
                double result = parseExpression();
                if (pos >= input.length() || input.charAt(pos) != ')') {
                    throw new IllegalArgumentException("Missing closing parenthesis");
                }
                pos++; // consume ')'
                return result;
            }

            // Unary minus
            if (pos < input.length() && input.charAt(pos) == '-') {
                pos++;
                return -parseFactor();
            }

            // Number
            int start = pos;
            while (pos < input.length() && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
                pos++;
            }
            if (start == pos) {
                throw new IllegalArgumentException("Expected number at position " + pos);
            }
            return Double.parseDouble(input.substring(start, pos));
        }
    }
}

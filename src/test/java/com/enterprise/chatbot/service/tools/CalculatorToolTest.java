package com.enterprise.chatbot.service.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CalculatorTool.
 * No Spring context needed — pure unit tests.
 */
@DisplayName("CalculatorTool Unit Tests")
class CalculatorToolTest {

    private CalculatorTool calculator;

    @BeforeEach
    void setUp() {
        calculator = new CalculatorTool();
    }

    @ParameterizedTest(name = "{0} = {1}")
    @CsvSource({
        "2 + 3,              5",
        "10 - 4,             6",
        "25 * 48,            1200",
        "100 / 4,            25",
        "(100 + 50) / 3,     50",
        "2 ^ 10,             1024",
        "144 * 25 + 500 / 4, 3725"
    })
    @DisplayName("Arithmetic expressions are evaluated correctly")
    void arithmeticEvaluation(String expression, long expected) {
        String result = calculator.calculate(expression);
        assertThat(result).contains(String.valueOf(expected));
    }

    @Test
    @DisplayName("Empty expression returns error message")
    void emptyExpressionReturnsError() {
        String result = calculator.calculate("");
        assertThat(result).containsIgnoringCase("error");
    }

    @Test
    @DisplayName("Null expression returns error message")
    void nullExpressionReturnsError() {
        String result = calculator.calculate(null);
        assertThat(result).containsIgnoringCase("error");
    }

    @Test
    @DisplayName("Division by zero returns descriptive error")
    void divisionByZeroReturnsError() {
        String result = calculator.calculate("10 / 0");
        assertThat(result).containsIgnoringCase("zero");
    }
}

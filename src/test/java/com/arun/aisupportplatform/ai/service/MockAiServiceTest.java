package com.arun.aisupportplatform.ai.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MockAiServiceTest {

    private final MockAiService aiService = new MockAiService();

    @Test
    void shouldReturnMockResponse() {

        String result = aiService.generateResponse(
                "Explain what a customer support ticket is."
        );

        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    @Test
    void shouldReturnSummaryResponse() {

        String result = aiService.generateResponse(
                "Summarize this customer ticket"
        );

        assertTrue(result.contains("Mock summary"));
    }

    @Test
    void shouldReturnCategoryResponse() {

        String result = aiService.generateResponse(
                "Determine the category of this ticket"
        );

        assertTrue(result.contains("TECHNICAL_SUPPORT"));
    }

    @Test
    void shouldReturnPriorityResponse() {

        String result = aiService.generateResponse(
                "Determine the priority of this ticket"
        );

        assertTrue(result.contains("MEDIUM"));
    }

    @Test
    void shouldRejectEmptyPrompt() {

        assertThrows(
                IllegalArgumentException.class,
                () -> aiService.generateResponse("")
        );
    }
}
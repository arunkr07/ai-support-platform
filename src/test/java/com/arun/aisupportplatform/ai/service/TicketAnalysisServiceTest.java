package com.arun.aisupportplatform.ai.service;

import com.arun.aisupportplatform.entity.AiSuggestedPriority;
import com.arun.aisupportplatform.ai.dto.TicketAnalysisRequest;
import com.arun.aisupportplatform.ai.dto.TicketAnalysisResponse;
import com.arun.aisupportplatform.entity.TicketCategory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketAnalysisServiceTest {

    private final AiService aiService = mock(AiService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final TicketAnalysisService ticketAnalysisService =
            new TicketAnalysisService(
                    aiService,
                    objectMapper
            );

    @Test
    void shouldAnalyzeTicketSuccessfully() {

        String aiResponse = """
                {
                  "summary": "Customer cannot reset their password.",
                  "category": "ACCOUNT_ACCESS",
                  "suggestedPriority": "HIGH"
                }
                """;

        when(aiService.generateResponse(anyString()))
                .thenReturn(aiResponse);

        TicketAnalysisRequest request =
                new TicketAnalysisRequest(
                        "Cannot reset password",
                        "I tried resetting my password but never received the reset email."
                );

        TicketAnalysisResponse result =
                ticketAnalysisService.analyzeTicket(request);

        assertNotNull(result);

        assertEquals(
                "Customer cannot reset their password.",
                result.summary()
        );

        assertEquals(
                TicketCategory.ACCOUNT_ACCESS,
                result.category()
        );

        assertEquals(
                AiSuggestedPriority.HIGH,
                result.suggestedPriority()
        );

        verify(aiService, times(1))
                .generateResponse(anyString());
    }

    @Test
    void shouldRejectInvalidAiResponse() {

        String invalidAiResponse = """
                This is not valid JSON.
                """;

        when(aiService.generateResponse(anyString()))
                .thenReturn(invalidAiResponse);

        TicketAnalysisRequest request =
                new TicketAnalysisRequest(
                        "Cannot reset password",
                        "I never received the reset email."
                );

        assertThrows(
                IllegalStateException.class,
                () -> ticketAnalysisService.analyzeTicket(request)
        );

        verify(aiService, times(1))
                .generateResponse(anyString());
    }

    @Test
    void shouldRejectInvalidCategory() {

        String invalidAiResponse = """
                {
                  "summary": "Customer cannot reset their password.",
                  "category": "INVALID_CATEGORY",
                  "suggestedPriority": "HIGH"
                }
                """;

        when(aiService.generateResponse(anyString()))
                .thenReturn(invalidAiResponse);

        TicketAnalysisRequest request =
                new TicketAnalysisRequest(
                        "Cannot reset password",
                        "I never received the reset email."
                );

        assertThrows(
                IllegalStateException.class,
                () -> ticketAnalysisService.analyzeTicket(request)
        );
    }

    @Test
    void shouldRejectInvalidPriority() {

        String invalidAiResponse = """
                {
                  "summary": "Customer cannot reset their password.",
                  "category": "ACCOUNT_ACCESS",
                  "suggestedPriority": "CRITICAL"
                }
                """;

        when(aiService.generateResponse(anyString()))
                .thenReturn(invalidAiResponse);

        TicketAnalysisRequest request =
                new TicketAnalysisRequest(
                        "Cannot reset password",
                        "I never received the reset email."
                );

        assertThrows(
                IllegalStateException.class,
                () -> ticketAnalysisService.analyzeTicket(request)
        );
    }
}


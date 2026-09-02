package com.arun.aisupportplatform.ai.service;

import com.arun.aisupportplatform.ai.dto.SuggestedReplyRequest;
import com.arun.aisupportplatform.ai.dto.SuggestedReplyResponse;
import com.arun.aisupportplatform.service.TicketService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SuggestedReplyServiceTest {

    private final AiService aiService = mock(AiService.class);

    private final TicketService ticketService = mock(TicketService.class);

    private final SuggestedReplyService suggestedReplyService =
            new SuggestedReplyService(aiService,ticketService);

    @Test
    void shouldGenerateSuggestedReply() {

        String aiReply =
                "Hi, I'm sorry to hear about the duplicate charge. " +
                        "I'll help you resolve this issue.";

        when(aiService.generateResponse(anyString()))
                .thenReturn(aiReply);

        SuggestedReplyRequest request =
                new SuggestedReplyRequest(
                        "I was charged twice for the same order.",
                        "Customer purchased order #12345 yesterday."
                );

        SuggestedReplyResponse result =
                suggestedReplyService.generateSuggestedReply(request);

        assertNotNull(result);

        assertEquals(
                aiReply,
                result.suggestedReply()
        );

        verify(aiService, times(1))
                .generateResponse(anyString());
    }

    @Test
    void shouldHandleMissingTicketContext() {

        String aiReply =
                "Hi, thank you for contacting support. " +
                        "We'll look into this issue for you.";

        when(aiService.generateResponse(anyString()))
                .thenReturn(aiReply);

        SuggestedReplyRequest request =
                new SuggestedReplyRequest(
                        "My payment failed.",
                        null
                );

        SuggestedReplyResponse result =
                suggestedReplyService.generateSuggestedReply(request);

        assertNotNull(result);

        assertEquals(
                aiReply,
                result.suggestedReply()
        );

        verify(aiService, times(1))
                .generateResponse(anyString());
    }

    @Test
    void shouldPassCustomerMessageToAiService() {

        when(aiService.generateResponse(anyString()))
                .thenReturn("Suggested response");

        SuggestedReplyRequest request =
                new SuggestedReplyRequest(
                        "I cannot login to my account.",
                        "Customer has an existing account."
                );

        suggestedReplyService.generateSuggestedReply(request);

        verify(aiService).generateResponse(
                argThat(prompt ->
                        prompt.contains("I cannot login to my account.")
                )
        );
    }
}

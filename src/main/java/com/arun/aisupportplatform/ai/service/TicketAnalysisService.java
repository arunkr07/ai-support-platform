package com.arun.aisupportplatform.ai.service;

import com.arun.aisupportplatform.ai.dto.TicketAnalysisRequest;
import com.arun.aisupportplatform.ai.dto.TicketAnalysisResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class TicketAnalysisService {

    private final AiService aiService;
    private final ObjectMapper objectMapper;

    public TicketAnalysisService(
            AiService aiService,
            ObjectMapper objectMapper
    ) {
        this.aiService = aiService;
        this.objectMapper = objectMapper;
    }

    public TicketAnalysisResponse analyzeTicket(
            TicketAnalysisRequest request
    ) {

        String prompt = """
                Analyze the following customer support ticket.
                
                Title: %s
                
                Description: %s
                
                Return ONLY valid JSON using exactly this structure:
                
                {
                  "summary": "short summary of the issue",
                  "category": "one valid category",
                  "suggestedPriority": "LOW, MEDIUM, HIGH, or URGENT"
                }
                
                Valid categories:
                ACCOUNT_ACCESS
                BILLING
                TECHNICAL_SUPPORT
                PAYMENT
                SECURITY
                PRODUCT_ISSUE
                FEATURE_REQUEST
                OTHER
                """.formatted(
                request.title(),
                request.description()
        );

        String aiResponse = aiService.generateResponse(prompt);

        try {

            return objectMapper.readValue(
                    aiResponse,
                    TicketAnalysisResponse.class
            );

        } catch (JsonProcessingException exception) {

            throw new IllegalStateException(
                    "AI returned an invalid response"
            );
        }
    }
}
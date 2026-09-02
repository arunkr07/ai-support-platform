package com.arun.aisupportplatform.ai.dto;

import com.arun.aisupportplatform.entity.AiSuggestedPriority;
import com.arun.aisupportplatform.entity.TicketCategory;

import java.time.LocalDateTime;

public record TicketAiAnalysisResponse(
        Long id,
        Long ticketId,
        String summary,
        TicketCategory category,
        AiSuggestedPriority suggestedPriority,
        LocalDateTime createdAt
) {
}
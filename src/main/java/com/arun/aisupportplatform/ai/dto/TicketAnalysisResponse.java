package com.arun.aisupportplatform.ai.dto;

import com.arun.aisupportplatform.entity.AiSuggestedPriority;
import com.arun.aisupportplatform.entity.TicketCategory;

public record TicketAnalysisResponse(
        String summary,
        TicketCategory category,
        AiSuggestedPriority suggestedPriority
) {
}
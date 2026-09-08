package com.arun.aisupportplatform.ai.dto;

public record AiAnalysisResult(
        String summary,
        String category,
        String suggestedPriority
) {}
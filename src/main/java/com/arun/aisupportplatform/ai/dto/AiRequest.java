package com.arun.aisupportplatform.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiRequest(

        @NotBlank(message = "Prompt is required")
        @Size(max = 5000, message = "Prompt cannot exceed 5000 characters")
        String prompt

) {
}
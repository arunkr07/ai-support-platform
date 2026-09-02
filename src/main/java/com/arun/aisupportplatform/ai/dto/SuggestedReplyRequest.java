package com.arun.aisupportplatform.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SuggestedReplyRequest(

        @NotBlank(message = "Customer message is required")
        @Size(max = 2000, message = "Customer message cannot exceed 2000 characters")
        String customerMessage,

        @Size(max = 5000, message = "Ticket context cannot exceed 5000 characters")
        String ticketContext
) {
}
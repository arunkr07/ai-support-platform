package com.arun.aisupportplatform.dto;

import com.arun.aisupportplatform.entity.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTicketStatusRequest(
        @NotNull(message = "Status is required")
        TicketStatus status
) {
}
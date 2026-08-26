package com.arun.aisupportplatform.dto;

import java.time.LocalDateTime;

public record TicketNoteResponse(
        Long id,
        String content,
        Long ticketId,
        Long agentId,
        String agentName,
        LocalDateTime createdAt
) {
}

package com.arun.aisupportplatform.dto;

import java.time.LocalDateTime;

public record TicketMessageResponse(
        Long id,
        String content,
        Long senderId,
        String senderName,
        String senderRole,
        LocalDateTime createdAt
) {
}
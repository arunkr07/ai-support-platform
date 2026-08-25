package com.arun.aisupportplatform.dto;

import com.arun.aisupportplatform.entity.TicketPriority;
import com.arun.aisupportplatform.entity.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {

    private Long id;
    private String title;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;

    private Long customerId;
    private String customerName;
    private String customerEmail;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
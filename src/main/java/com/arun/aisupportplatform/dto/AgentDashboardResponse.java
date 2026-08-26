package com.arun.aisupportplatform.dto;

public record AgentDashboardResponse(
        long totalTickets,
        long openTickets,
        long inProgressTickets,
        long resolvedTickets
) {
}
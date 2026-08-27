package com.arun.aisupportplatform.dto;

public record AdminDashboardResponse(
        long totalTickets,
        long openTickets,
        long inProgressTickets,
        long resolvedTickets,
        long unassignedTickets,
        long totalAgents,
        long totalCustomers
) {
}
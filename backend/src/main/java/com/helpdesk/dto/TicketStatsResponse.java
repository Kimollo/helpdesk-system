package com.helpdesk.dto;

public record TicketStatsResponse(
        long open,
        long inProgress,
        long waitingOnCustomer,
        long resolved,
        long closed,
        long total
) {
}

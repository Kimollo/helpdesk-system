package com.helpdesk.dto;

import com.helpdesk.domain.TicketCategory;
import com.helpdesk.domain.TicketPriority;
import com.helpdesk.domain.TicketStatus;

import java.time.Instant;

public record TicketResponse(
        Long id,
        String subject,
        String description,
        TicketStatus status,
        TicketPriority priority,
        TicketCategory category,
        UserResponse requester,
        UserResponse assignee,
        Instant createdAt,
        Instant updatedAt,
        Instant resolvedAt
) {
}

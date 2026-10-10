package com.helpdesk.dto;

import com.helpdesk.domain.TicketHistoryType;
import com.helpdesk.domain.TicketStatus;

import java.time.Instant;

public record TicketHistoryResponse(
        Long id,
        UserResponse changedBy,
        TicketHistoryType changeType,
        TicketStatus oldStatus,
        TicketStatus newStatus,
        UserResponse oldAssignee,
        UserResponse newAssignee,
        Instant changedAt
) {
}

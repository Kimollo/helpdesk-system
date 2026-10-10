package com.helpdesk.dto;

import com.helpdesk.domain.TicketCategory;
import com.helpdesk.domain.TicketPriority;
import com.helpdesk.domain.TicketStatus;

public record UpdateTicketRequest(
        TicketStatus status,
        TicketPriority priority,
        TicketCategory category,
        Long assigneeId,
        boolean clearAssignee
) {
}

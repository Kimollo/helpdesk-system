package com.helpdesk.dto;

import com.helpdesk.domain.TicketCategory;
import com.helpdesk.domain.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotBlank @Size(min = 5, max = 160) String subject,
        @NotBlank @Size(min = 10, max = 5000) String description,
        @NotNull TicketPriority priority,
        @NotNull TicketCategory category
) {
}

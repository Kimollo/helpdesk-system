package com.helpdesk.dto;

import com.helpdesk.domain.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
        @NotNull Role role,
        @NotNull Boolean enabled
) {
}

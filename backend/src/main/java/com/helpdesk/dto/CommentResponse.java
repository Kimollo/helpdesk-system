package com.helpdesk.dto;

import java.time.Instant;

public record CommentResponse(
        Long id,
        String body,
        boolean internalNote,
        UserResponse author,
        Instant createdAt
) {
}

package com.helpdesk.mapper;

import com.helpdesk.domain.Ticket;
import com.helpdesk.domain.TicketComment;
import com.helpdesk.domain.UserAccount;
import com.helpdesk.dto.CommentResponse;
import com.helpdesk.dto.TicketResponse;
import com.helpdesk.dto.UserResponse;

public final class ApiMapper {

    private ApiMapper() {
    }

    public static UserResponse toUser(UserAccount user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }

    public static TicketResponse toTicket(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCategory(),
                toUser(ticket.getRequester()),
                toUser(ticket.getAssignee()),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getResolvedAt()
        );
    }

    public static CommentResponse toComment(TicketComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getBody(),
                comment.isInternalNote(),
                toUser(comment.getAuthor()),
                comment.getCreatedAt()
        );
    }
}

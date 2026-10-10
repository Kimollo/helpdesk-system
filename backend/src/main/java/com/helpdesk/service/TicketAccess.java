package com.helpdesk.service;

import com.helpdesk.domain.Role;
import com.helpdesk.domain.Ticket;
import com.helpdesk.exception.ForbiddenOperationException;
import com.helpdesk.exception.NotFoundException;
import com.helpdesk.repository.TicketRepository;
import com.helpdesk.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TicketAccess {

    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public Ticket requireReadable(Long ticketId, UserPrincipal actor) {
        Ticket ticket = ticketRepository.findWithPartiesById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));
        if (actor.getRole() == Role.CUSTOMER && !actor.getId().equals(ticket.getRequester().getId())) {
            throw new ForbiddenOperationException("You can only access your own tickets");
        }
        return ticket;
    }

    @Transactional(readOnly = true)
    public Ticket requireManageable(Long ticketId, UserPrincipal actor) {
        if (actor.getRole() == Role.CUSTOMER) {
            throw new ForbiddenOperationException("Only agents can update ticket workflow");
        }
        return requireReadable(ticketId, actor);
    }
}

package com.helpdesk.service;

import com.helpdesk.domain.Role;
import com.helpdesk.domain.Ticket;
import com.helpdesk.domain.UserAccount;
import com.helpdesk.exception.ForbiddenOperationException;
import com.helpdesk.repository.TicketRepository;
import com.helpdesk.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketAccessTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketAccess ticketAccess;

    @Test
    void customerCannotReadAnotherCustomersTicket() {
        Ticket ticket = ticketOwnedBy(7L);
        when(ticketRepository.findWithPartiesById(10L)).thenReturn(Optional.of(ticket));

        UserPrincipal otherCustomer = new UserPrincipal(8L, "other@helpdesk.local", "hash", Role.CUSTOMER, true);

        assertThrows(ForbiddenOperationException.class, () -> ticketAccess.requireReadable(10L, otherCustomer));
    }

    @Test
    void customerCanReadOwnTicket() {
        Ticket ticket = ticketOwnedBy(7L);
        when(ticketRepository.findWithPartiesById(10L)).thenReturn(Optional.of(ticket));

        UserPrincipal owner = new UserPrincipal(7L, "owner@helpdesk.local", "hash", Role.CUSTOMER, true);

        assertEquals(10L, ticketAccess.requireReadable(10L, owner).getId());
    }

    @Test
    void customerCannotManageWorkflow() {
        UserPrincipal customer = new UserPrincipal(7L, "owner@helpdesk.local", "hash", Role.CUSTOMER, true);

        assertThrows(ForbiddenOperationException.class, () -> ticketAccess.requireManageable(10L, customer));
    }

    @Test
    void agentCanReadAnyTicket() {
        Ticket ticket = ticketOwnedBy(7L);
        when(ticketRepository.findWithPartiesById(10L)).thenReturn(Optional.of(ticket));

        UserPrincipal agent = new UserPrincipal(3L, "agent@helpdesk.local", "hash", Role.AGENT, true);

        assertEquals(10L, ticketAccess.requireReadable(10L, agent).getId());
    }

    private Ticket ticketOwnedBy(Long ownerId) {
        UserAccount owner = new UserAccount();
        owner.setId(ownerId);
        owner.setEmail("owner@helpdesk.local");
        owner.setRole(Role.CUSTOMER);
        Ticket ticket = new Ticket();
        ticket.setId(10L);
        ticket.setRequester(owner);
        return ticket;
    }
}

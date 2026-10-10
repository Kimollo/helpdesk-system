package com.helpdesk.service;

import com.helpdesk.domain.Role;
import com.helpdesk.domain.Ticket;
import com.helpdesk.domain.TicketStatus;
import com.helpdesk.domain.UserAccount;
import com.helpdesk.dto.UpdateTicketRequest;
import com.helpdesk.repository.TicketRepository;
import com.helpdesk.repository.UserRepository;
import com.helpdesk.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceHistoryTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketAccess ticketAccess;

    @Mock
    private TicketHistoryService historyService;

    @InjectMocks
    private TicketService ticketService;

    private Ticket ticket;
    private UserPrincipal actor;

    @BeforeEach
    void setUp() {
        UserAccount requester = new UserAccount();
        requester.setId(4L);
        requester.setRole(Role.CUSTOMER);

        ticket = new Ticket();
        ticket.setId(10L);
        ticket.setSubject("VPN");
        ticket.setDescription("Drops after a minute");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setRequester(requester);

        actor = new UserPrincipal(2L, "agent@helpdesk.local", "hash", Role.AGENT, true);
        when(ticketAccess.requireManageable(10L, actor)).thenReturn(ticket);
    }

    @Test
    void statusChangeIsRecorded() {
        ticketService.update(10L, new UpdateTicketRequest(TicketStatus.IN_PROGRESS, null, null, null, false), actor);

        verify(historyService).recordStatus(ticket, actor, TicketStatus.OPEN, TicketStatus.IN_PROGRESS);
        verify(historyService, never()).recordAssignment(any(), any(), any(), any(), any());
    }

    @Test
    void assignmentChangeIsRecorded() {
        UserAccount agent = new UserAccount();
        agent.setId(3L);
        agent.setRole(Role.AGENT);
        agent.setEnabled(true);
        when(userRepository.findById(3L)).thenReturn(Optional.of(agent));

        ticketService.update(10L, new UpdateTicketRequest(null, null, null, 3L, false), actor);

        verify(historyService).recordAssignment(ticket, actor, TicketStatus.OPEN, null, agent);
        verify(historyService, never()).recordStatus(any(), any(), any(), any());
    }

    @Test
    void unchangedWorkflowWritesNoHistory() {
        UserAccount agent = new UserAccount();
        agent.setId(3L);
        ticket.setAssignee(agent);
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        ticketService.update(10L, new UpdateTicketRequest(TicketStatus.IN_PROGRESS, null, null, 3L, false), actor);

        verify(historyService, never()).recordStatus(any(), any(), any(), any());
        verify(historyService, never()).recordAssignment(any(), any(), any(), any(), any());
    }
}

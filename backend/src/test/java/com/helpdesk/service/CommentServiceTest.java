package com.helpdesk.service;

import com.helpdesk.domain.Role;
import com.helpdesk.domain.Ticket;
import com.helpdesk.domain.TicketComment;
import com.helpdesk.domain.UserAccount;
import com.helpdesk.dto.CreateCommentRequest;
import com.helpdesk.repository.TicketCommentRepository;
import com.helpdesk.repository.UserRepository;
import com.helpdesk.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private TicketCommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TicketAccess ticketAccess;

    @InjectMocks
    private CommentService commentService;

    @Test
    void customerCannotCreateInternalNote() {
        UserAccount customer = new UserAccount();
        customer.setId(7L);
        customer.setEmail("owner@helpdesk.local");
        customer.setFirstName("Lydia");
        customer.setLastName("Njeri");
        customer.setRole(Role.CUSTOMER);
        customer.setEnabled(true);

        Ticket ticket = new Ticket();
        ticket.setId(10L);
        ticket.setRequester(customer);

        UserPrincipal actor = UserPrincipal.from(customer);
        when(ticketAccess.requireReadable(10L, actor)).thenReturn(ticket);
        when(userRepository.findById(7L)).thenReturn(Optional.of(customer));
        when(commentRepository.save(any(TicketComment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        commentService.add(10L, new CreateCommentRequest("Please check again", true), actor);

        ArgumentCaptor<TicketComment> captor = ArgumentCaptor.forClass(TicketComment.class);
        verify(commentRepository).save(captor.capture());
        assertFalse(captor.getValue().isInternalNote());
    }
}

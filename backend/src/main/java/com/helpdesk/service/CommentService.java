package com.helpdesk.service;

import com.helpdesk.domain.Role;
import com.helpdesk.domain.TicketComment;
import com.helpdesk.domain.UserAccount;
import com.helpdesk.dto.CommentResponse;
import com.helpdesk.dto.CreateCommentRequest;
import com.helpdesk.exception.NotFoundException;
import com.helpdesk.mapper.ApiMapper;
import com.helpdesk.repository.TicketCommentRepository;
import com.helpdesk.repository.UserRepository;
import com.helpdesk.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final TicketCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final TicketAccess ticketAccess;

    @Transactional(readOnly = true)
    public List<CommentResponse> list(Long ticketId, UserPrincipal actor) {
        ticketAccess.requireReadable(ticketId, actor);
        List<TicketComment> comments = actor.getRole() == Role.CUSTOMER
                ? commentRepository.findByTicketIdAndInternalNoteFalseOrderByCreatedAtAsc(ticketId)
                : commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        return comments.stream().map(ApiMapper::toComment).toList();
    }

    @Transactional
    public CommentResponse add(Long ticketId, CreateCommentRequest request, UserPrincipal actor) {
        var ticket = ticketAccess.requireReadable(ticketId, actor);
        UserAccount author = userRepository.findById(actor.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setBody(request.body().trim());
        comment.setInternalNote(actor.getRole() != Role.CUSTOMER && request.internalNote());
        return ApiMapper.toComment(commentRepository.save(comment));
    }
}

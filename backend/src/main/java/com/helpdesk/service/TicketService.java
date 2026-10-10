package com.helpdesk.service;

import com.helpdesk.domain.Role;
import com.helpdesk.domain.Ticket;
import com.helpdesk.domain.TicketCategory;
import com.helpdesk.domain.TicketPriority;
import com.helpdesk.domain.TicketStatus;
import com.helpdesk.domain.UserAccount;
import com.helpdesk.dto.CreateTicketRequest;
import com.helpdesk.dto.PageResponse;
import com.helpdesk.dto.TicketResponse;
import com.helpdesk.dto.TicketStatsResponse;
import com.helpdesk.dto.UpdateTicketRequest;
import com.helpdesk.exception.BadRequestException;
import com.helpdesk.exception.ForbiddenOperationException;
import com.helpdesk.exception.NotFoundException;
import com.helpdesk.mapper.ApiMapper;
import com.helpdesk.repository.TicketRepository;
import com.helpdesk.repository.UserRepository;
import com.helpdesk.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketAccess ticketAccess;

    @Transactional
    public TicketResponse create(CreateTicketRequest request, UserPrincipal actor) {
        UserAccount requester = userRepository.findById(actor.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Ticket ticket = new Ticket();
        ticket.setSubject(request.subject().trim());
        ticket.setDescription(request.description().trim());
        ticket.setPriority(request.priority());
        ticket.setCategory(request.category());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setRequester(requester);
        return ApiMapper.toTicket(ticketRepository.save(ticket));
    }

    @Transactional(readOnly = true)
    public TicketResponse get(Long id, UserPrincipal actor) {
        return ApiMapper.toTicket(ticketAccess.requireReadable(id, actor));
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> list(
            TicketStatus status,
            TicketPriority priority,
            TicketCategory category,
            String q,
            String queue,
            int page,
            int size,
            UserPrincipal actor
    ) {
        Long requesterId = null;
        Long assigneeId = null;
        boolean unassignedOnly = false;

        if (actor.getRole() == Role.CUSTOMER) {
            requesterId = actor.getId();
        } else {
            String normalized = queue == null ? "" : queue.trim().toLowerCase();
            switch (normalized) {
                case "", "all" -> {
                }
                case "mine" -> assigneeId = actor.getId();
                case "unassigned" -> unassignedOnly = true;
                case "reported" -> requesterId = actor.getId();
                default -> throw new BadRequestException("Unknown queue '" + queue + "'");
            }
        }

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 50),
                Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("id"))
        );
        Page<Ticket> result = ticketRepository.findAll(
                TicketSpecifications.filter(status, priority, category, requesterId, assigneeId, unassignedOnly, q),
                pageable
        );
        return PageResponse.from(result, ApiMapper::toTicket);
    }

    @Transactional(readOnly = true)
    public TicketStatsResponse stats(UserPrincipal actor) {
        Long requesterId = actor.getRole() == Role.CUSTOMER ? actor.getId() : null;
        long open = count(requesterId, TicketStatus.OPEN);
        long inProgress = count(requesterId, TicketStatus.IN_PROGRESS);
        long waiting = count(requesterId, TicketStatus.WAITING_ON_CUSTOMER);
        long resolved = count(requesterId, TicketStatus.RESOLVED);
        long closed = count(requesterId, TicketStatus.CLOSED);
        return new TicketStatsResponse(open, inProgress, waiting, resolved, closed, open + inProgress + waiting + resolved + closed);
    }

    @Transactional
    public TicketResponse update(Long id, UpdateTicketRequest request, UserPrincipal actor) {
        Ticket ticket = ticketAccess.requireManageable(id, actor);

        if (request.status() != null && request.status() != ticket.getStatus()) {
            ticket.setStatus(request.status());
            if (request.status() == TicketStatus.RESOLVED || request.status() == TicketStatus.CLOSED) {
                if (ticket.getResolvedAt() == null) {
                    ticket.setResolvedAt(Instant.now());
                }
            } else {
                ticket.setResolvedAt(null);
            }
        }
        if (request.priority() != null) {
            ticket.setPriority(request.priority());
        }
        if (request.category() != null) {
            ticket.setCategory(request.category());
        }
        if (request.clearAssignee()) {
            ticket.setAssignee(null);
        } else if (request.assigneeId() != null) {
            ticket.setAssignee(resolveAssignee(request.assigneeId()));
        }
        return ApiMapper.toTicket(ticket);
    }

    @Transactional
    public void delete(Long id, UserPrincipal actor) {
        if (actor.getRole() != Role.ADMIN) {
            throw new ForbiddenOperationException("Only administrators can delete tickets");
        }
        Ticket ticket = ticketAccess.requireReadable(id, actor);
        ticketRepository.delete(ticket);
    }

    private UserAccount resolveAssignee(Long assigneeId) {
        UserAccount assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new NotFoundException("Assignee not found"));
        if (assignee.getRole() == Role.CUSTOMER) {
            throw new BadRequestException("Tickets can only be assigned to agents or administrators");
        }
        if (!assignee.isEnabled()) {
            throw new BadRequestException("Assignee is disabled");
        }
        return assignee;
    }

    private long count(Long requesterId, TicketStatus status) {
        if (requesterId == null) {
            return ticketRepository.countByStatus(status);
        }
        return ticketRepository.countByRequesterIdAndStatus(requesterId, status);
    }
}

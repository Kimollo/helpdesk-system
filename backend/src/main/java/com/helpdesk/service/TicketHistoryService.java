package com.helpdesk.service;

import com.helpdesk.domain.Ticket;
import com.helpdesk.domain.TicketHistory;
import com.helpdesk.domain.TicketHistoryType;
import com.helpdesk.domain.TicketStatus;
import com.helpdesk.domain.UserAccount;
import com.helpdesk.dto.TicketHistoryResponse;
import com.helpdesk.exception.NotFoundException;
import com.helpdesk.mapper.ApiMapper;
import com.helpdesk.repository.TicketHistoryRepository;
import com.helpdesk.repository.UserRepository;
import com.helpdesk.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TicketHistoryService {

    private final TicketHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final TicketAccess ticketAccess;

    @Transactional(readOnly = true)
    public List<TicketHistoryResponse> list(Long ticketId, UserPrincipal actor) {
        ticketAccess.requireReadable(ticketId, actor);
        return historyRepository.findByTicketIdOrderByChangedAtDesc(ticketId).stream()
                .map(ApiMapper::toHistory)
                .toList();
    }

    @Transactional
    public void recordStatus(Ticket ticket, UserPrincipal actor, TicketStatus oldStatus, TicketStatus newStatus) {
        if (oldStatus == newStatus) {
            return;
        }
        TicketHistory entry = base(ticket, actor, oldStatus, newStatus);
        entry.setChangeType(TicketHistoryType.STATUS);
        historyRepository.save(entry);
    }

    @Transactional
    public void recordAssignment(Ticket ticket,
                                 UserPrincipal actor,
                                 TicketStatus status,
                                 UserAccount oldAssignee,
                                 UserAccount newAssignee) {
        if (Objects.equals(idOf(oldAssignee), idOf(newAssignee))) {
            return;
        }
        TicketHistory entry = base(ticket, actor, status, status);
        entry.setChangeType(TicketHistoryType.ASSIGNMENT);
        entry.setOldAssignee(oldAssignee);
        entry.setNewAssignee(newAssignee);
        historyRepository.save(entry);
    }

    private TicketHistory base(Ticket ticket, UserPrincipal actor, TicketStatus oldStatus, TicketStatus newStatus) {
        TicketHistory entry = new TicketHistory();
        entry.setTicket(ticket);
        entry.setChangedBy(userRepository.findById(actor.getId())
                .orElseThrow(() -> new NotFoundException("User not found")));
        entry.setOldStatus(oldStatus);
        entry.setNewStatus(newStatus);
        return entry;
    }

    private static Long idOf(UserAccount user) {
        return user == null ? null : user.getId();
    }
}

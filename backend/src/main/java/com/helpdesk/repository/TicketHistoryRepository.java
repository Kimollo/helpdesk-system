package com.helpdesk.repository;

import com.helpdesk.domain.TicketHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {

    @EntityGraph(attributePaths = {"changedBy", "oldAssignee", "newAssignee"})
    List<TicketHistory> findByTicketIdOrderByChangedAtDesc(Long ticketId);
}

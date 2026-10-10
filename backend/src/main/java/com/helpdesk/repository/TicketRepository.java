package com.helpdesk.repository;

import com.helpdesk.domain.Ticket;
import com.helpdesk.domain.TicketStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    @EntityGraph(attributePaths = {"requester", "assignee"})
    Optional<Ticket> findWithPartiesById(Long id);

    long countByStatus(TicketStatus status);

    long countByRequesterIdAndStatus(Long requesterId, TicketStatus status);
}

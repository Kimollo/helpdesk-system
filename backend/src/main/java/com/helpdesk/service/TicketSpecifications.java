package com.helpdesk.service;

import com.helpdesk.domain.Ticket;
import com.helpdesk.domain.TicketCategory;
import com.helpdesk.domain.TicketPriority;
import com.helpdesk.domain.TicketStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class TicketSpecifications {

    private TicketSpecifications() {
    }

    public static Specification<Ticket> filter(
            TicketStatus status,
            TicketPriority priority,
            TicketCategory category,
            Long requesterId,
            Long assigneeId,
            boolean unassignedOnly,
            String queryText
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (requesterId != null) {
                predicates.add(cb.equal(root.get("requester").get("id"), requesterId));
            }
            if (unassignedOnly) {
                predicates.add(cb.isNull(root.get("assignee")));
            } else if (assigneeId != null) {
                predicates.add(cb.equal(root.get("assignee").get("id"), assigneeId));
            }
            if (queryText != null && !queryText.isBlank()) {
                String like = "%" + queryText.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("subject")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}

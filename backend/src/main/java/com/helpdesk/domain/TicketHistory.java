package com.helpdesk.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "ticket_history", indexes = {
        @Index(name = "idx_ticket_history_ticket", columnList = "ticket_id")
})
@Getter
@Setter
@NoArgsConstructor
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    private UserAccount changedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketHistoryType changeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", nullable = false, length = 32)
    private TicketStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 32)
    private TicketStatus newStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_assignee_id")
    private UserAccount oldAssignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_assignee_id")
    private UserAccount newAssignee;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    @PrePersist
    void onCreate() {
        if (changedAt == null) {
            changedAt = Instant.now();
        }
    }
}

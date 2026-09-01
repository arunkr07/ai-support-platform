package com.arun.aisupportplatform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tickets",
        indexes = {
                @Index(name = "idx_ticket_customer", columnList = "customer_id"),
                @Index(name = "idx_ticket_agent", columnList = "agent_id"),
                @Index(name = "idx_ticket_status", columnList = "status"),
                @Index(name = "idx_ticket_priority", columnList = "priority"),
                @Index(name = "idx_ticket_agent_status", columnList = "agent_id, status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPriority priority;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "agent_id")
    private User assignedAgent;
}
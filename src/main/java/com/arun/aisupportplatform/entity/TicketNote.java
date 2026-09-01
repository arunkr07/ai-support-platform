package com.arun.aisupportplatform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ticket_notes",
        indexes = {
                @Index(
                        name = "idx_note_ticket",
                        columnList = "ticket_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "agent_id", nullable = false)
    private User agent;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
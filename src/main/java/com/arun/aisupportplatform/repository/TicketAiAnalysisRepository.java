package com.arun.aisupportplatform.repository;

import com.arun.aisupportplatform.entity.Ticket;
import com.arun.aisupportplatform.entity.TicketAiAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketAiAnalysisRepository
        extends JpaRepository<TicketAiAnalysis, Long> {

    Optional<TicketAiAnalysis> findByTicket(Ticket ticket);

    void deleteByTicket(Ticket ticket);
}
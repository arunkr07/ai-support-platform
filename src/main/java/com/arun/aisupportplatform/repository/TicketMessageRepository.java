package com.arun.aisupportplatform.repository;

import com.arun.aisupportplatform.entity.Ticket;
import com.arun.aisupportplatform.entity.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketMessageRepository
        extends JpaRepository<TicketMessage, Long> {

    List<TicketMessage> findByTicketOrderByCreatedAtAsc(Ticket ticket);
}
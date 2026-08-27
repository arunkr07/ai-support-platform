package com.arun.aisupportplatform.repository;

import com.arun.aisupportplatform.entity.Ticket;
import com.arun.aisupportplatform.entity.TicketNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketNoteRepository
        extends JpaRepository<TicketNote, Long> {

    List<TicketNote> findByTicket(Ticket ticket);

    void deleteByTicket(Ticket ticket);
}

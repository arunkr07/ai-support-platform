package com.arun.aisupportplatform.repository;

import com.arun.aisupportplatform.entity.Ticket;
import com.arun.aisupportplatform.entity.TicketPriority;
import com.arun.aisupportplatform.entity.TicketStatus;
import com.arun.aisupportplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCustomer(User customer);

    Optional<Ticket> findByIdAndCustomer(Long id, User customer);

    List<Ticket> findByAssignedAgent(User assignedAgent);

    Optional<Ticket> findByIdAndAssignedAgent(Long id,User assignedAgent);

    List<Ticket> findByAssignedAgentAndStatus(User assignedAgent, TicketStatus status);

    List<Ticket> findByAssignedAgentAndPriority(User assignedAgent, TicketPriority priority);

    List<Ticket> findByAssignedAgentAndStatusAndPriority(User assignedAgent, TicketStatus status, TicketPriority priority);

    List<Ticket> findByAssignedAgentAndTitleContainingIgnoreCase(User assignedAgent, String title);

    long countByAssignedAgent(User assignedAgent);

    long countByAssignedAgentAndStatus(User assignedAgent, TicketStatus status);

    List<Ticket> findByAssignedAgentIsNull();

    long countByStatus(TicketStatus status);

    long countByAssignedAgentIsNull();

    List<Ticket> findByTitleContainingIgnoreCase(String title);

}
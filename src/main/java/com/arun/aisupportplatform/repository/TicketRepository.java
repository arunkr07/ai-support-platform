package com.arun.aisupportplatform.repository;

import com.arun.aisupportplatform.entity.Ticket;
import com.arun.aisupportplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCustomer(User customer);

    Optional<Ticket> findByIdAndCustomer(Long id, User customer);

}
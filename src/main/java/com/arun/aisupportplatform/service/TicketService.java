package com.arun.aisupportplatform.service;

import com.arun.aisupportplatform.dto.TicketResponse;
import com.arun.aisupportplatform.entity.Ticket;
import com.arun.aisupportplatform.entity.TicketPriority;
import com.arun.aisupportplatform.entity.TicketStatus;
import com.arun.aisupportplatform.entity.User;
import com.arun.aisupportplatform.exception.TicketNotFoundException;
import com.arun.aisupportplatform.repository.TicketRepository;
import com.arun.aisupportplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketResponse createTicket(
            String email,
            String title,
            String description,
            TicketPriority priority
    ) {

        User customer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        LocalDateTime now = LocalDateTime.now();

        Ticket ticket = Ticket.builder()
                .title(title)
                .description(description)
                .status(TicketStatus.OPEN)
                .priority(priority)
                .customer(customer)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);

        return TicketResponse.builder()
                .id(savedTicket.getId())
                .title(savedTicket.getTitle())
                .description(savedTicket.getDescription())
                .status(savedTicket.getStatus())
                .priority(savedTicket.getPriority())
                .customerId(customer.getId())
                .customerName(customer.getName())
                .customerEmail(customer.getEmail())
                .createdAt(savedTicket.getCreatedAt())
                .updatedAt(savedTicket.getUpdatedAt())
                .build();
    }

    public TicketResponse getTicketById(
            Long ticketId,
            String email
    ) {

        User customer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Optional<Ticket> ticketOptional =
                ticketRepository.findByIdAndCustomer(ticketId, customer);

        System.out.println("TICKET FOUND: " + ticketOptional.isPresent());

        if (ticketOptional.isEmpty()) {
            throw new TicketNotFoundException("Ticket not found");
        }

        Ticket ticket = ticketOptional.get();

        return TicketResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .customerId(customer.getId())
                .customerName(customer.getName())
                .customerEmail(customer.getEmail())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    public List<TicketResponse> getMyTickets(String email) {

        User customer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        List<Ticket> tickets =
                ticketRepository.findByCustomer(customer);

        return tickets.stream()
                .map(ticket -> TicketResponse.builder()
                        .id(ticket.getId())
                        .title(ticket.getTitle())
                        .description(ticket.getDescription())
                        .status(ticket.getStatus())
                        .priority(ticket.getPriority())
                        .customerId(customer.getId())
                        .customerName(customer.getName())
                        .customerEmail(customer.getEmail())
                        .createdAt(ticket.getCreatedAt())
                        .updatedAt(ticket.getUpdatedAt())
                        .build())
                .toList();
    }

    public TicketResponse updateTicket(
            Long ticketId,
            String email,
            String title,
            String description,
            TicketPriority priority
    ) {

        User customer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Ticket ticket = ticketRepository
                .findByIdAndCustomer(ticketId, customer)
                .orElseThrow(() ->
                        new TicketNotFoundException("Ticket not found"));

        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setPriority(priority);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket updatedTicket = ticketRepository.save(ticket);

        return TicketResponse.builder()
                .id(updatedTicket.getId())
                .title(updatedTicket.getTitle())
                .description(updatedTicket.getDescription())
                .status(updatedTicket.getStatus())
                .priority(updatedTicket.getPriority())
                .customerId(customer.getId())
                .customerName(customer.getName())
                .customerEmail(customer.getEmail())
                .createdAt(updatedTicket.getCreatedAt())
                .updatedAt(updatedTicket.getUpdatedAt())
                .build();
    }

    public void deleteTicket(
            Long ticketId,
            String email
    ) {

        User customer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Ticket ticket = ticketRepository
                .findByIdAndCustomer(ticketId, customer)
                .orElseThrow(() ->
                        new TicketNotFoundException("Ticket not found"));

        ticketRepository.delete(ticket);
    }
}
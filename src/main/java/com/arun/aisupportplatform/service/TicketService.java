package com.arun.aisupportplatform.service;

import com.arun.aisupportplatform.dto.AgentDashboardResponse;
import com.arun.aisupportplatform.dto.TicketMessageResponse;
import com.arun.aisupportplatform.dto.TicketNoteResponse;
import com.arun.aisupportplatform.dto.TicketResponse;
import com.arun.aisupportplatform.entity.*;
import com.arun.aisupportplatform.exception.TicketAlreadyAssignedException;
import com.arun.aisupportplatform.exception.TicketNotFoundException;
import com.arun.aisupportplatform.repository.TicketMessageRepository;
import com.arun.aisupportplatform.repository.TicketNoteRepository;
import com.arun.aisupportplatform.repository.TicketRepository;
import com.arun.aisupportplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
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
    private final TicketNoteRepository ticketNoteRepository;
    private final TicketMessageRepository ticketMessageRepository;

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

    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll()
                .stream()
                .map(ticket -> TicketResponse.builder()
                        .id(ticket.getId())
                        .title(ticket.getTitle())
                        .description(ticket.getDescription())
                        .status(ticket.getStatus())
                        .priority(ticket.getPriority())
                        .customerId(ticket.getCustomer().getId())
                        .customerName(ticket.getCustomer().getName())
                        .customerEmail(ticket.getCustomer().getEmail())
                        .createdAt(ticket.getCreatedAt())
                        .updatedAt(ticket.getUpdatedAt())
                        .build())
                .toList();
    }

    public TicketResponse claimTicket(
            Long ticketId,
            String email
    ) {

        User agent = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Agent not found"));

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new TicketNotFoundException("Ticket not found"));

        if (ticket.getAssignedAgent() != null) {
            throw new TicketAlreadyAssignedException(
                    "Ticket is already assigned"
            );
        }

        ticket.setAssignedAgent(agent);

        ticket.setStatus(TicketStatus.IN_PROGRESS);

        Ticket savedTicket = ticketRepository.save(ticket);

        return TicketResponse.builder()
                .id(savedTicket.getId())
                .title(savedTicket.getTitle())
                .description(savedTicket.getDescription())
                .status(savedTicket.getStatus())
                .priority(savedTicket.getPriority())
                .customerId(savedTicket.getCustomer().getId())
                .customerName(savedTicket.getCustomer().getName())
                .customerEmail(savedTicket.getCustomer().getEmail())
                .createdAt(savedTicket.getCreatedAt())
                .updatedAt(savedTicket.getUpdatedAt())
                .build();
    }

    public List<TicketResponse> getAssignedTickets(String email) {

        User agent = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Agent not found"));

        return ticketRepository.findByAssignedAgent(agent)
                .stream()
                .map(ticket -> TicketResponse.builder()
                        .id(ticket.getId())
                        .title(ticket.getTitle())
                        .description(ticket.getDescription())
                        .status(ticket.getStatus())
                        .priority(ticket.getPriority())
                        .customerId(ticket.getCustomer().getId())
                        .customerName(ticket.getCustomer().getName())
                        .customerEmail(ticket.getCustomer().getEmail())
                        .createdAt(ticket.getCreatedAt())
                        .updatedAt(ticket.getUpdatedAt())
                        .build())
                .toList();
    }

    public TicketResponse updateTicketStatus(
            Long ticketId,
            String email,
            TicketStatus status
    ) {

        User agent = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Agent not found"));

        Ticket ticket = ticketRepository
                .findByIdAndAssignedAgent(ticketId, agent)
                .orElseThrow(() ->
                        new TicketNotFoundException("Ticket not found"));

        ticket.setStatus(status);

        Ticket savedTicket = ticketRepository.save(ticket);

        return TicketResponse.builder()
                .id(savedTicket.getId())
                .title(savedTicket.getTitle())
                .description(savedTicket.getDescription())
                .status(savedTicket.getStatus())
                .priority(savedTicket.getPriority())
                .customerId(savedTicket.getCustomer().getId())
                .customerName(savedTicket.getCustomer().getName())
                .customerEmail(savedTicket.getCustomer().getEmail())
                .createdAt(savedTicket.getCreatedAt())
                .updatedAt(savedTicket.getUpdatedAt())
                .build();
    }

    public TicketResponse releaseTicket(
            Long ticketId,
            String email
    ) {

        User agent = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Agent not found"));

        Ticket ticket = ticketRepository
                .findByIdAndAssignedAgent(ticketId, agent)
                .orElseThrow(() ->
                        new TicketNotFoundException("Ticket not found"));

        ticket.setAssignedAgent(null);
        ticket.setStatus(TicketStatus.OPEN);

        Ticket savedTicket = ticketRepository.save(ticket);

        return TicketResponse.builder()
                .id(savedTicket.getId())
                .title(savedTicket.getTitle())
                .description(savedTicket.getDescription())
                .status(savedTicket.getStatus())
                .priority(savedTicket.getPriority())
                .customerId(savedTicket.getCustomer().getId())
                .customerName(savedTicket.getCustomer().getName())
                .customerEmail(savedTicket.getCustomer().getEmail())
                .createdAt(savedTicket.getCreatedAt())
                .updatedAt(savedTicket.getUpdatedAt())
                .build();
    }

    public TicketNoteResponse addInternalNote(
            Long ticketId,
            String email,
            String content
    ) {

        User agent = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Agent not found"));

        Ticket ticket = ticketRepository
                .findByIdAndAssignedAgent(ticketId, agent)
                .orElseThrow(() ->
                        new TicketNotFoundException("Ticket not found"));

        TicketNote note = TicketNote.builder()
                .content(content)
                .ticket(ticket)
                .agent(agent)
                .createdAt(LocalDateTime.now())
                .build();

        TicketNote savedNote = ticketNoteRepository.save(note);

        return new TicketNoteResponse(
                savedNote.getId(),
                savedNote.getContent(),
                savedNote.getTicket().getId(),
                savedNote.getAgent().getId(),
                savedNote.getAgent().getName(),
                savedNote.getCreatedAt()
        );
    }

    public TicketMessageResponse sendAgentMessage(
            Long ticketId,
            String email,
            String content
    ) {

        User agent = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Agent not found"));

        Ticket ticket = ticketRepository
                .findByIdAndAssignedAgent(ticketId, agent)
                .orElseThrow(() ->
                        new TicketNotFoundException("Ticket not found"));

        TicketMessage message = TicketMessage.builder()
                .content(content)
                .ticket(ticket)
                .sender(agent)
                .createdAt(LocalDateTime.now())
                .build();

        TicketMessage savedMessage =
                ticketMessageRepository.save(message);

        return new TicketMessageResponse(
                savedMessage.getId(),
                savedMessage.getContent(),
                agent.getId(),
                agent.getName(),
                agent.getRole().name(),
                savedMessage.getCreatedAt()
        );
    }

    public List<TicketMessageResponse> getAgentMessages(
            Long ticketId,
            String email
    ) {

        User agent = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Agent not found"));

        Ticket ticket = ticketRepository
                .findByIdAndAssignedAgent(ticketId, agent)
                .orElseThrow(() ->
                        new TicketNotFoundException("Ticket not found"));

        return ticketMessageRepository
                .findByTicketOrderByCreatedAtAsc(ticket)
                .stream()
                .map(message -> new TicketMessageResponse(
                        message.getId(),
                        message.getContent(),
                        message.getSender().getId(),
                        message.getSender().getName(),
                        message.getSender().getRole().name(),
                        message.getCreatedAt()
                ))
                .toList();
    }

    public List<TicketMessageResponse> getCustomerMessages(
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

        return ticketMessageRepository
                .findByTicketOrderByCreatedAtAsc(ticket)
                .stream()
                .map(message -> new TicketMessageResponse(
                        message.getId(),
                        message.getContent(),
                        message.getSender().getId(),
                        message.getSender().getName(),
                        message.getSender().getRole().name(),
                        message.getCreatedAt()
                ))
                .toList();
    }

    public TicketMessageResponse sendCustomerMessage(
            Long ticketId,
            String email,
            String content
    ) {

        User customer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        Ticket ticket = ticketRepository
                .findByIdAndCustomer(ticketId, customer)
                .orElseThrow(() ->
                        new TicketNotFoundException("Ticket not found"));

        TicketMessage message = TicketMessage.builder()
                .content(content)
                .ticket(ticket)
                .sender(customer)
                .createdAt(LocalDateTime.now())
                .build();

        TicketMessage savedMessage =
                ticketMessageRepository.save(message);

        return new TicketMessageResponse(
                savedMessage.getId(),
                savedMessage.getContent(),
                customer.getId(),
                customer.getName(),
                customer.getRole().name(),
                savedMessage.getCreatedAt()
        );
    }

    public List<TicketResponse> getAgentTickets(
            String email,
            TicketStatus status,
            TicketPriority priority,
            String search
    ) {

        User agent = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Agent not found"));

        List<Ticket> tickets;

        if (search != null && !search.isBlank()) {

            tickets = ticketRepository
                    .findByAssignedAgentAndTitleContainingIgnoreCase(
                            agent,
                            search
                    );

        } else if (status != null && priority != null) {

            tickets = ticketRepository
                    .findByAssignedAgentAndStatusAndPriority(
                            agent,
                            status,
                            priority
                    );

        } else if (status != null) {

            tickets = ticketRepository
                    .findByAssignedAgentAndStatus(
                            agent,
                            status
                    );

        } else if (priority != null) {

            tickets = ticketRepository
                    .findByAssignedAgentAndPriority(
                            agent,
                            priority
                    );

        } else {

            tickets = ticketRepository
                    .findByAssignedAgent(agent);
        }

        return tickets.stream()
                .map(ticket -> TicketResponse.builder()
                        .id(ticket.getId())
                        .title(ticket.getTitle())
                        .description(ticket.getDescription())
                        .status(ticket.getStatus())
                        .priority(ticket.getPriority())
                        .customerId(ticket.getCustomer().getId())
                        .customerName(ticket.getCustomer().getName())
                        .customerEmail(ticket.getCustomer().getEmail())
                        .createdAt(ticket.getCreatedAt())
                        .updatedAt(ticket.getUpdatedAt())
                        .build())
                .toList();
    }

    public AgentDashboardResponse getAgentDashboard(String email) {

        User agent = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Agent not found"));

        long totalTickets =
                ticketRepository.countByAssignedAgent(agent);

        long openTickets =
                ticketRepository.countByAssignedAgentAndStatus(
                        agent,
                        TicketStatus.OPEN
                );

        long inProgressTickets =
                ticketRepository.countByAssignedAgentAndStatus(
                        agent,
                        TicketStatus.IN_PROGRESS
                );

        long resolvedTickets =
                ticketRepository.countByAssignedAgentAndStatus(
                        agent,
                        TicketStatus.RESOLVED
                );

        return new AgentDashboardResponse(
                totalTickets,
                openTickets,
                inProgressTickets,
                resolvedTickets
        );
    }
}
package com.arun.aisupportplatform.service;

import com.arun.aisupportplatform.ai.dto.TicketAiAnalysisResponse;
import com.arun.aisupportplatform.ai.dto.TicketAnalysisRequest;
import com.arun.aisupportplatform.ai.dto.TicketAnalysisResponse;
import com.arun.aisupportplatform.ai.service.TicketAnalysisService;
import com.arun.aisupportplatform.dto.*;
import com.arun.aisupportplatform.entity.*;
import com.arun.aisupportplatform.exception.AgentNotFoundException;
import com.arun.aisupportplatform.exception.TicketAlreadyAssignedException;
import com.arun.aisupportplatform.exception.TicketNotFoundException;
import com.arun.aisupportplatform.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketNoteRepository ticketNoteRepository;
    private final TicketMessageRepository ticketMessageRepository;
    private final TicketAiAnalysisRepository ticketAiAnalysisRepository;
    private final TicketAnalysisService ticketAnalysisService;

    @Transactional
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

        try {

            TicketAnalysisResponse analysis =
                    ticketAnalysisService.analyzeTicket(
                            new TicketAnalysisRequest(
                                    savedTicket.getTitle(),
                                    savedTicket.getDescription()
                            )
                    );

            TicketAiAnalysis aiAnalysis =
                    TicketAiAnalysis.builder()
                            .ticket(savedTicket)
                            .summary(analysis.summary())
                            .category(analysis.category())
                            .suggestedPriority(analysis.suggestedPriority())
                            .createdAt(LocalDateTime.now())
                            .build();

            ticketAiAnalysisRepository.save(aiAnalysis);

        } catch (Exception exception) {
            log.error(
                    "AI analysis failed for ticket {}",
                    savedTicket.getId(),
                    exception
            );
        }

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

    @Transactional
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

        validateTicketIsNotClosed(ticket);

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

    @Transactional
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

    public List<TicketResponse> getAllTickets(
            TicketStatus status,
            TicketPriority priority,
            String search,
            String sort
    ) {

        List<Ticket> tickets;

        if (search != null && !search.isBlank()) {

            tickets = ticketRepository
                    .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                            search,
                            search
                    );

            if (status != null) {
                tickets = tickets.stream()
                        .filter(ticket -> ticket.getStatus() == status)
                        .toList();
            }

            if (priority != null) {
                tickets = tickets.stream()
                        .filter(ticket -> ticket.getPriority() == priority)
                        .toList();
            }

        } else if (status != null && priority != null) {

            tickets = ticketRepository
                    .findByStatusAndPriority(status, priority);

        } else if (status != null) {

            tickets = ticketRepository.findByStatus(status);

        } else if (priority != null) {

            tickets = ticketRepository.findByPriority(priority);

        } else {

            tickets = ticketRepository.findAll();
        }

        if (sort != null && !sort.isBlank()) {

            String[] sortParts = sort.split(",");

            String field = sortParts[0];

            Sort.Direction direction =
                    sortParts.length > 1
                            ? Sort.Direction.fromString(sortParts[1])
                            : Sort.Direction.ASC;

            tickets = tickets.stream()
                    .sorted((ticket1, ticket2) -> {

                        Comparable value1;
                        Comparable value2;

                        switch (field) {

                            case "id" -> {
                                value1 = ticket1.getId();
                                value2 = ticket2.getId();
                            }

                            case "title" -> {
                                value1 = ticket1.getTitle();
                                value2 = ticket2.getTitle();
                            }

                            case "createdAt" -> {
                                value1 = ticket1.getCreatedAt();
                                value2 = ticket2.getCreatedAt();
                            }

                            case "updatedAt" -> {
                                value1 = ticket1.getUpdatedAt();
                                value2 = ticket2.getUpdatedAt();
                            }

                            default -> throw new IllegalArgumentException(
                                    "Invalid sort field: " + field
                            );
                        }

                        int result = value1.compareTo(value2);

                        return direction.isAscending()
                                ? result
                                : -result;
                    })
                    .toList();
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

    @Transactional
    public TicketResponse reopenTicket(
            Long ticketId,
            String email
    ) {

        User customer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        Ticket ticket = ticketRepository
                .findByIdAndCustomer(ticketId, customer)
                .orElseThrow(() ->
                        new TicketNotFoundException("Ticket not found"));

        if (ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new IllegalArgumentException(
                    "Only resolved tickets can be reopened"
            );
        }

        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setUpdatedAt(LocalDateTime.now());

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

    @Transactional
    public TicketResponse claimTicket(
            Long ticketId,
            String email
    ) {

        User agent = getAgent(email);

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

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        List<Ticket> tickets;

        if (user.getRole() == UserRole.ADMIN) {

            tickets = ticketRepository.findAll()
                    .stream()
                    .filter(ticket -> ticket.getAssignedAgent() != null)
                    .toList();

        } else if (user.getRole() == UserRole.AGENT) {

            tickets = ticketRepository.findByAssignedAgent(user);

        } else {

            throw new RuntimeException("User is not authorized");
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

    @Transactional
    public TicketResponse updateTicketStatus(
            Long ticketId,
            String email,
            TicketStatus status
    ) {

        User agent = getAgent(email);

        Ticket ticket = ticketRepository
                .findByIdAndAssignedAgent(ticketId, agent)
                .orElseThrow(() ->
                        new TicketNotFoundException("Ticket not found"));

        if (status == TicketStatus.CLOSED) {
            throw new IllegalArgumentException(
                    "Agents cannot close tickets"
            );
        }


        validateStatusTransition(
                ticket.getStatus(),
                status
        );

        ticket.setStatus(status);
        ticket.setUpdatedAt(LocalDateTime.now());

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

    @Transactional
    public TicketResponse releaseTicket(
            Long ticketId,
            String email
    ) {

        User agent = getAgent(email);

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

    @Transactional
    public TicketNoteResponse addInternalNote(
            Long ticketId,
            String email,
            String content
    ) {

        User agent = getAgent(email);

        Ticket ticket = ticketRepository
                .findByIdAndAssignedAgent(ticketId, agent)
                .orElseThrow(() ->
                        new TicketNotFoundException("Ticket not found"));

        validateTicketIsNotClosed(ticket);

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

    @Transactional
    public TicketMessageResponse sendAgentMessage(
            Long ticketId,
            String email,
            String content
    ) {

        User agent = getAgent(email);

        Ticket ticket = ticketRepository
                .findByIdAndAssignedAgent(ticketId, agent)
                .orElseThrow(() ->
                        new TicketNotFoundException("Ticket not found"));

        validateTicketIsNotClosed(ticket);

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

    public TicketResponse getAgentTicketById(
            Long ticketId,
            String email
    ) {

        User agent = getAgent(email);

        Ticket ticket = ticketRepository
                .findByIdAndAssignedAgent(ticketId, agent)
                .orElseThrow(() ->
                        new TicketNotFoundException("Ticket not found"));

        return TicketResponse.builder()
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
                .build();
    }

    public List<TicketMessageResponse> getAgentMessages(
            Long ticketId,
            String email
    ) {

        User agent = getAgent(email);

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

    @Transactional
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

        validateTicketIsNotClosed(ticket);

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

        User agent = getAgent(email);

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

        User agent = getAgent(email);

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

    @Transactional
    public TicketResponse assignTicket(
            Long ticketId,
            Long agentId
    ) {

        Ticket ticket = ticketRepository
                .findById(ticketId)
                .orElseThrow(() ->
                        new TicketNotFoundException("Ticket not found"));

        User agent = userRepository
                .findById(agentId)
                .orElseThrow(() ->
                        new RuntimeException("Agent not found"));

        if (!agent.getRole().equals(UserRole.AGENT)) {
            throw new RuntimeException("User is not an agent");
        }

        if (ticket.getAssignedAgent() != null) {
            throw new TicketAlreadyAssignedException(
                    "Ticket is already assigned"
            );
        }

        ticket.setAssignedAgent(agent);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setUpdatedAt(LocalDateTime.now());

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

    public List<TicketResponse> getUnassignedTickets() {

        return ticketRepository.findByAssignedAgentIsNull()
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

    @Transactional
    public TicketResponse reassignTicket(
            Long ticketId,
            Long agentId
    ) {

        Ticket ticket = ticketRepository
                .findById(ticketId)
                .orElseThrow(() ->
                        new TicketNotFoundException("Ticket not found"));

        User agent = userRepository
                .findById(agentId)
                .orElseThrow(() ->
                        new RuntimeException("Agent not found"));

        if (!agent.getRole().equals(UserRole.AGENT)) {
            throw new RuntimeException("User is not an agent");
        }

        ticket.setAssignedAgent(agent);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setUpdatedAt(LocalDateTime.now());

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

    @Transactional
    public TicketResponse adminUpdateTicketStatus(
            Long ticketId,
            TicketStatus status
    ) {

        Ticket ticket = ticketRepository
                .findById(ticketId)
                .orElseThrow(() ->
                        new TicketNotFoundException("Ticket not found"));

        validateStatusTransition(
                ticket.getStatus(),
                status
        );

        ticket.setStatus(status);
        ticket.setUpdatedAt(LocalDateTime.now());

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

    public AdminDashboardResponse getAdminDashboard() {

        long totalTickets = ticketRepository.count();

        long openTickets =
                ticketRepository.countByStatus(TicketStatus.OPEN);

        long inProgressTickets =
                ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);

        long resolvedTickets =
                ticketRepository.countByStatus(TicketStatus.RESOLVED);

        long unassignedTickets =
                ticketRepository.countByAssignedAgentIsNull();

        long totalAgents =
                userRepository.findByRole(UserRole.AGENT).size();

        long totalCustomers =
                userRepository.findByRole(UserRole.CUSTOMER).size();

        return new AdminDashboardResponse(
                totalTickets,
                openTickets,
                inProgressTickets,
                resolvedTickets,
                unassignedTickets,
                totalAgents,
                totalCustomers
        );
    }

    public List<TicketResponse> searchTickets(String search) {

        List<Ticket> tickets;

        if (search == null || search.isBlank()) {
            tickets = ticketRepository.findAll();
        } else {
            tickets = ticketRepository
                    .findByTitleContainingIgnoreCase(search);
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

    public TicketResponse getAdminTicketById(Long ticketId) {

        Ticket ticket = ticketRepository
                .findById(ticketId)
                .orElseThrow(() ->
                        new TicketNotFoundException("Ticket not found"));

        return TicketResponse.builder()
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
                .build();
    }

    @Transactional
    public void adminDeleteTicket(Long ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new TicketNotFoundException("Ticket not found"));

        ticketMessageRepository.deleteByTicket(ticket);

        ticketNoteRepository.deleteByTicket(ticket);

        ticketRepository.delete(ticket);
    }

    public TicketAiAnalysisResponse getTicketAiAnalysis(
            Long ticketId,
            String email
    ) {

        User agent = getAgent(email);

        Ticket ticket = ticketRepository
                .findByIdAndAssignedAgent(ticketId, agent)
                .orElseThrow(() ->
                        new TicketNotFoundException("Ticket not found"));

        TicketAiAnalysis analysis =
                ticketAiAnalysisRepository
                        .findByTicket(ticket)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "AI analysis not found"
                                ));

        return new TicketAiAnalysisResponse(
                analysis.getId(),
                ticket.getId(),
                analysis.getSummary(),
                analysis.getCategory(),
                analysis.getSuggestedPriority(),
                analysis.getCreatedAt()
        );
    }

    private User getAgent(String email) {

        User agent = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new AgentNotFoundException("Agent not found"));

        if (agent.getRole() != UserRole.AGENT) {
            throw new RuntimeException("User is not an agent");
        }

        return agent;
    }

    private void validateStatusTransition(
            TicketStatus currentStatus,
            TicketStatus newStatus
    ) {

        if (currentStatus == newStatus) {
            throw new IllegalArgumentException(
                    "Ticket is already in " + currentStatus + " status"
            );
        }

        boolean valid = switch (currentStatus) {

            case OPEN ->
                    newStatus == TicketStatus.IN_PROGRESS;

            case IN_PROGRESS ->
                    newStatus == TicketStatus.OPEN
                            || newStatus == TicketStatus.RESOLVED;

            case RESOLVED ->
                    newStatus == TicketStatus.CLOSED
                            || newStatus == TicketStatus.IN_PROGRESS;

            case CLOSED -> false;
        };

        if (!valid) {
            throw new IllegalArgumentException(
                    "Invalid status transition from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }
    }

    private void validateTicketIsNotClosed(Ticket ticket) {

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new IllegalArgumentException(
                    "Closed tickets cannot be modified"
            );
        }
    }

}
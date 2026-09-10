package com.arun.aisupportplatform.controller;

import com.arun.aisupportplatform.entity.Ticket;
import com.arun.aisupportplatform.entity.TicketPriority;
import com.arun.aisupportplatform.entity.TicketStatus;
import com.arun.aisupportplatform.entity.User;
import com.arun.aisupportplatform.entity.UserRole;
import com.arun.aisupportplatform.repository.TicketRepository;
import com.arun.aisupportplatform.repository.UserRepository;
import com.arun.aisupportplatform.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User customer;
    private User agent;

    private String agentToken;
    private String customerToken;

    @BeforeEach
    void setUp() {

        customer = userRepository.save(
                User.builder()
                        .name("Test Customer")
                        .email("agent-test-customer@example.com")
                        .password(passwordEncoder.encode("Password123"))
                        .role(UserRole.CUSTOMER)
                        .build()
        );

        agent = userRepository.save(
                User.builder()
                        .name("Test Agent")
                        .email("agent-test-agent@example.com")
                        .password(passwordEncoder.encode("Password123"))
                        .role(UserRole.AGENT)
                        .build()
        );

        agentToken = jwtService.generateToken(agent.getEmail());
        customerToken = jwtService.generateToken(customer.getEmail());
    }

    @Test
    void agentCanViewAssignedTickets() throws Exception {

        Ticket ticket = createTicket(
                "Payment issue",
                "Customer cannot complete payment",
                TicketStatus.IN_PROGRESS,
                TicketPriority.HIGH
        );

        ticket.setAssignedAgent(agent);
        ticketRepository.save(ticket);

        mockMvc.perform(
                        get("/api/agent/tickets/assigned")
                                .header("Authorization", "Bearer " + agentToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Payment issue")));
    }

    @Test
    void agentCanClaimOpenTicket() throws Exception {

        Ticket ticket = createTicket(
                "Cannot login",
                "Customer cannot login",
                TicketStatus.OPEN,
                TicketPriority.MEDIUM
        );

        mockMvc.perform(
                        put("/api/agent/tickets/" + ticket.getId() + "/claim")
                                .header("Authorization", "Bearer " + agentToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        Ticket updatedTicket = ticketRepository.findById(ticket.getId())
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertNotNull(
                updatedTicket.getAssignedAgent()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                agent.getId(),
                updatedTicket.getAssignedAgent().getId()
        );

    }


    @Test
    void agentCannotClaimAlreadyAssignedTicket() throws Exception {

        User anotherAgent = userRepository.save(
                User.builder()
                        .name("Another Agent")
                        .email("another-agent@example.com")
                        .password(passwordEncoder.encode("Password123"))
                        .role(UserRole.AGENT)
                        .build()
        );

        Ticket ticket = createTicket(
                "Already assigned",
                "This ticket is already assigned",
                TicketStatus.IN_PROGRESS,
                TicketPriority.HIGH
        );

        ticket.setAssignedAgent(anotherAgent);
        ticketRepository.save(ticket);

        mockMvc.perform(
                        put("/api/agent/tickets/" + ticket.getId() + "/claim")
                                .header("Authorization", "Bearer " + agentToken)
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    void agentCanReleaseAssignedTicket() throws Exception {

        Ticket ticket = createTicket(
                "Release test",
                "Testing ticket release",
                TicketStatus.IN_PROGRESS,
                TicketPriority.MEDIUM
        );

        ticket.setAssignedAgent(agent);
        ticketRepository.save(ticket);

        mockMvc.perform(
                        put("/api/agent/tickets/" + ticket.getId() + "/release")
                                .header("Authorization", "Bearer " + agentToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OPEN")));
    }

    @Test
    void agentCanUpdateAssignedTicketStatus() throws Exception {

        Ticket ticket = createTicket(
                "Status update",
                "Testing status update",
                TicketStatus.IN_PROGRESS,
                TicketPriority.HIGH
        );

        ticket.setAssignedAgent(agent);
        ticketRepository.save(ticket);

        String requestBody = """
            {
                "status": "RESOLVED"
            }
            """;

        mockMvc.perform(
                        put("/api/agent/tickets/" + ticket.getId() + "/status")
                                .header("Authorization", "Bearer " + agentToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")));
    }

    @Test
    void agentCannotUpdateUnassignedTicketStatus() throws Exception {

        Ticket ticket = createTicket(
                "Unassigned status",
                "Agent should not modify this ticket",
                TicketStatus.OPEN,
                TicketPriority.MEDIUM
        );

        String requestBody = """
            {
                "status": "IN_PROGRESS"
            }
            """;

        mockMvc.perform(
                        put("/api/agent/tickets/" + ticket.getId() + "/status")
                                .header("Authorization", "Bearer " + agentToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    void agentCannotUseInvalidStatusTransition() throws Exception {

        Ticket ticket = createTicket(
                "Invalid transition",
                "Testing invalid status transition",
                TicketStatus.OPEN,
                TicketPriority.MEDIUM
        );

        ticket.setAssignedAgent(agent);
        ticketRepository.save(ticket);

        String requestBody = """
            {
                "status": "RESOLVED"
            }
            """;

        mockMvc.perform(
                        put("/api/agent/tickets/" + ticket.getId() + "/status")
                                .header("Authorization", "Bearer " + agentToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    void agentCanSendMessage() throws Exception {

        Ticket ticket = createTicket(
                "Message test",
                "Testing agent messaging",
                TicketStatus.IN_PROGRESS,
                TicketPriority.MEDIUM
        );

        ticket.setAssignedAgent(agent);
        ticketRepository.save(ticket);

        String requestBody = """
            {
                "content": "We are investigating your issue."
            }
            """;

        mockMvc.perform(
                        post("/api/agent/tickets/" + ticket.getId() + "/messages")
                                .header("Authorization", "Bearer " + agentToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("We are investigating your issue.")));
    }

    @Test
    void agentCanGetMessages() throws Exception {

        Ticket ticket = createTicket(
                "Get messages",
                "Testing message retrieval",
                TicketStatus.IN_PROGRESS,
                TicketPriority.MEDIUM
        );

        ticket.setAssignedAgent(agent);
        ticketRepository.save(ticket);

        String requestBody = """
            {
                "content": "Test agent message"
            }
            """;

        mockMvc.perform(
                        post("/api/agent/tickets/" + ticket.getId() + "/messages")
                                .header("Authorization", "Bearer " + agentToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/agent/tickets/" + ticket.getId() + "/messages")
                                .header("Authorization", "Bearer " + agentToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].content", is("Test agent message")));
    }

    @Test
    void customerCannotAccessAgentEndpoint() throws Exception {

        mockMvc.perform(
                        get("/api/agent/tickets/assigned")
                                .header("Authorization", "Bearer " + customerToken)
                )
                .andExpect(status().isForbidden());
    }

    private Ticket createTicket(
            String title,
            String description,
            TicketStatus status,
            TicketPriority priority
    ) {

        Ticket ticket = Ticket.builder()
                .title(title)
                .description(description)
                .status(status)
                .priority(priority)
                .customer(customer)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return ticketRepository.save(ticket);
    }

}

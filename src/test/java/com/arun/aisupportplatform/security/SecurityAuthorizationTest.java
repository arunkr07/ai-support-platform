package com.arun.aisupportplatform.security;

import com.arun.aisupportplatform.entity.Ticket;
import com.arun.aisupportplatform.entity.TicketPriority;
import com.arun.aisupportplatform.entity.TicketStatus;
import com.arun.aisupportplatform.entity.User;
import com.arun.aisupportplatform.entity.UserRole;
import com.arun.aisupportplatform.repository.TicketRepository;
import com.arun.aisupportplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("mock")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityAuthorizationTest {

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
    private User secondCustomer;
    private User agent;
    private User secondAgent;
    private User admin;

    private String customerToken;
    private String secondCustomerToken;
    private String agentToken;
    private String secondAgentToken;
    private String adminToken;

    @BeforeEach
    void setUp() {

        customer = createUser(
                "Customer One",
                "security-customer@example.com",
                UserRole.CUSTOMER
        );

        secondCustomer = createUser(
                "Customer Two",
                "security-customer-two@example.com",
                UserRole.CUSTOMER
        );

        agent = createUser(
                "Agent One",
                "security-agent@example.com",
                UserRole.AGENT
        );

        secondAgent = createUser(
                "Agent Two",
                "security-agent-two@example.com",
                UserRole.AGENT
        );

        admin = createUser(
                "Admin",
                "security-admin@example.com",
                UserRole.ADMIN
        );

        customerToken = jwtService.generateToken(customer.getEmail());
        secondCustomerToken =
                jwtService.generateToken(secondCustomer.getEmail());

        agentToken = jwtService.generateToken(agent.getEmail());
        secondAgentToken =
                jwtService.generateToken(secondAgent.getEmail());

        adminToken = jwtService.generateToken(admin.getEmail());
    }

    @Test
    void unauthenticatedUserCannotAccessProtectedEndpoint()
            throws Exception {

        mockMvc.perform(
                        get("/api/users/me")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(
                        jsonPath(
                                "$.message",
                                is("Authentication required")
                        )
                );
    }

    @Test
    void invalidJwtCannotAccessProtectedEndpoint()
            throws Exception {

        mockMvc.perform(
                        get("/api/users/me")
                                .header(
                                        "Authorization",
                                        "Bearer invalid-token"
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(
                        jsonPath(
                                "$.message",
                                is("Invalid or expired token")
                        )
                );
    }

    @Test
    void customerCannotAccessAgentEndpoint()
            throws Exception {

        mockMvc.perform(
                        get("/api/agent/tickets/assigned")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(
                        jsonPath(
                                "$.message",
                                is("Access denied")
                        )
                );
    }

    @Test
    void customerCannotAccessAdminEndpoint()
            throws Exception {

        mockMvc.perform(
                        get("/api/admin/tickets")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(
                        jsonPath(
                                "$.message",
                                is("Access denied")
                        )
                );
    }

    @Test
    void agentCannotAccessAdminEndpoint()
            throws Exception {

        mockMvc.perform(
                        get("/api/admin/tickets")
                                .header(
                                        "Authorization",
                                        "Bearer " + agentToken
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(
                        jsonPath(
                                "$.message",
                                is("Access denied")
                        )
                );
    }

    @Test
    void agentCanAccessAgentEndpoint()
            throws Exception {

        mockMvc.perform(
                        get("/api/agent/tickets/assigned")
                                .header(
                                        "Authorization",
                                        "Bearer " + agentToken
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void adminCanAccessAdminEndpoint()
            throws Exception {

        mockMvc.perform(
                        get("/api/admin/tickets")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void adminCanAccessAgentEndpoint()
            throws Exception {

        mockMvc.perform(
                        get("/api/agent/tickets/assigned")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void customerCannotAccessAnotherCustomersTicket()
            throws Exception {

        Ticket ticket = createTicket(
                secondCustomer,
                "Private ticket",
                "This belongs to another customer",
                TicketStatus.OPEN,
                TicketPriority.MEDIUM
        );

        mockMvc.perform(
                        get("/api/tickets/" + ticket.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath(
                                "$.message",
                                is("Ticket not found")
                        )
                );
    }

    @Test
    void agentCannotUpdateUnassignedTicketStatus()
            throws Exception {

        Ticket ticket = createTicket(
                customer,
                "Unassigned ticket",
                "This ticket has no assigned agent",
                TicketStatus.OPEN,
                TicketPriority.MEDIUM
        );

        String requestBody = """
                {
                    "status": "IN_PROGRESS"
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/agent/tickets/"
                                        + ticket.getId()
                                        + "/status"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + agentToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void agentCannotModifyAnotherAgentsTicket()
            throws Exception {

        Ticket ticket = createTicket(
                customer,
                "Another agent ticket",
                "This ticket belongs to another agent",
                TicketStatus.IN_PROGRESS,
                TicketPriority.MEDIUM
        );

        ticket.setAssignedAgent(secondAgent);
        ticketRepository.save(ticket);

        String requestBody = """
                {
                    "status": "RESOLVED"
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/agent/tickets/"
                                        + ticket.getId()
                                        + "/status"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + agentToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void assignedAgentCanModifyOwnTicket()
            throws Exception {

        Ticket ticket = createTicket(
                customer,
                "My assigned ticket",
                "This ticket belongs to the current agent",
                TicketStatus.IN_PROGRESS,
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
                        put(
                                "/api/agent/tickets/"
                                        + ticket.getId()
                                        + "/status"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + agentToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.status",
                                is("RESOLVED")
                        )
                );

        Ticket updatedTicket =
                ticketRepository.findById(ticket.getId())
                        .orElseThrow();

        assertEquals(
                TicketStatus.RESOLVED,
                updatedTicket.getStatus()
        );

        assertNotNull(
                updatedTicket.getAssignedAgent()
        );

        assertEquals(
                agent.getId(),
                updatedTicket.getAssignedAgent().getId()
        );
    }

    @Test
    void generatedJwtCanAuthenticateUser()
            throws Exception {

        String token =
                jwtService.generateToken(customer.getEmail());

        mockMvc.perform(
                        get("/api/users/me")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }

    private User createUser(
            String name,
            String email,
            UserRole role
    ) {

        return userRepository.save(
                User.builder()
                        .name(name)
                        .email(email)
                        .password(
                                passwordEncoder.encode("Password123")
                        )
                        .role(role)
                        .build()
        );
    }

    private Ticket createTicket(
            User customer,
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
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();

        return ticketRepository.save(ticket);
    }
}
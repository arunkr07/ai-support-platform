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
import org.springframework.test.context.ActiveProfiles;
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

@ActiveProfiles("mock")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminControllerTest {

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

    private User admin;
    private User agent;
    private User customer;

    private String adminToken;
    private String agentToken;
    private String customerToken;

    @BeforeEach
    void setUp() {

        admin = userRepository.save(
                User.builder()
                        .name("Test Admin")
                        .email("admin-test@example.com")
                        .password(passwordEncoder.encode("Password123"))
                        .role(UserRole.ADMIN)
                        .build()
        );

        agent = userRepository.save(
                User.builder()
                        .name("Test Agent")
                        .email("admin-test-agent@example.com")
                        .password(passwordEncoder.encode("Password123"))
                        .role(UserRole.AGENT)
                        .build()
        );

        customer = userRepository.save(
                User.builder()
                        .name("Test Customer")
                        .email("admin-test-customer@example.com")
                        .password(passwordEncoder.encode("Password123"))
                        .role(UserRole.CUSTOMER)
                        .build()
        );

        adminToken = jwtService.generateToken(admin.getEmail());
        agentToken = jwtService.generateToken(agent.getEmail());
        customerToken = jwtService.generateToken(customer.getEmail());
    }

    @Test
    void adminCanGetAllTickets() throws Exception {

        createTicket(
                "Admin ticket",
                "Testing admin ticket list",
                TicketStatus.OPEN,
                TicketPriority.HIGH
        );

        mockMvc.perform(
                        get("/api/admin/tickets")
                                .header("Authorization", "Bearer " + adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Admin ticket")));
    }

    @Test
    void adminCanAssignTicketToAgent() throws Exception {

        Ticket ticket = createTicket(
                "Assignment test",
                "Testing assignment",
                TicketStatus.OPEN,
                TicketPriority.MEDIUM
        );

        String requestBody = """
        {
            "agentId": %d
        }
        """.formatted(agent.getId());

        mockMvc.perform(
                        post("/api/admin/tickets/" + ticket.getId() + "/assign")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
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
    void adminCanReassignTicket() throws Exception {

        User secondAgent = userRepository.save(
                User.builder()
                        .name("Second Agent")
                        .email("second-admin-agent@example.com")
                        .password(passwordEncoder.encode("Password123"))
                        .role(UserRole.AGENT)
                        .build()
        );

        Ticket ticket = createTicket(
                "Reassignment test",
                "Testing reassignment",
                TicketStatus.IN_PROGRESS,
                TicketPriority.HIGH
        );

        ticket.setAssignedAgent(agent);
        ticketRepository.save(ticket);

        String requestBody = """
        {
            "agentId": %d
        }
        """.formatted(secondAgent.getId());

        mockMvc.perform(
                        put("/api/admin/tickets/" + ticket.getId() + "/reassign")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk());

        Ticket updatedTicket = ticketRepository.findById(ticket.getId())
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertNotNull(
                updatedTicket.getAssignedAgent()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                secondAgent.getId(),
                updatedTicket.getAssignedAgent().getId()
        );
    }


    @Test
    void adminCanUpdateTicketStatus() throws Exception {

        Ticket ticket = createTicket(
                "Admin status",
                "Testing admin status update",
                TicketStatus.IN_PROGRESS,
                TicketPriority.MEDIUM
        );

        String requestBody = """
            {
                "status": "RESOLVED"
            }
            """;

        mockMvc.perform(
                        put("/api/admin/tickets/" + ticket.getId() + "/status")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")));
    }

    @Test
    void adminCanGetUnassignedTickets() throws Exception {

        createTicket(
                "Unassigned ticket",
                "Testing unassigned tickets",
                TicketStatus.OPEN,
                TicketPriority.LOW
        );

        mockMvc.perform(
                        get("/api/admin/tickets/unassigned")
                                .header("Authorization", "Bearer " + adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Unassigned ticket")));
    }

    @Test
    void adminCanGetAllAgents() throws Exception {

        mockMvc.perform(
                        get("/api/admin/agents")
                                .header("Authorization", "Bearer " + adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email", is(agent.getEmail())));
    }

    @Test
    void adminCanGetAllCustomers() throws Exception {

        mockMvc.perform(
                        get("/api/admin/customers")
                                .header("Authorization", "Bearer " + adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email", is(customer.getEmail())));
    }

    @Test
    void adminCanCreateAgent() throws Exception {

        String requestBody = """
            {
                "name": "New Agent",
                "email": "new-agent@example.com",
                "password": "Password123"
            }
            """;

        mockMvc.perform(
                        post("/api/admin/agents")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Agent")))
                .andExpect(jsonPath("$.email", is("new-agent@example.com")));
    }

    @Test
    void adminCanGetTicketById() throws Exception {

        Ticket ticket = createTicket(
                "Ticket details",
                "Testing admin ticket details",
                TicketStatus.OPEN,
                TicketPriority.HIGH
        );

        mockMvc.perform(
                        get("/api/admin/tickets/" + ticket.getId())
                                .header("Authorization", "Bearer " + adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ticket.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Ticket details")));
    }

    @Test
    void nonAdminCannotAccessAdminEndpoints() throws Exception {

        mockMvc.perform(
                        get("/api/admin/tickets")
                                .header("Authorization", "Bearer " + agentToken)
                )
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        get("/api/admin/tickets")
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

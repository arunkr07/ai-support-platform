package com.arun.aisupportplatform.controller;

import com.arun.aisupportplatform.ai.dto.TicketAnalysisResponse;
import com.arun.aisupportplatform.ai.service.TicketAnalysisService;
import com.arun.aisupportplatform.entity.Ticket;
import com.arun.aisupportplatform.entity.TicketPriority;
import com.arun.aisupportplatform.entity.TicketStatus;
import com.arun.aisupportplatform.entity.User;
import com.arun.aisupportplatform.entity.UserRole;
import com.arun.aisupportplatform.repository.TicketRepository;
import com.arun.aisupportplatform.repository.UserRepository;
import com.arun.aisupportplatform.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private TicketAnalysisService ticketAnalysisService;

    private User customer;

    private User secondCustomer;

    private String customerToken;

    private String secondCustomerToken;

    @BeforeEach
    void setUp() {

        customer = User.builder()
                .name("Test Customer")
                .email("customer@test.com")
                .password(passwordEncoder.encode("Password123"))
                .role(UserRole.CUSTOMER)
                .build();

        customer = userRepository.save(customer);

        secondCustomer = User.builder()
                .name("Second Customer")
                .email("customer2@test.com")
                .password(passwordEncoder.encode("Password123"))
                .role(UserRole.CUSTOMER)
                .build();

        secondCustomer = userRepository.save(secondCustomer);

        customerToken = jwtService.generateToken(customer.getEmail());

        secondCustomerToken =
                jwtService.generateToken(secondCustomer.getEmail());

        when(ticketAnalysisService.analyzeTicket(any()))
                .thenReturn(
                        new TicketAnalysisResponse(
                                "Test ticket summary",
                                com.arun.aisupportplatform.entity.TicketCategory.OTHER,
                                com.arun.aisupportplatform.entity.AiSuggestedPriority.MEDIUM
                        )
                );
    }

    @Test
    void customerCanCreateTicket() throws Exception {

        String request = """
            {
                "title": "Payment failed",
                "description": "My payment failed while placing the order",
                "priority": "HIGH"
            }
            """;

        mockMvc.perform(
                        post("/api/tickets")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title")
                        .value("Payment failed"))
                .andExpect(jsonPath("$.description")
                        .value("My payment failed while placing the order"))
                .andExpect(jsonPath("$.priority")
                        .value("HIGH"))
                .andExpect(jsonPath("$.status")
                        .value("OPEN"))
                .andExpect(jsonPath("$.customerEmail")
                        .value("customer@test.com"));
    }

    @Test
    void invalidTicketDataReturnsBadRequest() throws Exception {

        String request = """
            {
                "title": "",
                "description": "",
                "priority": null
            }
            """;

        mockMvc.perform(
                        post("/api/tickets")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void customerCanGetTheirTickets() throws Exception {

        createTicketForCustomer(
                customer,
                "Payment issue",
                "Payment failed",
                TicketPriority.HIGH
        );

        createTicketForCustomer(
                customer,
                "Login issue",
                "Unable to login",
                TicketPriority.MEDIUM
        );

        mockMvc.perform(
                        get("/api/tickets")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2))
                .andExpect(jsonPath("$[0].customerEmail")
                        .value("customer@test.com"))
                .andExpect(jsonPath("$[1].customerEmail")
                        .value("customer@test.com"));
    }

    @Test
    void customerCanGetTheirOwnTicket() throws Exception {

        Ticket ticket = createTicketForCustomer(
                customer,
                "Payment issue",
                "Payment failed",
                TicketPriority.HIGH
        );

        mockMvc.perform(
                        get("/api/tickets/" + ticket.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(ticket.getId()))
                .andExpect(jsonPath("$.title")
                        .value("Payment issue"))
                .andExpect(jsonPath("$.customerEmail")
                        .value("customer@test.com"));
    }

    @Test
    void customerCannotAccessAnotherCustomersTicket() throws Exception {

        Ticket secondCustomerTicket = createTicketForCustomer(
                secondCustomer,
                "Private ticket",
                "This belongs to another customer",
                TicketPriority.HIGH
        );

        mockMvc.perform(
                        get("/api/tickets/" + secondCustomerTicket.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void customerCanUpdateTheirOwnTicket() throws Exception {

        Ticket ticket = createTicketForCustomer(
                customer,
                "Old title",
                "Old description",
                TicketPriority.LOW
        );

        String request = """
            {
                "title": "Updated title",
                "description": "Updated description",
                "priority": "HIGH"
            }
            """;

        mockMvc.perform(
                        put("/api/tickets/" + ticket.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title")
                        .value("Updated title"))
                .andExpect(jsonPath("$.description")
                        .value("Updated description"))
                .andExpect(jsonPath("$.priority")
                        .value("HIGH"));
    }

    @Test
    void customerCanSendMessage() throws Exception {

        Ticket ticket = createTicketForCustomer(
                customer,
                "Payment issue",
                "Payment failed",
                TicketPriority.HIGH
        );

        String request = """
            {
                "content": "I tried the payment again and it still failed"
            }
            """;

        mockMvc.perform(
                        post("/api/tickets/" + ticket.getId() + "/messages")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content")
                        .value("I tried the payment again and it still failed"))
                .andExpect(jsonPath("$.senderName")
                        .value("Test Customer"))
                .andExpect(jsonPath("$.senderRole")
                        .value("CUSTOMER"));
    }

    @Test
    void customerCanGetMessages() throws Exception {

        Ticket ticket = createTicketForCustomer(
                customer,
                "Payment issue",
                "Payment failed",
                TicketPriority.HIGH
        );

        String request = """
            {
                "content": "Please help me with this issue"
            }
            """;

        mockMvc.perform(
                post("/api/tickets/" + ticket.getId() + "/messages")
                        .header(
                                "Authorization",
                                "Bearer " + customerToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        );

        mockMvc.perform(
                        get("/api/tickets/" + ticket.getId() + "/messages")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(1))
                .andExpect(jsonPath("$[0].content")
                        .value("Please help me with this issue"))
                .andExpect(jsonPath("$[0].senderRole")
                        .value("CUSTOMER"));
    }

    @Test
    void customerCanReopenResolvedTicket() throws Exception {

        Ticket ticket = createTicketForCustomer(
                customer,
                "Resolved issue",
                "This issue was resolved",
                TicketPriority.MEDIUM
        );

        ticket.setStatus(TicketStatus.RESOLVED);
        ticketRepository.save(ticket);

        mockMvc.perform(
                        put("/api/tickets/" + ticket.getId() + "/reopen")
                                .header(
                                        "Authorization",
                                        "Bearer " + customerToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("IN_PROGRESS"));
    }

    @Test
    void unauthenticatedCustomerRequestReturnsUnauthorized()
            throws Exception {

        mockMvc.perform(
                        get("/api/tickets")
                )
                .andExpect(status().isUnauthorized());
    }

    private Ticket createTicketForCustomer(
            User customer,
            String title,
            String description,
            TicketPriority priority
    ) {

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

        return ticketRepository.save(ticket);
    }

}

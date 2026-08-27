package com.arun.aisupportplatform.controller;

import com.arun.aisupportplatform.dto.*;
import com.arun.aisupportplatform.service.TicketService;
import com.arun.aisupportplatform.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TicketService ticketService;
    private final UserService userService;

    public record CreateAgentRequest(
            @NotBlank(message = "Name is required")
            String name,

            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 8, max = 100,
                    message = "Password must be between 8 and 100 characters")
            String password
    ) {
    }

    @GetMapping("/tickets")
    public List<TicketResponse> getAllTickets() {

        return ticketService.getAllTickets();
    }

    @PostMapping("/tickets/{id}/assign")
    public TicketResponse assignTicket(
            @PathVariable Long id,
            @Valid @RequestBody AssignTicketRequest request
    ) {

        return ticketService.assignTicket(
                id,
                request.agentId()
        );
    }

    @GetMapping("/agents")
    public List<AdminUserResponse> getAllAgents() {

        return userService.getAllAgents();
    }

    @GetMapping("/customers")
    public List<AdminUserResponse> getAllCustomers() {

        return userService.getAllCustomers();
    }

    @GetMapping("/tickets/unassigned")
    public List<TicketResponse> getUnassignedTickets() {

        return ticketService.getUnassignedTickets();
    }

    @PutMapping("/tickets/{id}/reassign")
    public TicketResponse reassignTicket(
            @PathVariable Long id,
            @Valid @RequestBody AssignTicketRequest request
    ) {

        return ticketService.reassignTicket(
                id,
                request.agentId()
        );
    }

    @PutMapping("/tickets/{id}/status")
    public TicketResponse updateTicketStatus(
            @PathVariable Long id,
            @RequestBody UpdateTicketStatusRequest request
    ) {

        return ticketService.adminUpdateTicketStatus(
                id,
                request.status()
        );
    }

    @GetMapping("/dashboard")
    public AdminDashboardResponse getDashboard() {

        return ticketService.getAdminDashboard();
    }

    @GetMapping("/tickets/search")
    public List<TicketResponse> searchTickets(
            @RequestParam(required = false) String search
    ) {

        return ticketService.searchTickets(search);
    }

    @GetMapping("/tickets/{id}")
    public TicketResponse getTicketById(
            @PathVariable Long id
    ) {

        return ticketService.getAdminTicketById(id);
    }

    @GetMapping("/users/{id}")
    public AdminUserResponse getUserById(
            @PathVariable Long id
    ) {
        return userService.getUserById(id);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id
    ) {
        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/agents")
    public AdminUserResponse createAgent(
            @Valid @RequestBody CreateAgentRequest request
    ) {
        return userService.createAgent(
                request.name(),
                request.email(),
                request.password()
        );
    }

    @PutMapping("/agents/{id}")
    public AdminUserResponse updateAgent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAgentRequest request
    ) {

        return userService.updateAgent(
                id,
                request.name(),
                request.email()
        );
    }

    @DeleteMapping("/tickets/{id}")
    public ResponseEntity<Void> deleteTicket(
            @PathVariable Long id
    ) {

        ticketService.adminDeleteTicket(id);

        return ResponseEntity.noContent().build();
    }

}
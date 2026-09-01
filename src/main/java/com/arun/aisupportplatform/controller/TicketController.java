package com.arun.aisupportplatform.controller;

import com.arun.aisupportplatform.dto.TicketMessageResponse;
import com.arun.aisupportplatform.dto.TicketResponse;
import com.arun.aisupportplatform.entity.TicketPriority;
import com.arun.aisupportplatform.service.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    public record CreateTicketRequest(
            @NotBlank(message = "Title is required")
            @Size(max = 100, message = "Title must not exceed 100 characters")
            String title,

            @NotBlank(message = "Description is required")
            @Size(max = 1000, message = "Description must not exceed 1000 characters")
            String description,

            @NotNull(message = "Priority is required")
            TicketPriority priority
    ) {
    }

    public record UpdateTicketRequest(

            @NotBlank(message = "Title is required")
            @Size(max = 100, message = "Title must not exceed 100 characters")
            String title,

            @NotBlank(message = "Description is required")
            @Size(max = 1000, message = "Description must not exceed 1000 characters")
            String description,

            @NotNull(message = "Priority is required")
            TicketPriority priority
    ) {
    }

    @PostMapping
    public TicketResponse createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();

        return ticketService.createTicket(
                email,
                request.title(),
                request.description(),
                request.priority()
        );
    }

    @GetMapping
    public List<TicketResponse> getMyTickets(
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ticketService.getMyTickets(email);
    }


    @GetMapping("/{id}")
    public TicketResponse getTicketById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ticketService.getTicketById(
                id,
                email
        );
    }

    @PutMapping("/{id}")
    public TicketResponse updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ticketService.updateTicket(
                id,
                email,
                request.title(),
                request.description(),
                request.priority()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String email = authentication.getName();

        ticketService.deleteTicket(id, email);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/messages")
    public List<TicketMessageResponse> getMessages(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ticketService.getCustomerMessages(id, email);
    }

    @PostMapping("/{id}/messages")
    public TicketMessageResponse sendMessage(
            @PathVariable Long id,
            @Valid @RequestBody AgentController.SendMessageRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ticketService.sendCustomerMessage(
                id,
                email,
                request.content()
        );
    }

    @PutMapping("/{id}/reopen")
    public TicketResponse reopenTicket(
            @PathVariable Long id,
            Authentication authentication
    ) {

        return ticketService.reopenTicket(
                id,
                authentication.getName()
        );
    }

}
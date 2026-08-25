package com.arun.aisupportplatform.controller;

import com.arun.aisupportplatform.dto.TicketResponse;
import com.arun.aisupportplatform.entity.TicketPriority;
import com.arun.aisupportplatform.service.TicketService;
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

    public record UpdateTicketRequest(
            String title,
            String description,
            TicketPriority priority
    ) {
    }

    public record CreateTicketRequest(
            String title,
            String description,
            TicketPriority priority
    ) {
    }

    @PostMapping
    public TicketResponse createTicket(
            @RequestBody CreateTicketRequest request,
            Authentication authentication
    ) {

        System.out.println("========== TICKET REQUEST ==========");
        System.out.println("Authentication: " + authentication);
        System.out.println("User: " + authentication.getName());
        System.out.println("Title: " + request.title());
        System.out.println("Description: " + request.description());
        System.out.println("Priority: " + request.priority());

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
            @RequestBody UpdateTicketRequest request,
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
}
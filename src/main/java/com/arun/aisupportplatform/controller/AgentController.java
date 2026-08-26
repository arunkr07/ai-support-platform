package com.arun.aisupportplatform.controller;

import com.arun.aisupportplatform.dto.AgentDashboardResponse;
import com.arun.aisupportplatform.dto.TicketMessageResponse;
import com.arun.aisupportplatform.dto.TicketNoteResponse;
import com.arun.aisupportplatform.dto.TicketResponse;
import com.arun.aisupportplatform.entity.TicketNote;
import com.arun.aisupportplatform.entity.TicketPriority;
import com.arun.aisupportplatform.entity.TicketStatus;
import com.arun.aisupportplatform.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final TicketService ticketService;

    public record UpdateStatusRequest(
            TicketStatus status
    ) {
    }

    public record AddNoteRequest(
            String content
    ) {
    }

    public record SendMessageRequest(
            String content
    ) {
    }

    @PutMapping("/tickets/{id}/claim")
    public TicketResponse claimTicket(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();

        return ticketService.claimTicket(id, email);
    }

    @GetMapping("/tickets/assigned")
    public List<TicketResponse> getAssignedTickets(
            Authentication authentication
    ) {
        String email = authentication.getName();

        return ticketService.getAssignedTickets(email);
    }

    @PutMapping("/tickets/{id}/status")
    public TicketResponse updateTicketStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();

        return ticketService.updateTicketStatus(
                id,
                email,
                request.status()
        );
    }

    @PutMapping("/tickets/{id}/release")
    public TicketResponse releaseTicket(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();

        return ticketService.releaseTicket(id, email);
    }

    @PostMapping("/tickets/{id}/notes")
    public TicketNoteResponse addInternalNote(
            @PathVariable Long id,
            @RequestBody AddNoteRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ticketService.addInternalNote(
                id,
                email,
                request.content()
        );
    }

    @PostMapping("/tickets/{id}/messages")
    public TicketMessageResponse sendMessage(
            @PathVariable Long id,
            @RequestBody SendMessageRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ticketService.sendAgentMessage(
                id,
                email,
                request.content()
        );
    }

    @GetMapping("/tickets/{id}/messages")
    public List<TicketMessageResponse> getMessages(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ticketService.getAgentMessages(id, email);
    }

    @GetMapping("/tickets")
    public List<TicketResponse> getTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) String search,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ticketService.getAgentTickets(
                email,
                status,
                priority,
                search
        );
    }

    @GetMapping("/dashboard")
    public AgentDashboardResponse getDashboard(
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ticketService.getAgentDashboard(email);
    }
}
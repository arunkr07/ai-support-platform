package com.arun.aisupportplatform.controller;

import com.arun.aisupportplatform.ai.dto.AgentSuggestedReplyRequest;
import com.arun.aisupportplatform.ai.dto.SuggestedReplyRequest;
import com.arun.aisupportplatform.ai.dto.SuggestedReplyResponse;
import com.arun.aisupportplatform.ai.dto.TicketAiAnalysisResponse;
import com.arun.aisupportplatform.ai.service.SuggestedReplyService;
import com.arun.aisupportplatform.dto.*;
import com.arun.aisupportplatform.entity.TicketPriority;
import com.arun.aisupportplatform.entity.TicketStatus;
import com.arun.aisupportplatform.service.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final TicketService ticketService;
    private final SuggestedReplyService suggestedReplyService;

    public record UpdateStatusRequest(
            @NotNull(message = "Status is required")
            TicketStatus status
    ) {
    }

    public record AddNoteRequest(
            @NotBlank(message = "Note content is required")
            @Size(max = 2000, message = "Note must not exceed 2000 characters")
            String content
    ) {
    }

    public record SendMessageRequest(
            @NotBlank(message = "Message content is required")
            @Size(max = 2000, message = "Message must not exceed 2000 characters")
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
            @Valid @RequestBody UpdateStatusRequest request,
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
            @Valid @RequestBody AddNoteRequest request,
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
            @Valid @RequestBody SendMessageRequest request,
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

    @PostMapping("/tickets/{id}/suggest-reply")
    public SuggestedReplyResponse suggestReply(
            @PathVariable Long id,
            @Valid @RequestBody AgentSuggestedReplyRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();

        return suggestedReplyService.generateAgentSuggestedReply(
                id,
                email
        );
    }

    @GetMapping("/tickets/{id}/ai-analysis")
    public TicketAiAnalysisResponse getTicketAiAnalysis(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();

        return ticketService.getTicketAiAnalysis(
                id,
                email
        );
    }

}
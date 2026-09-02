package com.arun.aisupportplatform.ai.controller;

import com.arun.aisupportplatform.ai.dto.*;
import com.arun.aisupportplatform.ai.service.AiService;
import com.arun.aisupportplatform.ai.service.SuggestedReplyService;
import com.arun.aisupportplatform.ai.service.TicketAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;
    private final TicketAnalysisService ticketAnalysisService;
    private final SuggestedReplyService suggestedReplyService;

    public AiController(
            AiService aiService,
            TicketAnalysisService ticketAnalysisService,
            SuggestedReplyService suggestedReplyService
    ) {
        this.aiService = aiService;
        this.ticketAnalysisService = ticketAnalysisService;
        this.suggestedReplyService = suggestedReplyService;
    }

    @PostMapping("/test")
    public ResponseEntity<AiResponse> testAi(
            @Valid @RequestBody AiRequest request
    ) {

        String response = aiService.generateResponse(request.prompt());

        return ResponseEntity.ok(
                new AiResponse(response)
        );
    }

    @PostMapping("/analyze-ticket")
    public ResponseEntity<TicketAnalysisResponse> analyzeTicket(
            @Valid @RequestBody TicketAnalysisRequest request
    ) {

        TicketAnalysisResponse response =
                ticketAnalysisService.analyzeTicket(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/suggest-reply")
    public ResponseEntity<SuggestedReplyResponse> suggestReply(
            @Valid @RequestBody SuggestedReplyRequest request
    ) {
        SuggestedReplyResponse response =
                suggestedReplyService.generateSuggestedReply(request);

        return ResponseEntity.ok(response);
    }
}
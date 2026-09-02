package com.arun.aisupportplatform.ai.service;

import com.arun.aisupportplatform.ai.dto.SuggestedReplyRequest;
import com.arun.aisupportplatform.ai.dto.SuggestedReplyResponse;
import com.arun.aisupportplatform.dto.TicketMessageResponse;
import com.arun.aisupportplatform.dto.TicketResponse;
import com.arun.aisupportplatform.service.TicketService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SuggestedReplyService {

    private final AiService aiService;
    private final TicketService ticketService;

    public SuggestedReplyService(
            AiService aiService,
            TicketService ticketService
    ) {
        this.aiService = aiService;
        this.ticketService = ticketService;
    }

    public SuggestedReplyResponse generateSuggestedReply(
            SuggestedReplyRequest request
    ) {

        String prompt = """
                You are an AI assistant helping a customer support agent.

                Generate a professional, helpful, and empathetic reply
                to the customer's message.

                Customer message:
                %s

                Ticket context:
                %s

                Requirements:
                - Be professional and polite.
                - Be concise.
                - Do not invent information.
                - Do not promise refunds, compensation, or actions
                  that are not mentioned in the context.
                - Do not mention that you are an AI.
                - Return only the suggested reply text.
                """.formatted(
                request.customerMessage(),
                request.ticketContext() == null
                        ? "No additional context provided."
                        : request.ticketContext()
        );

        String reply = aiService.generateResponse(prompt);

        return new SuggestedReplyResponse(reply);
    }

    public SuggestedReplyResponse generateAgentSuggestedReply(
            Long ticketId,
            String agentEmail
    ) {

        TicketResponse ticket =
                ticketService.getAgentTicketById(
                        ticketId,
                        agentEmail
                );

        List<TicketMessageResponse> messages =
                ticketService.getAgentMessages(
                        ticketId,
                        agentEmail
                );

        String latestCustomerMessage = messages.stream()
                .filter(message ->
                        "CUSTOMER".equals(message.senderRole())
                )
                .reduce((first, second)
                        -> second)
                .map(TicketMessageResponse::content)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No customer message found for this ticket"
                        )
                );

        String conversation = messages.stream()
                .map(message ->
                        message.senderRole()
                                + ": "
                                + message.content()
                )
                .reduce(
                        (first, second) ->
                                first + "\n" + second
                )
                .orElse("No previous conversation.");

        String prompt = """
                You are an AI assistant helping a customer support agent.

                Generate a professional, helpful, and empathetic reply
                to the customer's latest message.

                TICKET INFORMATION:

                Title:
                %s

                Description:
                %s

                Current status:
                %s

                Current priority:
                %s

                CONVERSATION HISTORY:

                %s

                LATEST CUSTOMER MESSAGE:

                %s

                Requirements:
                - Reply directly to the customer's latest message.
                - Use the ticket information and conversation history as context.
                - Be professional, polite, and empathetic.
                - Be concise.
                - Do not invent information.
                - Do not promise refunds, compensation, or actions
                  that are not supported by the ticket context.
                - Do not mention that you are an AI.
                - Do not expose internal notes or internal system information.
                - Return only the suggested reply text.
                """.formatted(
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                conversation,
                latestCustomerMessage
        );

        String reply = aiService.generateResponse(prompt);

        return new SuggestedReplyResponse(reply);
    }
}

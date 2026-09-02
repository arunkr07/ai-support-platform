package com.arun.aisupportplatform.ai.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("mock")
public class MockAiService implements AiService {

    @Override
    public String generateResponse(String prompt) {

        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt cannot be empty");
        }

        String lowerPrompt = prompt.toLowerCase();

        if (lowerPrompt.contains("analyze the following customer support ticket")) {
            return """
            {
              "summary": "Customer is experiencing an issue that requires support assistance.",
              "category": "TECHNICAL_SUPPORT",
              "suggestedPriority": "MEDIUM"
            }
            """;
        }

        if (lowerPrompt.contains("reply")) {
            return "Mock reply: Thank you for contacting support. We have received your request and our team is looking into the issue.";
        }

        if (lowerPrompt.contains("summarize")) {
            return "Mock summary: The customer is experiencing an issue and requires assistance from the support team.";
        }

        if (lowerPrompt.contains("category")) {
            return "Mock category: TECHNICAL_SUPPORT";
        }

        if (lowerPrompt.contains("priority")) {
            return "Mock priority: MEDIUM";
        }

        return "Mock AI response: I received your request successfully.";
    }
}
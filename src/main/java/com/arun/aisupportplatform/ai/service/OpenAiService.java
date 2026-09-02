package com.arun.aisupportplatform.ai.service;

import com.arun.aisupportplatform.ai.exception.AiServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@Profile("openai")
public class OpenAiService implements AiService {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public OpenAiService(
            @Value("${openai.base-url}") String baseUrl,
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String generateResponse(String prompt) {

        if (prompt == null || prompt.isBlank()) {
            throw new AiServiceException("Prompt cannot be empty");
        }

        try {

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "input", prompt
            );

            Map<?, ?> response = restClient.post()
                    .uri("/responses")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                throw new AiServiceException("Empty response received from OpenAI");
            }

            Object outputText = response.get("output_text");

            if (outputText instanceof String text && !text.isBlank()) {
                return text;
            }

            throw new AiServiceException("No text response received from OpenAI");

        } catch (AiServiceException exception) {
            throw exception;

        } catch (Exception exception) {
            throw new AiServiceException(
                    "Failed to communicate with OpenAI"
            );
        }
    }
}
package com.arun.aisupportplatform.ai.service;

import com.arun.aisupportplatform.ai.exception.AiServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Service
@Profile("gemini")
public class GeminiAiService implements AiService {

    private static final Logger logger =
            LoggerFactory.getLogger(GeminiAiService.class);

    private final RestClient restClient;
    private final String model;

    public GeminiAiService(
            @Value("${gemini.base-url}") String baseUrl,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model
    ) {
        this.model = model;

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-goog-api-key", apiKey)
                .defaultHeader(
                        "Content-Type",
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }

    @Override
    public String generateResponse(String prompt) {

        if (prompt == null || prompt.isBlank()) {
            throw new AiServiceException("Prompt cannot be empty");
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    )
            );

            Map<?, ?> response = restClient.post()
                    .uri("/v1beta/models/" + model + ":generateContent")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                throw new AiServiceException(
                        "Empty response received from Gemini"
                );
            }

            Object candidatesObject = response.get("candidates");

            if (!(candidatesObject instanceof List<?> candidates)
                    || candidates.isEmpty()) {
                throw new AiServiceException(
                        "No candidates received from Gemini"
                );
            }

            Object firstCandidate = candidates.get(0);

            if (!(firstCandidate instanceof Map<?, ?> candidate)) {
                throw new AiServiceException(
                        "Invalid response received from Gemini"
                );
            }

            Object contentObject = candidate.get("content");

            if (!(contentObject instanceof Map<?, ?> content)) {
                throw new AiServiceException(
                        "No content received from Gemini"
                );
            }

            Object partsObject = content.get("parts");

            if (!(partsObject instanceof List<?> parts)
                    || parts.isEmpty()) {
                throw new AiServiceException(
                        "No response parts received from Gemini"
                );
            }

            Object firstPart = parts.get(0);

            if (!(firstPart instanceof Map<?, ?> part)) {
                throw new AiServiceException(
                        "Invalid response part received from Gemini"
                );
            }

            Object textObject = part.get("text");

            if (textObject instanceof String text && !text.isBlank()) {
                return text;
            }

            throw new AiServiceException(
                    "No text response received from Gemini"
            );

        } catch (AiServiceException exception) {
            throw exception;

        } catch (RestClientResponseException exception) {

            logger.error(
                    "Gemini API error. Status: {}, Response: {}",
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString()
            );

            throw new AiServiceException(
                    "Gemini API request failed with status "
                            + exception.getStatusCode()
            );

        } catch (Exception exception) {

            logger.error(
                    "Unexpected error while communicating with Gemini",
                    exception
            );

            throw new AiServiceException(
                    "Failed to communicate with Gemini"
            );
        }
    }
}

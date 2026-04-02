package com.synflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synflow.dto.AIGenerateRequest;
import com.synflow.dto.AIGenerateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final ObjectMapper objectMapper;

    @Value("${app.openai.api-key}")
    private String apiKey;

    @Value("${app.openai.model}")
    private String model;

    @Value("${app.openai.base-url}")
    private String baseUrl;

    private static final String SYSTEM_PROMPT = """
            You are a professional profile analyst. Given raw text input about a person or company \
            (from LinkedIn, website, or free text), extract and generate a structured profile in JSON format.

            Return ONLY valid JSON with this structure:
            {
              "name": "extracted name",
              "expertise": ["tag1", "tag2", "tag3"],
              "servicesOffered": "description of services",
              "industryFocus": "primary industry",
              "geographicReach": ["region1", "region2"],
              "trackRecord": "summary of experience and achievements",
              "summary": "2-3 sentence professional summary",
              "suggestedTags": ["tag1", "tag2", "tag3", "tag4", "tag5"]
            }

            Rules:
            - Extract real information, do not invent
            - Keep expertise tags short (1-3 words each)
            - Geographic reach should be country or region names
            - Summary should be concise and professional
            - If information is missing, use "Not specified"
            """;

    public AIGenerateResponse generateProfile(AIGenerateRequest request) {
        StringBuilder userMessage = new StringBuilder();

        if (request.linkedinText() != null && !request.linkedinText().isBlank()) {
            userMessage.append("LinkedIn Info:\n").append(request.linkedinText()).append("\n\n");
        }
        if (request.websiteText() != null && !request.websiteText().isBlank()) {
            userMessage.append("Website Info:\n").append(request.websiteText()).append("\n\n");
        }
        if (request.freeText() != null && !request.freeText().isBlank()) {
            userMessage.append("Additional Info:\n").append(request.freeText()).append("\n\n");
        }

        if (userMessage.isEmpty()) {
            throw new RuntimeException("At least one text input is required");
        }

        try {
            RestClient client = RestClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .defaultHeader("Content-Type", "application/json")
                    .build();

            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of("role", "user", "content", userMessage.toString())
                    ),
                    "temperature", 0.3,
                    "response_format", Map.of("type", "json_object")
            );

            String responseJson = client.post()
                    .uri("/v1/chat/completions")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            // Parse OpenAI response
            var responseNode = objectMapper.readTree(responseJson);
            String content = responseNode.at("/choices/0/message/content").asText();

            return objectMapper.readValue(content, AIGenerateResponse.class);

        } catch (Exception e) {
            log.error("OpenAI API call failed", e);
            // Return a fallback response with available info
            return new AIGenerateResponse(
                    "Not specified", List.of(), "Not specified", "Not specified",
                    List.of(), "Not specified",
                    "AI generation failed. Please fill in details manually.",
                    List.of()
            );
        }
    }
}

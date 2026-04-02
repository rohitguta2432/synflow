package com.synflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synflow.dto.AIGenerateRequest;
import com.synflow.dto.AIGenerateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AIServiceTest {

    private AIService aiService;

    @BeforeEach
    void setUp() {
        aiService = new AIService(new ObjectMapper());
        ReflectionTestUtils.setField(aiService, "apiKey", "sk-test");
        ReflectionTestUtils.setField(aiService, "model", "gpt-4o");
        ReflectionTestUtils.setField(aiService, "baseUrl", "https://api.openai.com");
    }

    @Test
    void generateProfile_emptyInputs_throwsException() {
        AIGenerateRequest request = new AIGenerateRequest("", "", "");

        assertThatThrownBy(() -> aiService.generateProfile(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("At least one text input is required");
    }

    @Test
    void generateProfile_allNullInputs_throwsException() {
        AIGenerateRequest request = new AIGenerateRequest(null, null, null);

        assertThatThrownBy(() -> aiService.generateProfile(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("At least one text input is required");
    }

    @Test
    void generateProfile_blankInputs_throwsException() {
        AIGenerateRequest request = new AIGenerateRequest("   ", "  ", "  ");

        assertThatThrownBy(() -> aiService.generateProfile(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("At least one text input is required");
    }

    @Test
    void generateProfile_withInput_apiError_returnsFallback() {
        // With invalid API key, the call will fail and return fallback
        AIGenerateRequest request = new AIGenerateRequest("John Doe, CEO of TechCorp", null, null);

        AIGenerateResponse response = aiService.generateProfile(request);

        // Should return fallback response since API call will fail with test key
        assertThat(response).isNotNull();
        assertThat(response.name()).isNotNull();
        assertThat(response.summary()).contains("AI generation failed");
    }

    @Test
    void generateProfile_withWebsiteText_apiError_returnsFallback() {
        AIGenerateRequest request = new AIGenerateRequest(null, "TechCorp provides cloud solutions", null);

        AIGenerateResponse response = aiService.generateProfile(request);

        assertThat(response).isNotNull();
        assertThat(response.expertise()).isNotNull();
        assertThat(response.geographicReach()).isNotNull();
    }

    @Test
    void generateProfile_withFreeText_apiError_returnsFallback() {
        AIGenerateRequest request = new AIGenerateRequest(null, null, "Jane is a blockchain consultant");

        AIGenerateResponse response = aiService.generateProfile(request);

        assertThat(response).isNotNull();
    }

    @Test
    void generateProfile_withAllInputs_apiError_returnsFallback() {
        AIGenerateRequest request = new AIGenerateRequest("LinkedIn text", "Website text", "Free text");

        AIGenerateResponse response = aiService.generateProfile(request);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Not specified");
    }
}

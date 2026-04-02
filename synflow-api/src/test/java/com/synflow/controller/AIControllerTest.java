package com.synflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synflow.dto.AIGenerateRequest;
import com.synflow.dto.AIGenerateResponse;
import com.synflow.security.JwtAuthFilter;
import com.synflow.security.JwtTokenProvider;
import com.synflow.service.AIService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AIController.class)
@AutoConfigureMockMvc(addFilters = false)
class AIControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AIService aiService;
    @MockBean private JwtTokenProvider tokenProvider;
    @MockBean private JwtAuthFilter jwtAuthFilter;

    @Test
    void generateProfile_returns200WithResponse() throws Exception {
        AIGenerateResponse response = new AIGenerateResponse(
                "John Doe", List.of("M&A", "FinTech"), "Advisory services", "FinTech",
                List.of("EMEA"), "20 years experience", "Expert advisor", List.of("PE", "VC"));
        when(aiService.generateProfile(any())).thenReturn(response);

        AIGenerateRequest req = new AIGenerateRequest("LinkedIn about text", null, null);

        mockMvc.perform(post("/api/ai/generate-profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.expertise[0]").value("M&A"))
                .andExpect(jsonPath("$.industryFocus").value("FinTech"))
                .andExpect(jsonPath("$.geographicReach[0]").value("EMEA"));
    }

    @Test
    void generateProfile_serviceThrows_returns500() throws Exception {
        when(aiService.generateProfile(any())).thenThrow(new RuntimeException("At least one text input is required"));

        AIGenerateRequest req = new AIGenerateRequest(null, null, null);

        mockMvc.perform(post("/api/ai/generate-profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is5xxServerError());
    }
}

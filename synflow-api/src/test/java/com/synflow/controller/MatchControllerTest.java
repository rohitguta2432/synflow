package com.synflow.controller;

import com.synflow.dto.MatchDto;
import com.synflow.dto.StatsDto;
import com.synflow.security.JwtAuthFilter;
import com.synflow.security.JwtTokenProvider;
import com.synflow.service.MatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchController.class)
@AutoConfigureMockMvc(addFilters = false)
class MatchControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private MatchService matchService;
    @MockBean private JwtTokenProvider tokenProvider;
    @MockBean private JwtAuthFilter jwtAuthFilter;

    @Test
    void list_returnsPage() throws Exception {
        MatchDto match = new MatchDto(UUID.randomUUID(), UUID.randomUUID(), "Deal", "FinTech",
                UUID.randomUUID(), "Profile", List.of("Tag"), BigDecimal.valueOf(85), "Reason", LocalDateTime.now());
        when(matchService.findAll(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(match)));

        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].dealTitle").value("Deal"))
                .andExpect(jsonPath("$.content[0].profileName").value("Profile"))
                .andExpect(jsonPath("$.content[0].relevanceScore").value(85));
    }

    @Test
    void list_withFilters_passes() throws Exception {
        when(matchService.findAll(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        UUID dealId = UUID.randomUUID();
        mockMvc.perform(get("/api/matches?dealId=" + dealId + "&minScore=50"))
                .andExpect(status().isOk());
    }

    @Test
    void stats_returnsCounts() throws Exception {
        when(matchService.getStats()).thenReturn(new StatsDto(100, 25, 500));

        mockMvc.perform(get("/api/matches/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProfiles").value(100))
                .andExpect(jsonPath("$.totalDeals").value(25))
                .andExpect(jsonPath("$.totalMatches").value(500));
    }
}

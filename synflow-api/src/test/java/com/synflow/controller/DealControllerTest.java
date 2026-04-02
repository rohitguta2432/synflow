package com.synflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synflow.dto.*;
import com.synflow.security.JwtAuthFilter;
import com.synflow.security.JwtTokenProvider;
import com.synflow.service.DealService;
import com.synflow.service.MatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DealController.class)
@AutoConfigureMockMvc(addFilters = false)
class DealControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private DealService dealService;
    @MockBean private MatchService matchService;
    @MockBean private JwtTokenProvider tokenProvider;
    @MockBean private JwtAuthFilter jwtAuthFilter;

    private DealDto buildDealDto(UUID id, String title) {
        return new DealDto(id, title, "FinTech", "INVESTMENT", "$50M", List.of("EMEA"),
                "reqs", "ACTIVE", "Creator", null, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void list_returnsPage() throws Exception {
        when(dealService.findAll(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(buildDealDto(UUID.randomUUID(), "Deal A"))));

        mockMvc.perform(get("/api/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Deal A"));
    }

    @Test
    void get_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(dealService.findById(id)).thenReturn(buildDealDto(id, "Deal B"));

        mockMvc.perform(get("/api/deals/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Deal B"));
    }

    @Test
    void create_validBody_returns200() throws Exception {
        when(dealService.create(any(), any())).thenReturn(buildDealDto(UUID.randomUUID(), "New Deal"));

        DealRequest req = new DealRequest("New Deal", "FinTech", "INVESTMENT", "$50M", List.of("EMEA"), "reqs", "ACTIVE");

        mockMvc.perform(post("/api/deals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Deal"));
    }

    @Test
    void update_validBody_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(dealService.update(eq(id), any())).thenReturn(buildDealDto(id, "Updated"));

        DealRequest req = new DealRequest("Updated", "Energy", "ADVISORY", "$100M", List.of(), null, "ACTIVE");

        mockMvc.perform(put("/api/deals/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/deals/" + id))
                .andExpect(status().isNoContent());
        verify(dealService).delete(id);
    }

    @Test
    void match_triggersMatchingAndReturns() throws Exception {
        UUID id = UUID.randomUUID();
        MatchDto matchDto = new MatchDto(UUID.randomUUID(), id, "Deal", "FinTech",
                UUID.randomUUID(), "Profile", List.of("M&A"), BigDecimal.valueOf(80), "Reason", LocalDateTime.now());
        when(matchService.matchDealToProfiles(id)).thenReturn(List.of(matchDto));

        mockMvc.perform(post("/api/deals/" + id + "/match"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].relevanceScore").value(80))
                .andExpect(jsonPath("$[0].profileName").value("Profile"));
    }
}

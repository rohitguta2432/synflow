package com.synflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synflow.dto.*;
import com.synflow.security.JwtAuthFilter;
import com.synflow.security.JwtTokenProvider;
import com.synflow.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProfileControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ProfileService profileService;
    @MockBean private JwtTokenProvider tokenProvider;
    @MockBean private JwtAuthFilter jwtAuthFilter;

    private ProfileDto buildProfileDto(UUID id, String name) {
        return new ProfileDto(id, "M_001", name, "REAL", List.of("M&A"), "services", "FinTech",
                List.of("EMEA"), "track", "ACTIVE", "summary", false, "Creator",
                null, null, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void list_returnsPage() throws Exception {
        var dto = buildProfileDto(UUID.randomUUID(), "Julian");
        when(profileService.findAll(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/profiles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Julian"));
    }

    @Test
    void list_withSearchParam_passes() throws Exception {
        when(profileService.findAll(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/profiles?search=test&industry=FinTech&type=REAL&status=ACTIVE"))
                .andExpect(status().isOk());

        verify(profileService).findAll(eq("test"), eq("FinTech"), eq("REAL"), eq("ACTIVE"), any());
    }

    @Test
    void get_existingProfile_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        var dto = buildProfileDto(id, "Alice");
        when(profileService.findById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/profiles/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void create_validBody_returns200() throws Exception {
        var dto = buildProfileDto(UUID.randomUUID(), "New Profile");
        when(profileService.create(any(), any())).thenReturn(dto);

        ProfileRequest req = new ProfileRequest("New Profile", "REAL", List.of("Tag"), "svc", "FinTech",
                List.of("EMEA"), "track", "ACTIVE", "summary", false);

        mockMvc.perform(post("/api/profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Profile"));
    }

    @Test
    void update_validBody_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        var dto = buildProfileDto(id, "Updated");
        when(profileService.update(eq(id), any())).thenReturn(dto);

        ProfileRequest req = new ProfileRequest("Updated", "SHADOW", List.of(), null, null, List.of(), null, null, null, false);

        mockMvc.perform(put("/api/profiles/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/profiles/" + id))
                .andExpect(status().isNoContent());
        verify(profileService).delete(id);
    }

    @Test
    void addConnection_returns200() throws Exception {
        UUID profileId = UUID.randomUUID();
        var connDto = new ProfileDto.ConnectionDto(UUID.randomUUID(), UUID.randomUUID(), "ConnName", "REAL", "FinTech", "advisor");
        when(profileService.addConnection(eq(profileId), any())).thenReturn(connDto);

        mockMvc.perform(post("/api/profiles/" + profileId + "/connections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ConnectionRequest(UUID.randomUUID(), "advisor"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connectedProfileName").value("ConnName"));
    }

    @Test
    void removeConnection_returns204() throws Exception {
        UUID profileId = UUID.randomUUID();
        UUID connId = UUID.randomUUID();
        mockMvc.perform(delete("/api/profiles/" + profileId + "/connections/" + connId))
                .andExpect(status().isNoContent());
        verify(profileService).removeConnection(profileId, connId);
    }

    @Test
    void getGraph_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        var graph = new GraphDto(List.of(new GraphDto.GraphNode(id, "A", "REAL", "X")), List.of());
        when(profileService.getGraph(id)).thenReturn(graph);

        mockMvc.perform(get("/api/profiles/" + id + "/graph"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodes[0].name").value("A"));
    }
}

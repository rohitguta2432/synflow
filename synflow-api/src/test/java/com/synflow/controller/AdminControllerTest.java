package com.synflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synflow.dto.CreateUserRequest;
import com.synflow.dto.UserDto;
import com.synflow.security.JwtAuthFilter;
import com.synflow.security.JwtTokenProvider;
import com.synflow.service.AuthService;
import com.synflow.service.EmbeddingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private EmbeddingService embeddingService;
    @MockBean private JwtTokenProvider tokenProvider;
    @MockBean private JwtAuthFilter jwtAuthFilter;

    @Test
    void listUsers_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        UserDto user = new UserDto(id, "a@b.com", "Test User", "ADMIN", LocalDateTime.now());
        when(authService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("a@b.com"))
                .andExpect(jsonPath("$[0].fullName").value("Test User"));
    }

    @Test
    void createUser_validBody_returns200() throws Exception {
        UserDto created = new UserDto(UUID.randomUUID(), "new@test.com", "New User", "INTERNAL_USER", LocalDateTime.now());
        when(authService.createUser(any())).thenReturn(created);

        CreateUserRequest req = new CreateUserRequest("new@test.com", "pass123", "New User", "INTERNAL_USER");

        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@test.com"));
    }

    @Test
    void createUser_missingEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"pass\",\"fullName\":\"Test\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/admin/users/" + id))
                .andExpect(status().isNoContent());
    }
}

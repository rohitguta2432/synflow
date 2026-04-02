package com.synflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synflow.dto.AuthRequest;
import com.synflow.dto.AuthResponse;
import com.synflow.dto.UserDto;
import com.synflow.security.JwtAuthFilter;
import com.synflow.security.JwtTokenProvider;
import com.synflow.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private JwtTokenProvider tokenProvider;
    @MockBean private JwtAuthFilter jwtAuthFilter;

    @Test
    void login_validCredentials_returnsToken() throws Exception {
        UUID userId = UUID.randomUUID();
        AuthResponse resp = new AuthResponse("jwt-token", userId, "admin@synflow.com", "Alex", "ADMIN");
        when(authService.login(any())).thenReturn(resp);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AuthRequest("admin@synflow.com", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("admin@synflow.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void login_invalidCredentials_returns500() throws Exception {
        when(authService.login(any())).thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AuthRequest("bad@email.com", "wrong"))))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void login_missingEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"pass\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void me_authenticated_returnsUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UserDto dto = new UserDto(userId, "admin@synflow.com", "Alex Sterling", "ADMIN", LocalDateTime.now());
        when(authService.getCurrentUser(any())).thenReturn(dto);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk());
    }
}

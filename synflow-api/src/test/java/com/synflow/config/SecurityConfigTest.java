package com.synflow.config;

import com.synflow.security.JwtAuthFilter;
import com.synflow.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
class SecurityConfigTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private JwtTokenProvider tokenProvider;
    @MockBean private JwtAuthFilter jwtAuthFilter;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }
}

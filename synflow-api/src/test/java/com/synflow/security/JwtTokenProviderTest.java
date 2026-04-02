package com.synflow.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;
    private static final String SECRET = "mySuperSecretKeyForJWTThatIsAtLeast256BitsLongEnough2024";

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(SECRET, 86400000L);
    }

    @Test
    void generateToken_returnsNonEmptyString() {
        UUID userId = UUID.randomUUID();
        String token = provider.generateToken(userId, "test@test.com", "ADMIN");
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void getUserIdFromToken_extractsCorrectId() {
        UUID userId = UUID.randomUUID();
        String token = provider.generateToken(userId, "test@test.com", "ADMIN");
        assertThat(provider.getUserIdFromToken(token)).isEqualTo(userId);
    }

    @Test
    void getEmailFromToken_extractsCorrectEmail() {
        UUID userId = UUID.randomUUID();
        String token = provider.generateToken(userId, "admin@synflow.com", "ADMIN");
        assertThat(provider.getEmailFromToken(token)).isEqualTo("admin@synflow.com");
    }

    @Test
    void getRoleFromToken_extractsCorrectRole() {
        UUID userId = UUID.randomUUID();
        String token = provider.generateToken(userId, "test@test.com", "INTERNAL_USER");
        assertThat(provider.getRoleFromToken(token)).isEqualTo("INTERNAL_USER");
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = provider.generateToken(UUID.randomUUID(), "a@b.com", "ADMIN");
        assertThat(provider.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        assertThat(provider.validateToken("invalid.jwt.token")).isFalse();
    }

    @Test
    void validateToken_emptyToken_returnsFalse() {
        assertThat(provider.validateToken("")).isFalse();
    }

    @Test
    void validateToken_nullToken_returnsFalse() {
        assertThat(provider.validateToken(null)).isFalse();
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        // Create provider with 0ms expiration
        JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET, 0L);
        String token = expiredProvider.generateToken(UUID.randomUUID(), "a@b.com", "ADMIN");
        // Token should already be expired
        assertThat(expiredProvider.validateToken(token)).isFalse();
    }

    @Test
    void validateToken_tamperedToken_returnsFalse() {
        String token = provider.generateToken(UUID.randomUUID(), "a@b.com", "ADMIN");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(provider.validateToken(tampered)).isFalse();
    }

    @Test
    void generateToken_differentUsers_produceDifferentTokens() {
        String t1 = provider.generateToken(UUID.randomUUID(), "a@b.com", "ADMIN");
        String t2 = provider.generateToken(UUID.randomUUID(), "c@d.com", "INTERNAL_USER");
        assertThat(t1).isNotEqualTo(t2);
    }
}

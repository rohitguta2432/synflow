package com.synflow.service;

import com.synflow.dto.AuthRequest;
import com.synflow.dto.AuthResponse;
import com.synflow.dto.CreateUserRequest;
import com.synflow.dto.UserDto;
import com.synflow.entity.User;
import com.synflow.repository.UserRepository;
import com.synflow.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider tokenProvider;

    @InjectMocks private AuthService authService;

    private User buildUser(UUID id, String email, String name, User.Role role) {
        return User.builder()
                .id(id).email(email).fullName(name).role(role)
                .passwordHash("hashed").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void login_validCredentials_returnsToken() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "admin@synflow.com", "Alex Sterling", User.Role.ADMIN);
        when(userRepository.findByEmail("admin@synflow.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("admin123", "hashed")).thenReturn(true);
        when(tokenProvider.generateToken(userId, "admin@synflow.com", "ADMIN")).thenReturn("jwt-token");

        AuthResponse response = authService.login(new AuthRequest("admin@synflow.com", "admin123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("admin@synflow.com");
        assertThat(response.fullName()).isEqualTo("Alex Sterling");
        assertThat(response.role()).isEqualTo("ADMIN");
    }

    @Test
    void login_invalidEmail_throwsException() {
        when(userRepository.findByEmail("wrong@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new AuthRequest("wrong@email.com", "pass")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void login_wrongPassword_throwsException() {
        User user = buildUser(UUID.randomUUID(), "a@b.com", "Name", User.Role.ADMIN);
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new AuthRequest("a@b.com", "wrong")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void getCurrentUser_existingUser_returnsDto() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, "a@b.com", "Test User", User.Role.INTERNAL_USER);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserDto dto = authService.getCurrentUser(id);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.email()).isEqualTo("a@b.com");
        assertThat(dto.fullName()).isEqualTo("Test User");
        assertThat(dto.role()).isEqualTo("INTERNAL_USER");
    }

    @Test
    void getCurrentUser_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void createUser_newEmail_createsAndReturns() {
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        User saved = buildUser(UUID.randomUUID(), "new@test.com", "New User", User.Role.INTERNAL_USER);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserDto dto = authService.createUser(new CreateUserRequest("new@test.com", "pass123", "New User", "INTERNAL_USER"));

        assertThat(dto.email()).isEqualTo("new@test.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_existingEmail_throwsException() {
        when(userRepository.existsByEmail("exists@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.createUser(
                new CreateUserRequest("exists@test.com", "pass", "Name", "ADMIN")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void getAllUsers_returnsList() {
        User u1 = buildUser(UUID.randomUUID(), "a@b.com", "User A", User.Role.ADMIN);
        User u2 = buildUser(UUID.randomUUID(), "c@d.com", "User B", User.Role.INTERNAL_USER);
        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<UserDto> users = authService.getAllUsers();

        assertThat(users).hasSize(2);
        assertThat(users.get(0).email()).isEqualTo("a@b.com");
        assertThat(users.get(1).email()).isEqualTo("c@d.com");
    }

    @Test
    void deleteUser_differentUser_deletes() {
        UUID targetId = UUID.randomUUID();
        UUID currentId = UUID.randomUUID();

        authService.deleteUser(targetId, currentId);

        verify(userRepository).deleteById(targetId);
    }

    @Test
    void deleteUser_selfDelete_throwsException() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> authService.deleteUser(id, id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cannot delete yourself");
        verify(userRepository, never()).deleteById(any());
    }
}

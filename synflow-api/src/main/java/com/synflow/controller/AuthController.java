package com.synflow.controller;

import com.synflow.dto.AuthRequest;
import com.synflow.dto.AuthResponse;
import com.synflow.dto.UserDto;
import com.synflow.security.JwtAuthFilter;
import com.synflow.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal JwtAuthFilter.AuthUser user) {
        return ResponseEntity.ok(authService.getCurrentUser(user.id()));
    }
}

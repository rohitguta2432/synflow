package com.synflow.controller;

import com.synflow.dto.CreateUserRequest;
import com.synflow.dto.UserDto;
import com.synflow.security.JwtAuthFilter;
import com.synflow.service.AuthService;
import com.synflow.service.EmbeddingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthService authService;
    private final EmbeddingService embeddingService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> listUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(authService.createUser(request));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtAuthFilter.AuthUser user) {
        authService.deleteUser(id, user.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reindex-embeddings")
    public ResponseEntity<Map<String, Integer>> reindexEmbeddings() {
        int count = embeddingService.reindexAll();
        return ResponseEntity.ok(Map.of("updated", count));
    }
}

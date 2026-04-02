package com.synflow.controller;

import com.synflow.dto.*;
import com.synflow.security.JwtAuthFilter;
import com.synflow.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<Page<ProfileDto>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(profileService.findAll(search, industry, type, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileDto> get(@PathVariable UUID id) {
        return ResponseEntity.ok(profileService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ProfileDto> create(
            @Valid @RequestBody ProfileRequest request,
            @AuthenticationPrincipal JwtAuthFilter.AuthUser user) {
        return ResponseEntity.ok(profileService.create(request, user.id()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfileDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(profileService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        profileService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/connections")
    public ResponseEntity<ProfileDto.ConnectionDto> addConnection(
            @PathVariable UUID id,
            @Valid @RequestBody ConnectionRequest request) {
        return ResponseEntity.ok(profileService.addConnection(id, request));
    }

    @DeleteMapping("/{id}/connections/{connectionId}")
    public ResponseEntity<Void> removeConnection(
            @PathVariable UUID id,
            @PathVariable UUID connectionId) {
        profileService.removeConnection(id, connectionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/graph")
    public ResponseEntity<GraphDto> getGraph(@PathVariable UUID id) {
        return ResponseEntity.ok(profileService.getGraph(id));
    }
}

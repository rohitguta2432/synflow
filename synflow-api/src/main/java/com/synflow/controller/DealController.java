package com.synflow.controller;

import com.synflow.dto.DealDto;
import com.synflow.dto.DealRequest;
import com.synflow.dto.MatchDto;
import com.synflow.security.JwtAuthFilter;
import com.synflow.service.DealService;
import com.synflow.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;
    private final MatchService matchService;

    @GetMapping
    public ResponseEntity<Page<DealDto>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String dealType,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(dealService.findAll(search, industry, dealType, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DealDto> get(@PathVariable UUID id) {
        return ResponseEntity.ok(dealService.findById(id));
    }

    @PostMapping
    public ResponseEntity<DealDto> create(
            @Valid @RequestBody DealRequest request,
            @AuthenticationPrincipal JwtAuthFilter.AuthUser user) {
        return ResponseEntity.ok(dealService.create(request, user.id()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DealDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody DealRequest request) {
        return ResponseEntity.ok(dealService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        dealService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/match")
    public ResponseEntity<List<MatchDto>> match(@PathVariable UUID id) {
        return ResponseEntity.ok(matchService.matchDealToProfiles(id));
    }
}

package com.synflow.controller;

import com.synflow.dto.MatchDto;
import com.synflow.dto.StatsDto;
import com.synflow.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    public ResponseEntity<Page<MatchDto>> list(
            @RequestParam(required = false) UUID dealId,
            @RequestParam(required = false) UUID profileId,
            @RequestParam(required = false) BigDecimal minScore,
            @PageableDefault(size = 20, sort = "matchedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(matchService.findAll(dealId, profileId, minScore, pageable));
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsDto> stats() {
        return ResponseEntity.ok(matchService.getStats());
    }
}

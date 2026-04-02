package com.synflow.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DealDto(
        UUID id,
        String title,
        String industry,
        String dealType,
        String ticketSize,
        List<String> geography,
        String requirements,
        String status,
        String createdByName,
        List<MatchDto> matches,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

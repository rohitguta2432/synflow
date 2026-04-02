package com.synflow.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProfileDto(
        UUID id,
        String uniqueCode,
        String name,
        String type,
        List<String> expertise,
        String servicesOffered,
        String industryFocus,
        List<String> geographicReach,
        String trackRecord,
        String contactStatus,
        String summary,
        Boolean aiGenerated,
        String createdByName,
        List<ConnectionDto> connections,
        List<MatchDto> matches,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record ConnectionDto(
            UUID id,
            UUID connectedProfileId,
            String connectedProfileName,
            String connectedProfileType,
            String connectedProfileIndustry,
            String connectionType
    ) {}
}

package com.synflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProfileRequest(
        @NotBlank String name,
        @NotNull String type,
        List<String> expertise,
        String servicesOffered,
        String industryFocus,
        List<String> geographicReach,
        String trackRecord,
        String contactStatus,
        String summary,
        Boolean aiGenerated
) {}

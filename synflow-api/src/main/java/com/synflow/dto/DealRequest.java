package com.synflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DealRequest(
        @NotBlank String title,
        @NotBlank String industry,
        @NotNull String dealType,
        String ticketSize,
        List<String> geography,
        String requirements,
        String status
) {}

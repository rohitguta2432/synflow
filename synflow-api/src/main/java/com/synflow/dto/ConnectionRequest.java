package com.synflow.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConnectionRequest(
        @NotNull UUID connectedProfileId,
        String connectionType
) {}

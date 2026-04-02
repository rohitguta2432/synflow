package com.synflow.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDto(
        UUID id,
        String email,
        String fullName,
        String role,
        LocalDateTime createdAt
) {}

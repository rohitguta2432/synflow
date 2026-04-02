package com.synflow.dto;

public record AIGenerateRequest(
        String linkedinText,
        String websiteText,
        String freeText
) {}

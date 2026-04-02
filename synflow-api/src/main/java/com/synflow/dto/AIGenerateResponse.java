package com.synflow.dto;

import java.util.List;

public record AIGenerateResponse(
        String name,
        List<String> expertise,
        String servicesOffered,
        String industryFocus,
        List<String> geographicReach,
        String trackRecord,
        String summary,
        List<String> suggestedTags
) {}

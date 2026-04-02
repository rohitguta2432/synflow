package com.synflow.controller;

import com.synflow.dto.AIGenerateRequest;
import com.synflow.dto.AIGenerateResponse;
import com.synflow.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping("/generate-profile")
    public ResponseEntity<AIGenerateResponse> generateProfile(@RequestBody AIGenerateRequest request) {
        return ResponseEntity.ok(aiService.generateProfile(request));
    }
}

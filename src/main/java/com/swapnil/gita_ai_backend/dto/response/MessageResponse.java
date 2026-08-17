package com.swapnil.gita_ai_backend.dto.response;

import com.swapnil.gita_ai_backend.entity.enums.MessageRole;

import java.time.Instant;

public record MessageResponse(
        String id,
        MessageRole role,
        String content,
        Instant createdAt
) {
}
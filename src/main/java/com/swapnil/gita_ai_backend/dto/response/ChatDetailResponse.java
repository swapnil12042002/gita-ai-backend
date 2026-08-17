package com.swapnil.gita_ai_backend.dto.response;

import java.time.Instant;
import java.util.List;

public record ChatDetailResponse(
        String id,
        String title,
        Instant createdAt,
        Instant updatedAt,
        List<MessageResponse> messages
) {
}
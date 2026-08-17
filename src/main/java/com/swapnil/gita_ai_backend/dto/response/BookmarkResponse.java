package com.swapnil.gita_ai_backend.dto.response;

import java.time.Instant;

public record BookmarkResponse(
        String verseId,
        Integer chapter,
        Integer verse,
        String sanskrit,
        String translation,
        String summary,
        Instant createdAt
) {
}
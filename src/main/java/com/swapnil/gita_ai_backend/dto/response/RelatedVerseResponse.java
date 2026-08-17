package com.swapnil.gita_ai_backend.dto.response;

public record RelatedVerseResponse(
        String id,
        Integer chapter,
        Integer verse,
        String translation,
        String summary,
        String[] topics
) {
}
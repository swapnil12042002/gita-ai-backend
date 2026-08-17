package com.swapnil.gita_ai_backend.dto.response;

public record VerseResponse(
        String id,
        Integer chapter,
        Integer verse,
        String sanskrit,
        String transliteration,
        String translation,
        String commentary,
        String summary,
        String[] keywords,
        String[] topics,
        String[] lifeSituations,
        String[] emotions
) {
}
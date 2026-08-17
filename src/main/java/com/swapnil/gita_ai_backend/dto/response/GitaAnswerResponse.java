package com.swapnil.gita_ai_backend.dto.response;

import java.util.List;

public record GitaAnswerResponse(
        String chatId,
        String question,
        String answer,
        List<GitaSource> sources
) {
}
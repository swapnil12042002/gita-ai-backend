package com.swapnil.gita_ai_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GitaQuestionRequest(

        @NotBlank(message = "Question cannot be empty")
        String question,

        String chatId
) {
}
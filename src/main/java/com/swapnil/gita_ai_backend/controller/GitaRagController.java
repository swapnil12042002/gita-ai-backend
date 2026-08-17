package com.swapnil.gita_ai_backend.controller;

import com.swapnil.gita_ai_backend.dto.request.GitaQuestionRequest;
import com.swapnil.gita_ai_backend.dto.response.GitaAnswerResponse;
import com.swapnil.gita_ai_backend.service.GitaRagService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gita")
public class GitaRagController {

    private final GitaRagService gitaRagService;

    public GitaRagController(GitaRagService gitaRagService) {
        this.gitaRagService = gitaRagService;
    }

    @PostMapping("/ask")
    public GitaAnswerResponse ask(
            @Valid @RequestBody GitaQuestionRequest request,
            Authentication authentication) {

        return gitaRagService.answer(
                request.question(),
                request.chatId(),
                authentication
        );
    }
}
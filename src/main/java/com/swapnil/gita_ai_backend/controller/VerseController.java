package com.swapnil.gita_ai_backend.controller;

import com.swapnil.gita_ai_backend.dto.response.RelatedVerseResponse;
import com.swapnil.gita_ai_backend.dto.response.VerseResponse;
import com.swapnil.gita_ai_backend.entity.GitaVerse;
import com.swapnil.gita_ai_backend.repository.GitaVerseRepository;
import com.swapnil.gita_ai_backend.service.VerseSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/verses")
@RequiredArgsConstructor
public class VerseController {

    private final GitaVerseRepository gitaVerseRepository;
    private final VerseSearchService verseSearchService;

    @GetMapping("/{verseId}")
    public VerseResponse getVerse(
            @PathVariable String verseId,
            Authentication authentication) {

        GitaVerse verse = gitaVerseRepository.findById(verseId)
                .orElseThrow(() ->
                        new RuntimeException("Verse not found: " + verseId));

        return new VerseResponse(
                verse.getId(),
                verse.getChapter(),
                verse.getVerse(),
                verse.getSanskrit(),
                verse.getTransliteration(),
                verse.getTranslation(),
                verse.getCommentary(),
                verse.getSummary(),
                verse.getKeywords(),
                verse.getTopics(),
                verse.getLifeSituations(),
                verse.getEmotions()
        );
    }

    @GetMapping("/{verseId}/related")
    public List<RelatedVerseResponse> getRelatedVerses(
            @PathVariable String verseId,
            Authentication authentication) {

        return verseSearchService.findRelatedVerses(verseId, 5)
                .stream()
                .map(verse -> new RelatedVerseResponse(
                        verse.getId(),
                        verse.getChapter(),
                        verse.getVerse(),
                        verse.getTranslation(),
                        verse.getSummary(),
                        verse.getTopics()
                ))
                .toList();
    }
}
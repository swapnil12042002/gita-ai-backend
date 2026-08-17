package com.swapnil.gita_ai_backend.service;

import com.swapnil.gita_ai_backend.entity.GitaVerse;
import com.swapnil.gita_ai_backend.repository.GitaVerseRepository;
import com.swapnil.gita_ai_backend.repository.RelatedVerseProjection;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VerseSearchService {

    private final GeminiEmbeddingService embeddingService;
    private final GitaVerseRepository gitaVerseRepository;

    public VerseSearchService(
            GeminiEmbeddingService embeddingService,
            GitaVerseRepository gitaVerseRepository) {
        this.embeddingService = embeddingService;
        this.gitaVerseRepository = gitaVerseRepository;
    }

    public List<GitaVerse> search(String question, int limit) {

        float[] embedding = embeddingService.generateEmbedding(question);

        String vector = toPgVector(embedding);

        return gitaVerseRepository.findSimilarVerses(
                vector,
                limit
        );
    }

    public List<RelatedVerseProjection> findRelatedVerses(
            String verseId,
            int limit) {

        if (!gitaVerseRepository.existsById(verseId)) {
            throw new RuntimeException("Verse not found: " + verseId);
        }

        String vector = gitaVerseRepository.findEmbeddingById(verseId);

        return gitaVerseRepository.findSimilarVerseProjections(
                        vector,
                        limit + 1
                ).stream()
                .filter(verse -> !verse.getId().equals(verseId))
                .limit(limit)
                .toList();
    }

    private String toPgVector(float[] embedding) {

        StringBuilder vector = new StringBuilder("[");

        for (int i = 0; i < embedding.length; i++) {

            if (i > 0) {
                vector.append(",");
            }

            vector.append(embedding[i]);
        }

        vector.append("]");

        return vector.toString();
    }
}
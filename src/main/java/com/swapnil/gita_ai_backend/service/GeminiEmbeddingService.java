package com.swapnil.gita_ai_backend.service;

import com.google.genai.Client;
import com.google.genai.types.ContentEmbedding;
import com.google.genai.types.EmbedContentConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeminiEmbeddingService {

    private final Client client;

    public GeminiEmbeddingService(
            @Value("${gemini.api-key}") String apiKey) {

        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    public float[] generateEmbedding(String text) {

        var response = client.models.embedContent(
                "gemini-embedding-2",
                List.of(text),
                EmbedContentConfig.builder()
                        .outputDimensionality(3072)
                        .build()
        );

        ContentEmbedding embedding = response.embeddings()
                .orElseThrow(() ->
                        new IllegalStateException("No embedding returned by Gemini"))
                .get(0);

        List<Float> values = embedding.values()
                .orElseThrow(() ->
                        new IllegalStateException("Embedding contains no values"));

        float[] result = new float[values.size()];

        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }

        return result;
    }
}
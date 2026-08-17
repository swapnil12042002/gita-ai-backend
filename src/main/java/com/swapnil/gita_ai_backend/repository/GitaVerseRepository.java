package com.swapnil.gita_ai_backend.repository;

import com.swapnil.gita_ai_backend.entity.GitaVerse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GitaVerseRepository extends JpaRepository<GitaVerse, String> {

    @Query(value = """
        SELECT
            id,
            chapter,
            verse,
            sanskrit,
            transliteration,
            translation,
            commentary,
            summary,
            keywords,
            topics,
            life_situations,
            emotions,
            questions,
            embedding
        FROM gita_verses
        ORDER BY
            (embedding::halfvec(3072))
            <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<GitaVerse> findSimilarVerses(
            @Param("embedding") String embedding,
            @Param("limit") int limit
    );

    @Query(value = """
        SELECT
            id,
            chapter,
            verse,
            translation,
            summary,
            topics
        FROM gita_verses
        ORDER BY
            (embedding::halfvec(3072))
            <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<RelatedVerseProjection> findSimilarVerseProjections(
            @Param("embedding") String embedding,
            @Param("limit") int limit
    );

    @Query(value = """
        SELECT embedding::text
        FROM gita_verses
        WHERE id = :verseId
        """, nativeQuery = true)
    String findEmbeddingById(@Param("verseId") String verseId);
}
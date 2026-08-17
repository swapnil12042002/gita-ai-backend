package com.swapnil.gita_ai_backend.repository;

import com.swapnil.gita_ai_backend.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {

    @Query("""
            SELECT b
            FROM Bookmark b
            WHERE b.user.id = :userId
              AND b.verse.id = :verseId
            """)
    Optional<Bookmark> findByUserAndVerse(
            @Param("userId") UUID userId,
            @Param("verseId") String verseId
    );

    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
            FROM Bookmark b
            WHERE b.user.id = :userId
              AND b.verse.id = :verseId
            """)
    boolean existsByUserAndVerse(
            @Param("userId") UUID userId,
            @Param("verseId") String verseId
    );

    @Modifying
    @Transactional
    @Query("""
            DELETE FROM Bookmark b
            WHERE b.user.id = :userId
              AND b.verse.id = :verseId
            """)
    void deleteByUserAndVerse(
            @Param("userId") UUID userId,
            @Param("verseId") String verseId
    );

    @Query(value = """
            SELECT
                b.created_at AS "createdAt",
                v.id AS "verseId",
                v.chapter AS "chapter",
                v.verse AS "verse",
                v.sanskrit AS "sanskrit",
                v.translation AS "translation",
                v.summary AS "summary"
            FROM bookmarks b
            JOIN gita_verses v ON v.id = b.verse_id
            WHERE b.user_id = :userId
            ORDER BY b.created_at DESC
            """, nativeQuery = true)
    List<BookmarkProjection> findBookmarkProjections(
            @Param("userId") UUID userId
    );

    @Query(value = """
            SELECT
                b.created_at AS "createdAt",
                v.id AS "verseId",
                v.chapter AS "chapter",
                v.verse AS "verse",
                v.sanskrit AS "sanskrit",
                v.translation AS "translation",
                v.summary AS "summary"
            FROM bookmarks b
            JOIN gita_verses v ON v.id = b.verse_id
            WHERE b.user_id = :userId
              AND b.verse_id = :verseId
            """, nativeQuery = true)
    BookmarkProjection findBookmarkProjection(
            @Param("userId") UUID userId,
            @Param("verseId") String verseId
    );

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM Bookmark b
        WHERE b.user.id = :userId
        """)
    void deleteByUserId(@Param("userId") UUID userId);
}
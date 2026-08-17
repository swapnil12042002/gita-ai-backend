package com.swapnil.gita_ai_backend.repository;

import java.time.Instant;

public interface BookmarkProjection {

    String getVerseId();

    Integer getChapter();

    Integer getVerse();

    String getSanskrit();

    String getTranslation();

    String getSummary();

    Instant getCreatedAt();
}
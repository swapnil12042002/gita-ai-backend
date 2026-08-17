package com.swapnil.gita_ai_backend.repository;

public interface RelatedVerseProjection {

    String getId();

    Integer getChapter();

    Integer getVerse();

    String getTranslation();

    String getSummary();

    String[] getTopics();
}
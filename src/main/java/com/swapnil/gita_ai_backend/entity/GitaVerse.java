package com.swapnil.gita_ai_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "gita_verses")
@Getter
@Setter
@NoArgsConstructor
public class GitaVerse {

    @Id
    @Column(nullable = false)
    private String id;

    @Column(nullable = false)
    private Integer chapter;

    @Column(nullable = false)
    private Integer verse;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String sanskrit;

    @Column(columnDefinition = "TEXT")
    private String transliteration;

    @Column(columnDefinition = "TEXT")
    private String translation;

    @Column(columnDefinition = "TEXT")
    private String commentary;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT[]")
    private String[] keywords;

    @Column(columnDefinition = "TEXT[]")
    private String[] topics;

    @Column(name = "life_situations", columnDefinition = "TEXT[]")
    private String[] lifeSituations;

    @Column(columnDefinition = "TEXT[]")
    private String[] emotions;

    @Column(columnDefinition = "TEXT[]")
    private String[] questions;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(columnDefinition = "vector(3072)")
    private float[] embedding;
}
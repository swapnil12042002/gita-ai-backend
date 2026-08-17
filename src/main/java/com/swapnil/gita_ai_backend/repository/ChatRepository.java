package com.swapnil.gita_ai_backend.repository;

import com.swapnil.gita_ai_backend.entity.Chat;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, String> {

    List<Chat> findByUserIdOrderByUpdatedAtDesc(UUID userId);

    Optional<Chat> findByIdAndUserId(String chatId, UUID userId);

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM Chat c
        WHERE c.user.id = :userId
        """)
    void deleteByUserId(@Param("userId") UUID userId);
}
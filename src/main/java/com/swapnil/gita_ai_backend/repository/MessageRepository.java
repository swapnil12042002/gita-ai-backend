package com.swapnil.gita_ai_backend.repository;

import com.swapnil.gita_ai_backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, String> {

    List<Message> findByChatIdOrderByCreatedAtAsc(String chatId);

    @Modifying
    @Transactional
    @Query("""
            DELETE FROM Message m
            WHERE m.chat.id IN (
                SELECT c.id FROM Chat c WHERE c.user.id = :userId
            )
            """)
    void deleteByUserId(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("""
            DELETE FROM Message m
            WHERE m.chat.id = :chatId
            """)
    void deleteByChatId(@Param("chatId") String chatId);
}
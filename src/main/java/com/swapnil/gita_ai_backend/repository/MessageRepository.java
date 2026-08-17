package com.swapnil.gita_ai_backend.repository;

import com.swapnil.gita_ai_backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, String> {

    List<Message> findByChatIdOrderByCreatedAtAsc(String chatId);
}
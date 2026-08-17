package com.swapnil.gita_ai_backend.controller;

import com.swapnil.gita_ai_backend.dto.response.ChatDetailResponse;
import com.swapnil.gita_ai_backend.dto.response.ChatResponse;
import com.swapnil.gita_ai_backend.dto.response.MessageResponse;
import com.swapnil.gita_ai_backend.entity.Chat;
import com.swapnil.gita_ai_backend.entity.User;
import com.swapnil.gita_ai_backend.repository.ChatRepository;
import com.swapnil.gita_ai_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<ChatResponse> getChats(Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));

        return chatRepository.findByUserIdOrderByUpdatedAtDesc(user.getId())
                .stream()
                .map(chat -> new ChatResponse(
                        chat.getId(),
                        chat.getTitle(),
                        chat.getCreatedAt(),
                        chat.getUpdatedAt()
                ))
                .toList();
    }

    @GetMapping("/{chatId}")
    public ChatDetailResponse getChat(
            @PathVariable String chatId,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));

        Chat chat = chatRepository.findByIdAndUserId(chatId, user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Chat not found"));

        List<MessageResponse> messages = chat.getMessages()
                .stream()
                .map(message -> new MessageResponse(
                        message.getId(),
                        message.getRole(),
                        message.getContent(),
                        message.getCreatedAt()
                ))
                .toList();

        return new ChatDetailResponse(
                chat.getId(),
                chat.getTitle(),
                chat.getCreatedAt(),
                chat.getUpdatedAt(),
                messages
        );
    }

    @DeleteMapping("/{chatId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChat(
            @PathVariable String chatId,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));

        Chat chat = chatRepository.findByIdAndUserId(
                chatId,
                user.getId()
        ).orElseThrow(() ->
                new RuntimeException("Chat not found"));

        chatRepository.delete(chat);
    }
}
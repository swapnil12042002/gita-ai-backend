package com.swapnil.gita_ai_backend.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.swapnil.gita_ai_backend.dto.response.GitaAnswerResponse;
import com.swapnil.gita_ai_backend.dto.response.GitaSource;
import com.swapnil.gita_ai_backend.entity.Chat;
import com.swapnil.gita_ai_backend.entity.GitaVerse;
import com.swapnil.gita_ai_backend.entity.Message;
import com.swapnil.gita_ai_backend.entity.enums.MessageRole;
import com.swapnil.gita_ai_backend.entity.User;
import com.swapnil.gita_ai_backend.repository.ChatRepository;
import com.swapnil.gita_ai_backend.repository.MessageRepository;
import com.swapnil.gita_ai_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GitaRagService {

    private final Client client;
    private final VerseSearchService verseSearchService;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public GitaRagService(
            VerseSearchService verseSearchService,
            ChatRepository chatRepository,
            MessageRepository messageRepository,
            UserRepository userRepository,
            @Value("${gemini.api-key}") String apiKey) {

        this.verseSearchService = verseSearchService;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;

        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    @Transactional
    public GitaAnswerResponse answer(
            String question,
            String chatId,
            Authentication authentication) {

        // 1. Get currently logged-in user
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));

        // 2. Create a new chat OR continue existing chat
        Chat chat;

        if (chatId == null || chatId.isBlank()) {

            chat = new Chat();
            chat.setTitle(createTitle(question));
            chat.setUser(user);

            chat = chatRepository.save(chat);

        } else {

            chat = chatRepository.findByIdAndUserId(chatId, user.getId())
                    .orElseThrow(() ->
                            new RuntimeException("Chat not found"));
        }

        // 3. Save user's question
        Message userMessage = new Message();
        userMessage.setChat(chat);
        userMessage.setRole(MessageRole.USER);
        userMessage.setContent(question);

        messageRepository.save(userMessage);

        // 4. Find relevant Gita verses
        List<GitaVerse> verses =
                verseSearchService.search(question, 5);

        // 5. Build context
        StringBuilder context = new StringBuilder();

        for (GitaVerse verse : verses) {

            context.append("\n--- ")
                    .append(verse.getId())
                    .append(" ---\n");

            context.append("Chapter: ")
                    .append(verse.getChapter())
                    .append("\n");

            context.append("Verse: ")
                    .append(verse.getVerse())
                    .append("\n");

            context.append("Sanskrit:\n")
                    .append(verse.getSanskrit())
                    .append("\n");

            context.append("Translation:\n")
                    .append(verse.getTranslation())
                    .append("\n");

            context.append("Summary:\n")
                    .append(verse.getSummary())
                    .append("\n");

            if (verse.getTopics() != null) {
                context.append("Topics:\n")
                        .append(String.join(", ", verse.getTopics()))
                        .append("\n");
            }
        }

        // 6. Prompt Gemini
        String prompt = """
            You are a Bhagavad Gita assistant.

            Answer the user's question using the Bhagavad Gita
            verses provided in the context below.

            IMPORTANT RULES:

            - Base the answer primarily on the provided verses.
            - Do not invent Gita teachings or quotations.
            - Do not claim that Krishna said something unless it
              is supported by the provided verses.
            - Explain the teaching in simple, practical language.
            - If relevant, mention the chapter and verse number.
            - Do not unnecessarily repeat the entire verse.
            - If the provided verses do not contain enough information
              to answer the question, honestly say that the retrieved
              verses do not provide enough context.

            User question:
            %s

            Retrieved Bhagavad Gita context:
            %s
            """.formatted(question, context);

        // 7. Generate answer
        var response = client.models.generateContent(
                "gemini-3-flash-preview",
                prompt,
                GenerateContentConfig.builder()
                        .temperature(0.3f)
                        .build()
        );

        String answer = response.text();

        // 8. Save assistant answer
        Message assistantMessage = new Message();
        assistantMessage.setChat(chat);
        assistantMessage.setRole(MessageRole.ASSISTANT);
        assistantMessage.setContent(answer);

        messageRepository.save(assistantMessage);

        // 9. Build sources
        List<GitaSource> sources = verses.stream()
                .map(verse -> new GitaSource(
                        verse.getId(),
                        verse.getChapter(),
                        verse.getVerse()
                ))
                .toList();

        // 10. Return chat ID + answer
        return new GitaAnswerResponse(
                chat.getId(),
                question,
                answer,
                sources
        );
    }

    private String createTitle(String question) {

        if (question == null || question.isBlank()) {
            return "New Conversation";
        }

        String cleanedQuestion = question.trim();

        if (cleanedQuestion.length() <= 50) {
            return cleanedQuestion;
        }

        return cleanedQuestion.substring(0, 47) + "...";
    }
}
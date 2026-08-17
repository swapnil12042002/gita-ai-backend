package com.swapnil.gita_ai_backend.controller.user;

import com.swapnil.gita_ai_backend.entity.User;
import com.swapnil.gita_ai_backend.repository.BookmarkRepository;
import com.swapnil.gita_ai_backend.repository.ChatRepository;
import com.swapnil.gita_ai_backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ChatRepository chatRepository;

    @GetMapping("/me")
    public String me(Authentication authentication) {
        return "Hello " + authentication.getName();
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void deleteAccount(Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));

        UUID userId = user.getId();

        bookmarkRepository.deleteByUserId(userId);

        chatRepository.deleteByUserId(userId);

        userRepository.deleteById(userId);
    }
}
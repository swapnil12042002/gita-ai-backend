package com.swapnil.gita_ai_backend.controller;

import com.swapnil.gita_ai_backend.dto.response.BookmarkResponse;
import com.swapnil.gita_ai_backend.entity.Bookmark;
import com.swapnil.gita_ai_backend.entity.GitaVerse;
import com.swapnil.gita_ai_backend.entity.User;
import com.swapnil.gita_ai_backend.repository.BookmarkProjection;
import com.swapnil.gita_ai_backend.repository.BookmarkRepository;
import com.swapnil.gita_ai_backend.repository.GitaVerseRepository;
import com.swapnil.gita_ai_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkRepository bookmarkRepository;
    private final GitaVerseRepository gitaVerseRepository;
    private final UserRepository userRepository;

    @PostMapping("/{verseId}")
    @ResponseStatus(HttpStatus.CREATED)
    public BookmarkResponse addBookmark(
            @PathVariable String verseId,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        if (bookmarkRepository.existsByUserAndVerse(
                user.getId(), verseId)) {

            throw new RuntimeException("Verse is already bookmarked");
        }

        if (!gitaVerseRepository.existsById(verseId)) {
            throw new RuntimeException("Verse not found: " + verseId);
        }

        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .verse(gitaVerseRepository.getReferenceById(verseId))
                .build();

        bookmark = bookmarkRepository.save(bookmark);

        BookmarkProjection projection =
                bookmarkRepository.findBookmarkProjection(
                        user.getId(),
                        verseId
                );

        return toResponse(projection);
    }

    @GetMapping
    public List<BookmarkResponse> getBookmarks(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        return bookmarkRepository
                .findBookmarkProjections(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @DeleteMapping("/{verseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBookmark(
            @PathVariable String verseId,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        bookmarkRepository.deleteByUserAndVerse(
                user.getId(),
                verseId
        );
    }

    private User getAuthenticatedUser(Authentication authentication) {

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));
    }

    private BookmarkResponse toResponse(BookmarkProjection bookmark) {

        return new BookmarkResponse(
                bookmark.getVerseId(),
                bookmark.getChapter(),
                bookmark.getVerse(),
                bookmark.getSanskrit(),
                bookmark.getTranslation(),
                bookmark.getSummary(),
                bookmark.getCreatedAt()
        );
    }
}
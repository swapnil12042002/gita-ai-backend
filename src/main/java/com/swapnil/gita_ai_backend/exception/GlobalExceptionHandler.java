package com.swapnil.gita_ai_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException exception) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "timestamp", Instant.now(),
                        "status", 401,
                        "error", "Unauthorized",
                        "message", exception.getMessage() != null ? exception.getMessage() : "Invalid email or password"
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException exception) {

        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "timestamp", Instant.now(),
                        "status", 400,
                        "error", "Bad Request",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidJson(
            HttpMessageNotReadableException exception) {

        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "timestamp", Instant.now(),
                        "status", 400,
                        "error", "Bad Request",
                        "message", "Invalid request body"
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception) {

        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "timestamp", Instant.now(),
                        "status", 400,
                        "error", "Bad Request",
                        "message", "Invalid request data"
                ));
    }

    @ExceptionHandler(com.google.genai.errors.ApiException.class)
    public ResponseEntity<Map<String, Object>> handleGeminiApiException(
            com.google.genai.errors.ApiException exception) {

        int statusCode = exception.getStatusCode();
        String message = exception.getMessage();

        if (statusCode == 429 || (message != null && (message.contains("RESOURCE_EXHAUSTED") || message.toLowerCase().contains("quota")))) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "timestamp", Instant.now(),
                            "status", 429,
                            "error", "Too Many Requests",
                            "message", "Gemini API quota or rate limit exceeded. Please try again later."
                    ));
        }

        HttpStatus status = HttpStatus.resolve(statusCode);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ResponseEntity
                .status(status)
                .body(Map.of(
                        "timestamp", Instant.now(),
                        "status", status.value(),
                        "error", status.getReasonPhrase(),
                        "message", message != null ? message : "Gemini API error occurred."
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(
            Exception exception) {

        // Keep the actual exception in server logs.
        exception.printStackTrace();

        // Check if root cause or message is quota / rate-limit related
        String fullMsg = exception.getMessage() != null ? exception.getMessage() : "";
        Throwable cause = exception.getCause();
        while (cause != null) {
            if (cause.getMessage() != null) {
                fullMsg += " " + cause.getMessage();
            }
            cause = cause.getCause();
        }

        if (fullMsg.contains("429") || fullMsg.contains("RESOURCE_EXHAUSTED") || fullMsg.toLowerCase().contains("quota")) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "timestamp", Instant.now(),
                            "status", 429,
                            "error", "Too Many Requests",
                            "message", "Gemini API quota or rate limit exceeded. Please try again later."
                    ));
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "timestamp", Instant.now(),
                        "status", 500,
                        "error", "Internal Server Error",
                        "message", "Something went wrong while processing the request."
                ));
    }
}
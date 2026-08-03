package com.swapnil.gita_ai_backend.controller.auth;

import com.swapnil.gita_ai_backend.dto.request.LoginRequest;
import com.swapnil.gita_ai_backend.dto.request.RegisterRequest;
import com.swapnil.gita_ai_backend.dto.response.UserResponse;
import com.swapnil.gita_ai_backend.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
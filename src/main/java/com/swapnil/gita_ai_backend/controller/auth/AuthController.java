package com.swapnil.gita_ai_backend.controller.auth;

import com.swapnil.gita_ai_backend.dto.request.GoogleLoginRequest;
import com.swapnil.gita_ai_backend.dto.request.LoginRequest;
import com.swapnil.gita_ai_backend.dto.request.RegisterRequest;
import com.swapnil.gita_ai_backend.dto.response.UserResponse;
import com.swapnil.gita_ai_backend.service.auth.AuthService;
import com.swapnil.gita_ai_backend.dto.response.AuthResponse;
import com.swapnil.gita_ai_backend.service.auth.GoogleAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/google")
    public AuthResponse googleLogin(
            @Valid @RequestBody GoogleLoginRequest request) {

        return googleAuthService.authenticate(request);
    }
}
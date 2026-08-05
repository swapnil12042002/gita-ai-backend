package com.swapnil.gita_ai_backend.service.auth;

import com.swapnil.gita_ai_backend.dto.request.LoginRequest;
import com.swapnil.gita_ai_backend.dto.request.RegisterRequest;
import com.swapnil.gita_ai_backend.dto.response.AuthResponse;
import com.swapnil.gita_ai_backend.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
package com.swapnil.gita_ai_backend.service.auth;

import com.swapnil.gita_ai_backend.dto.request.GoogleLoginRequest;
import com.swapnil.gita_ai_backend.dto.response.AuthResponse;

public interface GoogleAuthService {

    AuthResponse authenticate(GoogleLoginRequest request);

}
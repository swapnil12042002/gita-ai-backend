package com.swapnil.gita_ai_backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthResponse {

    private String accessToken;

    private String tokenType;

    private UserResponse user;
}
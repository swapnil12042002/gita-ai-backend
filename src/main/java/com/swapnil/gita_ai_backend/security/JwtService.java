package com.swapnil.gita_ai_backend.security;

import com.swapnil.gita_ai_backend.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String generateToken(User user);

    String extractEmail(String token);

    boolean isTokenValid(String token, UserDetails userDetails);
}
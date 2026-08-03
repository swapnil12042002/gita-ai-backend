package com.swapnil.gita_ai_backend.service.auth;

import com.swapnil.gita_ai_backend.dto.request.RegisterRequest;
import com.swapnil.gita_ai_backend.dto.response.UserResponse;
import com.swapnil.gita_ai_backend.dto.request.LoginRequest;
import org.springframework.security.authentication.BadCredentialsException;
import com.swapnil.gita_ai_backend.entity.User;
import com.swapnil.gita_ai_backend.entity.enums.AuthProvider;
import com.swapnil.gita_ai_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse register(RegisterRequest request) {

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(AuthProvider.LOCAL)
                .build();

        User savedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(savedUser.getId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .picture(savedUser.getPicture())
                .provider(savedUser.getProvider().name())
                .build();
    }

    @Override
    public UserResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .picture(user.getPicture())
                .provider(user.getProvider().name())
                .build();
    }
}
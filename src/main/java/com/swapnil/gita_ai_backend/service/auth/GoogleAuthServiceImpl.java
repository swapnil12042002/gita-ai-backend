package com.swapnil.gita_ai_backend.service.auth;

import com.swapnil.gita_ai_backend.config.GoogleProperties;
import com.swapnil.gita_ai_backend.dto.request.GoogleLoginRequest;
import com.swapnil.gita_ai_backend.dto.response.AuthResponse;
import com.swapnil.gita_ai_backend.dto.response.UserResponse;
import com.swapnil.gita_ai_backend.entity.User;
import com.swapnil.gita_ai_backend.entity.enums.AuthProvider;
import com.swapnil.gita_ai_backend.repository.UserRepository;
import com.swapnil.gita_ai_backend.security.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.util.Collections;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleAuthServiceImpl implements GoogleAuthService {

    private final GoogleProperties googleProperties;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public AuthResponse authenticate(GoogleLoginRequest request) {

        try {

            GoogleIdTokenVerifier verifier =
                    new GoogleIdTokenVerifier.Builder(
                            new NetHttpTransport(),
                            GsonFactory.getDefaultInstance()
                    )
                            .setAudience(Collections.singletonList(googleProperties.getClientId()))
                            .build();

            GoogleIdToken idToken = verifier.verify(request.getIdToken());

            if (idToken == null) {
                throw new RuntimeException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            String googleId = payload.getSubject();
            String email = payload.getEmail();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");
            String picture = (String) payload.get("picture");

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        User newUser = User.builder()
                                .firstName(firstName)
                                .lastName(lastName)
                                .email(email)
                                .picture(picture)
                                .googleId(googleId)
                                .provider(AuthProvider.GOOGLE)
                                .build();

                        return userRepository.save(newUser);
                    });

            String token = jwtService.generateToken(user);

            return AuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .user(
                            UserResponse.builder()
                                    .id(user.getId())
                                    .firstName(user.getFirstName())
                                    .lastName(user.getLastName())
                                    .email(user.getEmail())
                                    .picture(user.getPicture())
                                    .provider(user.getProvider().name())
                                    .build()
                    )
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed", e);
        }
    }
}
package com.nextpick.backend.service;

import com.nextpick.backend.dto.auth.RegisterRequest;
import com.nextpick.backend.config.JwtProperties;
import com.nextpick.backend.entity.RefreshToken;
import com.nextpick.backend.entity.User;
import com.nextpick.backend.repository.RefreshTokenRepository;
import com.nextpick.backend.repository.UserRepository;
import com.nextpick.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final AuthenticationManager manager = mock(AuthenticationManager.class);
    private final RefreshTokenRepository refreshTokens = mock(RefreshTokenRepository.class);
    private JwtUtil jwt;
    private AuthService service;

    @BeforeEach
    void setUp() {
        byte[] secret = new byte[32];
        new SecureRandom().nextBytes(secret);
        jwt = new JwtUtil(new JwtProperties(Base64.getEncoder().encodeToString(secret),
                900_000L, 604_800_000L));
        service = new AuthService(users, encoder, jwt, manager, refreshTokens);
    }

    @Test
    void registrationReturnsAccessAndPersistsHashedRefreshToken() {
        when(users.existsByEmail("ana@example.com")).thenReturn(false);
        when(encoder.encode("password123")).thenReturn("hash");
        when(users.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(5L);
            return user;
        });
        when(refreshTokens.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AuthService.AuthSession session = service.register(new RegisterRequest("Ana", "ana@example.com", "password123"));

        assertThat(session.response().accessToken()).isNotBlank();
        assertThat(session.refreshToken()).isNotBlank();
        verify(refreshTokens).save(argThat(token -> token.getTokenHash().length() == 64
                && !token.getTokenHash().equals(session.refreshToken())));
    }

    @Test
    void malformedRefreshTokenIsUnauthorized() {
        assertThatThrownBy(() -> service.refresh("not-a-jwt"))
                .isInstanceOf(org.springframework.security.authentication.BadCredentialsException.class);
        verifyNoInteractions(refreshTokens);
    }
}

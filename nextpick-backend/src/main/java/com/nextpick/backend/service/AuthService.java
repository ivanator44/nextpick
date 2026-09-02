package com.nextpick.backend.service;

import com.nextpick.backend.dto.auth.AuthResponse;
import com.nextpick.backend.dto.auth.LoginRequest;
import com.nextpick.backend.dto.auth.RegisterRequest;
import com.nextpick.backend.entity.Role;
import com.nextpick.backend.entity.RefreshToken;
import com.nextpick.backend.entity.User;
import com.nextpick.backend.exception.EmailAlreadyExistsException;
import com.nextpick.backend.repository.UserRepository;
import com.nextpick.backend.repository.RefreshTokenRepository;
import com.nextpick.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthSession register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);
        return buildSession(user);
    }

    public AuthSession login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado tras autenticar"));

        return buildSession(user);
    }

    /**
     * Renueva el access token a partir de un refresh token válido.
     * No requiere contraseña: solo se exige que el refresh token no haya expirado
     * y que el usuario siga existiendo.
     */
    public AuthSession refresh(String refreshToken) {
        if (!jwtUtil.isRefreshTokenValid(refreshToken)) {
            throw new BadCredentialsException("Refresh token inválido o caducado");
        }

        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(refreshToken))
                .filter(RefreshToken::active)
                .orElseThrow(() -> new BadCredentialsException("Refresh token revocado o caducado"));
        String email = jwtUtil.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        stored.setRevokedAt(Instant.now());
        refreshTokenRepository.save(stored);
        return buildSession(user);
    }

    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;
        refreshTokenRepository.findByTokenHash(hash(refreshToken)).ifPresent(stored -> {
            stored.setRevokedAt(Instant.now());
            refreshTokenRepository.save(stored);
        });
    }

    private AuthSession buildSession(User user) {
        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                Map.of("role", user.getRole().name(), "uid", user.getId())
        );
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(hash(refreshToken))
                .expiresAt(Instant.now().plusMillis(jwtUtil.refreshExpirationMs()))
                .build());

        AuthResponse response = new AuthResponse(
                accessToken,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAvatarUrl()
        );
        return new AuthSession(response, refreshToken, jwtUtil.refreshExpirationMs());
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }

    public record AuthSession(AuthResponse response, String refreshToken, long refreshExpirationMs) {
    }
}

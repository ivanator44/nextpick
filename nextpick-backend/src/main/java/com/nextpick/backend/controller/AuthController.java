package com.nextpick.backend.controller;

import com.nextpick.backend.dto.auth.AuthResponse;
import com.nextpick.backend.dto.auth.LoginRequest;
import com.nextpick.backend.dto.auth.RefreshRequest;
import com.nextpick.backend.dto.auth.RegisterRequest;
import com.nextpick.backend.config.SecurityProperties;
import com.nextpick.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String REFRESH_COOKIE = "nextpick_refresh";

    private final AuthService authService;
    private final SecurityProperties.RefreshCookie refreshCookie;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthService.AuthSession session = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Set-Cookie", cookie(session.refreshToken(), session.refreshExpirationMs()).toString())
                .body(session.response());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthSession session = authService.login(request);
        return ResponseEntity.ok()
                .header("Set-Cookie", cookie(session.refreshToken(), session.refreshExpirationMs()).toString())
                .body(session.response());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String token) {
        if (token == null || token.isBlank()) {
            throw new org.springframework.security.authentication.BadCredentialsException("No hay sesión renovable");
        }
        AuthService.AuthSession session = authService.refresh(token);
        return ResponseEntity.ok()
                .header("Set-Cookie", cookie(session.refreshToken(), session.refreshExpirationMs()).toString())
                .body(session.response());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_COOKIE, required = false) String token) {
        authService.logout(token);
        return ResponseEntity.noContent()
                .header("Set-Cookie", cookie("", 0).toString())
                .build();
    }

    private ResponseCookie cookie(String value, long maxAgeMs) {
        return ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(refreshCookie.secure())
                .sameSite(refreshCookie.sameSite())
                .path(refreshCookie.path())
                .maxAge(java.time.Duration.ofMillis(maxAgeMs))
                .build();
    }
}

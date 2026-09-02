package com.nextpick.backend.dto.auth;

public record AuthResponse(
        String accessToken,
        Long userId,
        String name,
        String email,
        String avatarUrl
) {}

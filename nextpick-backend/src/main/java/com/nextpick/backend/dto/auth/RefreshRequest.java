package com.nextpick.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

// Petición para renovar el access token usando el refresh token guardado en el cliente
public record RefreshRequest(
        @NotBlank String refreshToken
) {}

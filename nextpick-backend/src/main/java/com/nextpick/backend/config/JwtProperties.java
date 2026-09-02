package com.nextpick.backend.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Base64;

@Validated
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        @NotBlank String secret,
        @Positive long accessExpirationMs,
        @Positive long refreshExpirationMs
) {
    @AssertTrue(message = "JWT_SECRET debe ser Base64 válido y contener al menos 256 bits")
    public boolean isSecretValid() {
        if (secret == null || secret.isBlank()) return false;
        try {
            return Base64.getDecoder().decode(secret).length >= 32;
        } catch (IllegalArgumentException invalidBase64) {
            return false;
        }
    }
}

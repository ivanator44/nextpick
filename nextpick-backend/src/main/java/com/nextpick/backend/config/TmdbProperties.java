package com.nextpick.backend.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "tmdb")
public record TmdbProperties(
        @NotBlank String baseUrl,
        @NotBlank String accessToken,
        @NotBlank String language,
        @NotBlank String region,
        @NotBlank String imageBaseUrl,
        @NotNull Duration connectTimeout,
        @NotNull Duration requestTimeout,
        @Min(0) @Max(3) int maxRetries
) {
}

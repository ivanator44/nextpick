package com.nextpick.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.Duration;

@ConfigurationProperties(prefix = "ai")
@Validated
public record AiProperties(
        @NotBlank @Pattern(regexp = "(?i)(openai|gemini|disabled)", message = "debe ser openai, gemini o disabled") String provider,
        String apiKey,
        String geminiApiKey,
        String baseUrl,
        @Pattern(regexp = "[A-Za-z0-9._-]*", message = "contiene caracteres no válidos") String model,
        @NotNull Duration connectTimeout,
        @NotNull Duration requestTimeout,
        @Min(1) @Max(20) int maxHistoryMessages
) {
    public boolean configured() {
        return !effectiveApiKey().isBlank();
    }

    public String effectiveApiKey() {
        if ("gemini".equalsIgnoreCase(provider) && geminiApiKey != null && !geminiApiKey.isBlank()) {
            return geminiApiKey;
        }
        return apiKey == null ? "" : apiKey;
    }

    public String effectiveBaseUrl() {
        if (baseUrl != null && !baseUrl.isBlank()) return baseUrl;
        return "gemini".equalsIgnoreCase(provider)
                ? "https://generativelanguage.googleapis.com"
                : "https://api.openai.com/v1";
    }

    public String effectiveModel() {
        if (model != null && !model.isBlank()) return model;
        return "gemini".equalsIgnoreCase(provider) ? "gemini-3.6-flash" : "gpt-4o-mini";
    }
}

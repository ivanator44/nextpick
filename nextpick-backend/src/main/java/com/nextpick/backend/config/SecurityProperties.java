package com.nextpick.backend.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.List;

public final class SecurityProperties {
    private SecurityProperties() {
    }

    @Validated
    @ConfigurationProperties(prefix = "security.cors")
    public record Cors(@NotBlank String allowedOrigins) {
        public List<String> origins() {
            return Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .toList();
        }
    }

    @Validated
    @ConfigurationProperties(prefix = "security.refresh-cookie")
    public record RefreshCookie(
            boolean secure,
            @NotBlank @Pattern(regexp = "(?i)(Strict|Lax|None)") String sameSite,
            @NotBlank String path
    ) {
    }
}

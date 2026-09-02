package com.nextpick.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Email(message = "Email no válido") @Size(max = 150)
        String email,

        @NotBlank @Size(max = 128)
        String password
) {}

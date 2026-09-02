package com.nextpick.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 80)
        String name,

        @NotBlank @Email(message = "Email no válido") @Size(max = 150)
        String email,

        @NotBlank @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
        String password
) {}

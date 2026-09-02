package com.nextpick.backend.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Petición del cliente al enviar un mensaje al asistente.
// conversationId es null cuando el usuario empieza un chat nuevo:
// el backend crea la conversación y devuelve su id en el stream.
public record ChatRequest(
        Long conversationId,
        @NotBlank(message = "El mensaje no puede estar vacío")
        @Size(max = 2000, message = "El mensaje no puede superar 2000 caracteres")
        String content
) {}

package com.nextpick.backend.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;
import java.util.function.BooleanSupplier;

/** Deterministic development/test implementation. Never active in production. */
@Service
@Profile("mock")
public class MockChatAiService implements ChatAiService {
    @Override
    public void streamResponse(AiChatRequest request, Consumer<String> onToken, BooleanSupplier cancelled) {
        String reply = "Modo de demostración: configura el proveedor de IA para obtener recomendaciones verificadas.";
        for (String word : reply.split(" ")) {
            if (cancelled.getAsBoolean()) return;
            onToken.accept(word + " ");
        }
    }
}

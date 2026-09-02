package com.nextpick.backend.service;

import com.nextpick.backend.exception.AiNotConfiguredException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.function.Consumer;
import java.util.function.BooleanSupplier;

/** Explicit production-safe fallback while no authorized provider is configured. */
@Service
@Profile("!mock")
@ConditionalOnProperty(name = "ai.provider", havingValue = "disabled")
public class UnavailableChatAiService implements ChatAiService {
    @Override
    public void streamResponse(AiChatRequest request, Consumer<String> onToken, BooleanSupplier cancelled) {
        throw new AiNotConfiguredException(AiErrorMessages.NOT_CONFIGURED);
    }
}

package com.nextpick.backend.service;

import java.util.function.Consumer;
import java.util.function.BooleanSupplier;
import java.util.List;

/** Provider-neutral boundary used by the chat streaming controller. */
public interface ChatAiService {
    void streamResponse(AiChatRequest request, Consumer<String> onToken, BooleanSupplier cancelled);

    record AiChatRequest(List<ChatTurn> history, List<GroundedTitle> catalogContext) {
        public AiChatRequest {
            history = List.copyOf(history);
            catalogContext = List.copyOf(catalogContext);
        }
    }

    record ChatTurn(String role, String content) {}

    record GroundedTitle(long tmdbId, String mediaType, String title, String posterUrl, Integer year,
                         Double rating, List<String> genres, List<String> streamingProviders) {}
}

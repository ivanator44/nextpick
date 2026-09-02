package com.nextpick.backend.service;

import com.nextpick.backend.exception.AiNotConfiguredException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnavailableChatAiServiceTest {

    @Test
    void doesNotExposeEnvironmentConfigurationInstructions() {
        UnavailableChatAiService service = new UnavailableChatAiService();

        assertThatThrownBy(() -> service.streamResponse(
                new ChatAiService.AiChatRequest(List.of(), List.of()), ignored -> {}, () -> false))
                .isInstanceOf(AiNotConfiguredException.class)
                .hasMessage(AiErrorMessages.NOT_CONFIGURED)
                .hasMessageNotContaining("AI_API_KEY")
                .hasMessageNotContaining("proveedor autorizado");
    }
}

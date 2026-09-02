package com.nextpick.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextpick.backend.config.AiProperties;
import com.nextpick.backend.exception.AiNotConfiguredException;
import com.nextpick.backend.exception.UpstreamServiceException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeminiChatAiServiceTest {
    private MockWebServer server;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void startServer() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void stopServer() throws Exception {
        server.shutdown();
    }

    @Test
    void streamsGeminiTextAndSendsGroundedMultiTurnRequest() throws Exception {
        server.enqueue(sse("""
                data: {"candidates":[{"content":{"role":"model","parts":[{"text":"Prueba "}]}}]}

                data: {"candidates":[{"content":{"role":"model","parts":[{"text":"The Matrix."}]},"finishReason":"STOP"}]}

                """));
        List<String> tokens = new ArrayList<>();
        ChatAiService.AiChatRequest request = new ChatAiService.AiChatRequest(
                List.of(
                        new ChatAiService.ChatTurn("user", "Hola"),
                        new ChatAiService.ChatTurn("assistant", "¿Qué género buscas?"),
                        new ChatAiService.ChatTurn("user", "Ciencia ficción")),
                List.of(new ChatAiService.GroundedTitle(603, "MOVIE", "The Matrix", "/poster.jpg",
                        1999, 8.2, List.of("Ciencia ficción"), List.of("Max"))));

        service("key", "gemini-2.5-flash").streamResponse(request, tokens::add, () -> false);

        assertThat(tokens).containsExactly("Prueba ", "The Matrix.");
        RecordedRequest recorded = server.takeRequest();
        assertThat(recorded.getPath()).isEqualTo(
                "/v1beta/models/gemini-2.5-flash:streamGenerateContent?alt=sse");
        assertThat(recorded.getHeader("x-goog-api-key")).isEqualTo("key");
        assertThat(recorded.getHeader("Authorization")).isNull();

        JsonNode body = mapper.readTree(recorded.getBody().readUtf8());
        assertThat(body.path("system_instruction").path("parts").path(0).path("text").asText())
                .contains("CATALOGO_VERIFICADO", "The Matrix");
        assertThat(body.path("contents").size()).isEqualTo(3);
        assertThat(body.path("contents").path(1).path("role").asText()).isEqualTo("model");
        assertThat(body.path("generationConfig").path("maxOutputTokens").asInt()).isEqualTo(2048);
    }

    @Test
    void mergesAdjacentUserTurnsForGeminiConversationContract() throws Exception {
        server.enqueue(sse("""
                data: {"candidates":[{"content":{"parts":[{"text":"Respuesta"}]},"finishReason":"STOP"}]}

                """));
        service("key", "gemini-2.5-flash").streamResponse(new ChatAiService.AiChatRequest(
                List.of(new ChatAiService.ChatTurn("user", "Primero"),
                        new ChatAiService.ChatTurn("user", "Segundo")), List.of()), ignored -> {}, () -> false);

        JsonNode contents = mapper.readTree(server.takeRequest().getBody().readUtf8()).path("contents");
        assertThat(contents.size()).isEqualTo(1);
        assertThat(contents.path(0).path("parts").path(0).path("text").asText())
                .isEqualTo("Primero\nSegundo");
    }

    @Test
    void hidesUnavailableModelDetailsFromTheInterface() {
        server.enqueue(new MockResponse().setResponseCode(404)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"error\":{\"message\":\"Model not found\"}}"));

        assertThatThrownBy(() -> service("key", "retired-model").streamResponse(
                new ChatAiService.AiChatRequest(List.of(new ChatAiService.ChatTurn("user", "Hola")), List.of()),
                ignored -> {}, () -> false))
                .isInstanceOf(UpstreamServiceException.class)
                .hasMessage(AiErrorMessages.NOT_CONFIGURED)
                .hasMessageNotContaining("retired-model")
                .hasMessageNotContaining("Model not found");
    }

    @Test
    void hidesConfigurationDetailsWhenGeminiKeyIsMissing() {
        assertThatThrownBy(() -> service("", "gemini-2.5-flash").streamResponse(
                new ChatAiService.AiChatRequest(List.of(), List.of()), ignored -> {}, () -> false))
                .isInstanceOf(AiNotConfiguredException.class)
                .hasMessage(AiErrorMessages.NOT_CONFIGURED)
                .hasMessageNotContaining("API_KEY");
    }

    @Test
    void mapsHighDemandToAGenericMessageWithoutLeakingProviderText() {
        String providerText = "This model is currently experiencing high demand. "
                + "Spikes in demand are usually temporary. Please try again later";
        MockResponse overloaded = new MockResponse().setResponseCode(503)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"error\":{\"message\":\"" + providerText + "\"}}");
        server.enqueue(overloaded);
        server.enqueue(overloaded);

        assertThatThrownBy(() -> service("key", "gemini-3.6-flash").streamResponse(
                new ChatAiService.AiChatRequest(
                        List.of(new ChatAiService.ChatTurn("user", "Hola")), List.of()),
                ignored -> {}, () -> false))
                .isInstanceOf(UpstreamServiceException.class)
                .hasMessage(AiErrorMessages.HIGH_DEMAND)
                .hasMessageNotContaining("This model")
                .hasMessageNotContaining("Spikes in demand");
    }

    private GeminiChatAiService service(String key, String model) {
        AiProperties properties = new AiProperties("gemini", "", key, server.url("/").toString(), model,
                Duration.ofSeconds(1), Duration.ofSeconds(3), 12);
        return new GeminiChatAiService(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build(), mapper, properties);
    }

    private MockResponse sse(String body) {
        return new MockResponse().setResponseCode(200)
                .addHeader("Content-Type", "text/event-stream")
                .setBody(body);
    }
}

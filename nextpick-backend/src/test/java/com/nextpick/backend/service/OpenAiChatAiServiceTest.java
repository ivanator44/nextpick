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
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenAiChatAiServiceTest {
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
    void streamsDeltasAndSendsOnlyBoundedHistoryAndGroundedContext() throws Exception {
        server.enqueue(sse("""
                event: response.output_text.delta
                data: {"type":"response.output_text.delta","delta":"Hola "}

                event: response.output_text.delta
                data: {"type":"response.output_text.delta","delta":"mundo"}

                event: response.completed
                data: {"type":"response.completed"}

                """));
        OpenAiChatAiService service = service("test-key");
        List<String> tokens = new ArrayList<>();
        ChatAiService.AiChatRequest request = new ChatAiService.AiChatRequest(
                List.of(new ChatAiService.ChatTurn("user", "Quiero ciencia ficción")),
                List.of(new ChatAiService.GroundedTitle(603, "MOVIE", "The Matrix", "/poster.jpg",
                        1999, 8.2, List.of("Ciencia ficción"), List.of("Max"))));

        service.streamResponse(request, tokens::add, () -> false);

        assertThat(tokens).containsExactly("Hola ", "mundo");
        RecordedRequest recorded = server.takeRequest();
        assertThat(recorded.getPath()).isEqualTo("/v1/responses");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer test-key");
        JsonNode body = mapper.readTree(recorded.getBody().readUtf8());
        assertThat(body.path("store").asBoolean()).isFalse();
        assertThat(body.path("stream").asBoolean()).isTrue();
        assertThat(body.path("max_output_tokens").asInt()).isEqualTo(1200);
        assertThat(body.path("input").size()).isEqualTo(1);
        assertThat(body.path("instructions").asText()).contains("CATALOGO_VERIFICADO", "The Matrix");
        assertThat(body.toString()).doesNotContain("email", "accessToken", "JWT");
    }

    @Test
    void retriesOneRateLimitAndHonorsCancellation() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(429).addHeader("Retry-After", "0"));
        server.enqueue(sse("""
                event: response.output_text.delta
                data: {"type":"response.output_text.delta","delta":"texto"}

                """));
        List<String> tokens = new ArrayList<>();
        service("test-key").streamResponse(new ChatAiService.AiChatRequest(
                List.of(new ChatAiService.ChatTurn("user", "hola")), List.of()), tokens::add, () -> false);

        assertThat(tokens).containsExactly("texto");
        assertThat(server.getRequestCount()).isEqualTo(2);
    }

    @Test
    void stopsConsumingTheStreamWhenCancelled() {
        server.enqueue(sse("""
                event: response.output_text.delta
                data: {"type":"response.output_text.delta","delta":"primero"}

                event: response.output_text.delta
                data: {"type":"response.output_text.delta","delta":"segundo"}

                """));
        AtomicBoolean cancelled = new AtomicBoolean(false);
        List<String> tokens = new ArrayList<>();

        service("test-key").streamResponse(new ChatAiService.AiChatRequest(List.of(), List.of()), token -> {
            tokens.add(token);
            cancelled.set(true);
        }, cancelled::get);

        assertThat(tokens).containsExactly("primero");
    }

    @Test
    void hidesConfigurationDetailsWithoutApiKey() {
        assertThatThrownBy(() -> service("").streamResponse(
                new ChatAiService.AiChatRequest(List.of(), List.of()), ignored -> {}, () -> false))
                .isInstanceOf(AiNotConfiguredException.class)
                .hasMessage(AiErrorMessages.NOT_CONFIGURED)
                .hasMessageNotContaining("API_KEY");
    }

    @Test
    void hidesBillingDetailsAndReturnsAGenericRateLimitWithoutRetrying() {
        server.enqueue(new MockResponse().setResponseCode(429)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {"error":{"message":"You have no credits remaining. Add credits to continue using the API.",
                        "type":"insufficient_quota","code":"insufficient_quota"}}
                        """));

        assertThatThrownBy(() -> service("test-key").streamResponse(
                new ChatAiService.AiChatRequest(List.of(), List.of()), ignored -> {}, () -> false))
                .isInstanceOf(UpstreamServiceException.class)
                .hasMessage(AiErrorMessages.RATE_LIMIT)
                .hasMessageNotContaining("credits")
                .hasMessageNotContaining("billing");
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void hidesFailedStreamProviderText() {
        server.enqueue(sse("""
                event: response.failed
                data: {"type":"response.failed","response":{"error":{"message":"Solicitud rechazada"}}}

                """));

        assertThatThrownBy(() -> service("test-key").streamResponse(
                new ChatAiService.AiChatRequest(List.of(), List.of()), ignored -> {}, () -> false))
                .isInstanceOf(UpstreamServiceException.class)
                .hasMessage(AiErrorMessages.TEMPORARY)
                .hasMessageNotContaining("Solicitud rechazada");
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    private OpenAiChatAiService service(String key) {
        AiProperties properties = new AiProperties("openai", key, "", server.url("/v1").toString(),
                "gpt-4o-mini", Duration.ofSeconds(1), Duration.ofSeconds(3), 12);
        return new OpenAiChatAiService(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build(),
                mapper, properties);
    }

    private MockResponse sse(String body) {
        return new MockResponse().setResponseCode(200)
                .addHeader("Content-Type", "text/event-stream")
                .setBody(body);
    }
}

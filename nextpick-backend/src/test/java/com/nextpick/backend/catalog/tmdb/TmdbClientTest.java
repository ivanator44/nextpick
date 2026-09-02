package com.nextpick.backend.catalog.tmdb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextpick.backend.config.TmdbProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TmdbClientTest {
    private MockWebServer server;
    private TmdbClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        TmdbProperties properties = new TmdbProperties(server.url("/3").toString(), "test-token", "es-ES", "ES",
                "https://images.test", Duration.ofSeconds(1), Duration.ofSeconds(2), 1);
        client = new TmdbClient(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build(),
                new ObjectMapper(), properties);
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void sendsBearerAndLanguageAndParsesTypedPage() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json")
                .setBody("""
                        {"page":1,"results":[{"id":7,"title":"Test","media_type":"movie"}],"total_pages":1,"total_results":1}
                        """));

        TmdbDtos.Page page = client.getPage("/search/multi", Map.of("query", "Test", "page", 1));

        assertThat(page.results()).hasSize(1);
        var request = server.takeRequest();
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test-token");
        assertThat(request.getRequestUrl().queryParameter("language")).isEqualTo("es-ES");
        assertThat(request.getRequestUrl().queryParameter("query")).isEqualTo("Test");
    }

    @Test
    void retriesOnceAfterRateLimit() {
        server.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));
        server.enqueue(new MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json")
                .setBody("""
                        {"page":1,"results":[],"total_pages":1,"total_results":0}
                        """));

        client.getPage("/trending/all/week", Map.of());

        assertThat(server.getRequestCount()).isEqualTo(2);
    }
}

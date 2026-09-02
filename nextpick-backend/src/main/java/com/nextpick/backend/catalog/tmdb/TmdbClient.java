package com.nextpick.backend.catalog.tmdb;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextpick.backend.config.TmdbProperties;
import com.nextpick.backend.exception.UpstreamServiceException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class TmdbClient {
    private static final String SERVICE = "TMDB";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final TmdbProperties properties;

    public TmdbClient(@Qualifier("tmdbHttpClient") HttpClient httpClient,
                      ObjectMapper objectMapper,
                      TmdbProperties properties) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public TmdbDtos.Page getPage(String path, Map<String, ?> parameters) {
        return get(path, parameters, objectMapper.getTypeFactory().constructType(TmdbDtos.Page.class));
    }

    public TmdbDtos.Detail getDetail(String path, Map<String, ?> parameters) {
        return get(path, parameters, objectMapper.getTypeFactory().constructType(TmdbDtos.Detail.class));
    }

    public TmdbDtos.ProviderResponse getProviders(String path) {
        return get(path, Map.of(), objectMapper.getTypeFactory().constructType(TmdbDtos.ProviderResponse.class));
    }

    public TmdbDtos.Genres getGenres(String path) {
        return get(path, Map.of(), objectMapper.getTypeFactory().constructType(TmdbDtos.Genres.class));
    }

    private <T> T get(String path, Map<String, ?> parameters, JavaType responseType) {
        URI uri = buildUri(path, parameters);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(properties.requestTimeout())
                .header("Authorization", "Bearer " + properties.accessToken())
                .header("Accept", "application/json")
                .GET()
                .build();

        for (int attempt = 0; attempt <= properties.maxRetries(); attempt++) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();
                if (status >= 200 && status < 300) {
                    return objectMapper.readValue(response.body(), responseType);
                }
                if ((status == 429 || status >= 500) && attempt < properties.maxRetries()) {
                    waitBeforeRetry(response, attempt);
                    continue;
                }
                throw upstreamError(status);
            } catch (java.net.http.HttpTimeoutException ex) {
                if (attempt == properties.maxRetries()) {
                    throw new UpstreamServiceException(SERVICE, 504, "TMDB no respondió dentro del tiempo límite");
                }
                waitBeforeRetry(null, attempt);
            } catch (IOException ex) {
                if (attempt == properties.maxRetries()) {
                    throw new UpstreamServiceException(SERVICE, 502, "No se pudo conectar con TMDB");
                }
                waitBeforeRetry(null, attempt);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new UpstreamServiceException(SERVICE, 503, "La consulta a TMDB fue interrumpida");
            }
        }
        throw new UpstreamServiceException(SERVICE, 502, "Error inesperado consultando TMDB");
    }

    private URI buildUri(String path, Map<String, ?> parameters) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(properties.baseUrl())
                .path(path)
                .queryParam("language", properties.language());
        new LinkedHashMap<>(parameters).forEach((key, value) -> {
            if (value != null && !value.toString().isBlank()) {
                builder.queryParam(key, value);
            }
        });
        return builder.build().encode().toUri();
    }

    private UpstreamServiceException upstreamError(int status) {
        return switch (status) {
            case 401, 403 -> new UpstreamServiceException(SERVICE, status, "La autenticación con TMDB no es válida");
            case 404 -> new UpstreamServiceException(SERVICE, status, "El título solicitado no existe en TMDB");
            case 429 -> new UpstreamServiceException(SERVICE, status, "TMDB ha limitado temporalmente las solicitudes");
            default -> new UpstreamServiceException(SERVICE, status, "TMDB devolvió un error temporal");
        };
    }

    private void waitBeforeRetry(HttpResponse<?> response, int attempt) {
        Duration delay = Duration.ofMillis(250L * (1L << attempt));
        if (response != null) {
            delay = response.headers().firstValue("Retry-After")
                    .flatMap(this::parseRetryAfter)
                    .orElse(delay);
        }
        try {
            Thread.sleep(Math.min(delay.toMillis(), 2_000L));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new UpstreamServiceException(SERVICE, 503, "La consulta a TMDB fue interrumpida");
        }
    }

    private java.util.Optional<Duration> parseRetryAfter(String value) {
        try {
            return java.util.Optional.of(Duration.ofSeconds(Long.parseLong(value.trim())));
        } catch (NumberFormatException ignored) {
            return java.util.Optional.empty();
        }
    }
}

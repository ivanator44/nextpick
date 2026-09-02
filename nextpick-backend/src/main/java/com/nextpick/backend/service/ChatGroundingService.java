package com.nextpick.backend.service;

import com.nextpick.backend.catalog.CatalogService;
import com.nextpick.backend.catalog.dto.CatalogDetail;
import com.nextpick.backend.catalog.dto.CatalogSummary;
import com.nextpick.backend.catalog.dto.ProviderDto;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;

@Service
public class ChatGroundingService {
    private static final int MAX_QUERY_LENGTH = 100;
    private static final int MAX_REFERENCES = 4;
    private final CatalogService catalogService;

    public ChatGroundingService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /** Searches TMDB through CatalogService using only a bounded, control-character-free query. */
    public List<ChatAiService.GroundedTitle> ground(String message) {
        String query = sanitizeQuery(message);
        try {
            List<CatalogSummary> matches = query.length() >= 2
                    ? catalogService.search(query, 0).content().stream().limit(MAX_REFERENCES).toList()
                    : List.of();
            if (matches.isEmpty()) {
                matches = catalogService.trending(0).content().stream().limit(MAX_REFERENCES).toList();
            }
            return matches.stream().map(this::hydrateSafely).toList();
        } catch (RuntimeException upstreamFailure) {
            return List.of();
        }
    }

    private String sanitizeQuery(String message) {
        if (message == null) return "";
        String query = message.replaceAll("[\\p{Cc}\\p{Cf}]", " ").replaceAll("\\s+", " ").trim();
        return query.length() > MAX_QUERY_LENGTH ? query.substring(0, MAX_QUERY_LENGTH) : query;
    }

    private ChatAiService.GroundedTitle hydrateSafely(CatalogSummary summary) {
        try {
            CatalogDetail detail = catalogService.detail(summary.mediaType(), summary.tmdbId());
            LinkedHashSet<String> providers = new LinkedHashSet<>();
            detail.providers().streaming().stream().map(ProviderDto::name).forEach(providers::add);
            detail.providers().free().stream().map(ProviderDto::name).forEach(providers::add);
            return new ChatAiService.GroundedTitle(detail.tmdbId(), detail.mediaType().name(), detail.title(), detail.posterUrl(),
                    detail.releaseYear(), detail.rating(), detail.genres().stream().map(genre -> genre.name()).toList(),
                    providers.stream().toList());
        } catch (RuntimeException detailFailure) {
            return new ChatAiService.GroundedTitle(summary.tmdbId(), summary.mediaType().name(), summary.title(), summary.posterUrl(),
                    summary.releaseYear(), summary.rating(), List.of(), List.of());
        }
    }
}

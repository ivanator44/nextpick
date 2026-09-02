package com.nextpick.backend.service;

import com.nextpick.backend.catalog.CatalogService;
import com.nextpick.backend.catalog.dto.CatalogSummary;
import com.nextpick.backend.config.CacheConfig;
import com.nextpick.backend.entity.MediaType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private final FavoriteService favoriteService;
    private final CatalogService catalogService;

    public RecommendationService(FavoriteService favoriteService, CatalogService catalogService) {
        this.favoriteService = favoriteService;
        this.catalogService = catalogService;
    }

    /** Content-based recommendations; this is not a proprietary ML model. */
    @Cacheable(cacheNames = CacheConfig.RECOMMENDATIONS, key = "#userId", sync = true)
    public List<CatalogSummary> forUser(long userId) {
        List<CatalogSummary> favorites = favoriteService.list(userId);
        if (favorites.isEmpty()) {
            return catalogService.trending(0).content().stream().limit(20).toList();
        }

        Set<String> excluded = favorites.stream().map(CatalogSummary::identity).collect(Collectors.toSet());
        LinkedHashMap<String, CatalogSummary> candidates = new LinkedHashMap<>();

        // Related titles are the strongest signal. Bound hydration to three favorites so
        // recommendation generation cannot fan out without limit; detail calls are cached.
        favorites.stream().limit(3).forEach(favorite -> {
            try {
                addCandidates(candidates,
                        catalogService.detail(favorite.mediaType(), favorite.tmdbId()).recommendations(), excluded);
            } catch (RuntimeException ignored) {
                // A related-title failure must not remove genre-based recommendations.
            }
        });

        List<Integer> preferredGenres = favorites.stream()
                .flatMap(item -> item.genreIds().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        for (Integer genreId : preferredGenres) {
            addCandidates(candidates, catalogService.discover(MediaType.MOVIE, genreId, 0).content(), excluded);
            addCandidates(candidates, catalogService.discover(MediaType.SERIES, genreId, 0).content(), excluded);
        }
        if (candidates.size() < 10) {
            addCandidates(candidates, catalogService.trending(0).content(), excluded);
        }
        return candidates.values().stream().limit(20).toList();
    }

    private void addCandidates(Map<String, CatalogSummary> target, List<CatalogSummary> source, Set<String> excluded) {
        source.stream()
                .filter(item -> !excluded.contains(item.identity()))
                .forEach(item -> target.putIfAbsent(item.identity(), item));
    }
}

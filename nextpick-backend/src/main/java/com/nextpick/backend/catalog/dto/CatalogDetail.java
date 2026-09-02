package com.nextpick.backend.catalog.dto;

import com.nextpick.backend.entity.MediaType;

import java.util.List;

public record CatalogDetail(
        long tmdbId,
        MediaType mediaType,
        String title,
        String overview,
        String posterUrl,
        String backdropUrl,
        Double rating,
        Integer releaseYear,
        List<GenreDto> genres,
        TrailerDto trailer,
        WatchProvidersDto providers,
        List<CatalogSummary> recommendations,
        boolean favorite
) {
    public CatalogDetail withFavorite(boolean value) {
        return new CatalogDetail(tmdbId, mediaType, title, overview, posterUrl, backdropUrl,
                rating, releaseYear, genres, trailer, providers, recommendations, value);
    }
}

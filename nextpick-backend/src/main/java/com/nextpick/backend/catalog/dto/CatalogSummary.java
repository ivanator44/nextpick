package com.nextpick.backend.catalog.dto;

import com.nextpick.backend.entity.MediaType;

import java.util.List;

public record CatalogSummary(
        long tmdbId,
        MediaType mediaType,
        String title,
        String overview,
        String posterUrl,
        String backdropUrl,
        Double rating,
        Integer releaseYear,
        List<Integer> genreIds
) {
    public String identity() {
        return mediaType + ":" + tmdbId;
    }
}

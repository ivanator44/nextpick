package com.nextpick.backend.dto.movie;

import com.nextpick.backend.entity.MediaType;
import com.nextpick.backend.entity.Movie;

import java.util.List;

public record MovieDetailDto(
        Long id,
        String title,
        String synopsis,
        Integer releaseYear,
        Double rating,
        String posterUrl,
        String backdropUrl,
        String trailerUrl,
        MediaType mediaType,
        List<String> genres,
        boolean isFavorite
) {
    public static MovieDetailDto fromEntity(Movie movie, boolean isFavorite) {
        return new MovieDetailDto(
                movie.getId(),
                movie.getTitle(),
                movie.getSynopsis(),
                movie.getReleaseYear(),
                movie.getRating(),
                movie.getPosterUrl(),
                movie.getBackdropUrl(),
                movie.getTrailerUrl(),
                movie.getMediaType(),
                movie.getGenres().stream().map(g -> g.getName()).toList(),
                isFavorite
        );
    }
}

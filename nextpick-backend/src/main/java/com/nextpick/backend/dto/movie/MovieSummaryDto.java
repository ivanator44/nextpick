package com.nextpick.backend.dto.movie;

import com.nextpick.backend.entity.MediaType;
import com.nextpick.backend.entity.Movie;

// DTO ligero para carruseles y grids (no carga sinopsis completa)
public record MovieSummaryDto(
        Long id,
        String title,
        String posterUrl,
        Double rating,
        Integer releaseYear,
        MediaType mediaType
) {
    public static MovieSummaryDto fromEntity(Movie movie) {
        return new MovieSummaryDto(
                movie.getId(),
                movie.getTitle(),
                movie.getPosterUrl(),
                movie.getRating(),
                movie.getReleaseYear(),
                movie.getMediaType()
        );
    }
}

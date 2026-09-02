package com.nextpick.backend.catalog.mapper;

import com.nextpick.backend.catalog.tmdb.TmdbDtos;
import com.nextpick.backend.config.TmdbProperties;
import com.nextpick.backend.entity.MediaType;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogMapperTest {
    private final TmdbProperties properties = new TmdbProperties("http://tmdb", "token", "es-ES", "ES",
            "https://image.tmdb.org/t/p", Duration.ofSeconds(1), Duration.ofSeconds(2), 0);

    @Test
    void mapsMovieSpecificTitleAndReleaseDate() {
        var raw = new TmdbDtos.Media(10, "La película", null, "Resumen", "/poster.jpg", "/backdrop.jpg",
                7.45, "2024-03-10", null, List.of(28), "movie");

        var mapped = new MovieCatalogMapper(properties).summary(raw);

        assertThat(mapped.tmdbId()).isEqualTo(10);
        assertThat(mapped.mediaType()).isEqualTo(MediaType.MOVIE);
        assertThat(mapped.title()).isEqualTo("La película");
        assertThat(mapped.releaseYear()).isEqualTo(2024);
        assertThat(mapped.posterUrl()).endsWith("/w500/poster.jpg");
    }

    @Test
    void mapsTvSpecificNameAndFirstAirDate() {
        var raw = new TmdbDtos.Media(20, null, "La serie", "Resumen", "/poster.jpg", null,
                8.1, null, "2022-11-01", List.of(18), "tv");

        var mapped = new TvCatalogMapper(properties).summary(raw);

        assertThat(mapped.tmdbId()).isEqualTo(20);
        assertThat(mapped.mediaType()).isEqualTo(MediaType.SERIES);
        assertThat(mapped.title()).isEqualTo("La serie");
        assertThat(mapped.releaseYear()).isEqualTo(2022);
        assertThat(mapped.backdropUrl()).isNull();
    }
}

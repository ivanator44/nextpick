package com.nextpick.backend.catalog.mapper;

import com.nextpick.backend.catalog.dto.GenreDto;
import com.nextpick.backend.catalog.dto.ProviderDto;
import com.nextpick.backend.catalog.dto.TrailerDto;
import com.nextpick.backend.catalog.dto.WatchProvidersDto;
import com.nextpick.backend.catalog.tmdb.TmdbDtos;
import com.nextpick.backend.config.TmdbProperties;

import java.util.Comparator;
import java.util.List;

final class CatalogMappingSupport {
    private CatalogMappingSupport() {
    }

    static Integer year(String date) {
        if (date == null || date.length() < 4) {
            return null;
        }
        try {
            return Integer.valueOf(date.substring(0, 4));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    static String image(TmdbProperties properties, String size, String path) {
        return path == null || path.isBlank() ? null : properties.imageBaseUrl() + "/" + size + path;
    }

    static List<GenreDto> genres(List<TmdbDtos.Genre> genres) {
        return genres.stream().map(genre -> new GenreDto(genre.id(), genre.name())).toList();
    }

    static TrailerDto trailer(TmdbDtos.Videos videos) {
        return videos.results().stream()
                .filter(video -> "YouTube".equalsIgnoreCase(video.site()))
                .filter(video -> "Trailer".equalsIgnoreCase(video.type()) || "Teaser".equalsIgnoreCase(video.type()))
                .sorted(Comparator.comparing(TmdbDtos.Video::official).reversed()
                        .thenComparing(video -> "Trailer".equalsIgnoreCase(video.type()) ? 0 : 1))
                .findFirst()
                .map(video -> new TrailerDto(video.key(), video.name(),
                        "https://www.youtube-nocookie.com/embed/" + video.key()))
                .orElse(null);
    }

    static WatchProvidersDto providers(TmdbProperties properties, TmdbDtos.ProviderResponse response) {
        TmdbDtos.CountryProviders country = response.results().get(properties.region());
        if (country == null) {
            return WatchProvidersDto.empty(properties.region());
        }
        List<TmdbDtos.Provider> free = java.util.stream.Stream.concat(country.free().stream(), country.ads().stream())
                .distinct()
                .toList();
        return new WatchProvidersDto(properties.region(), country.link(),
                providers(properties, country.flatrate()), providers(properties, free),
                providers(properties, country.rent()), providers(properties, country.buy()));
    }

    private static List<ProviderDto> providers(TmdbProperties properties, List<TmdbDtos.Provider> providers) {
        return providers.stream()
                .sorted(Comparator.comparingInt(TmdbDtos.Provider::displayPriority))
                .map(provider -> new ProviderDto(provider.providerId(), provider.providerName(),
                        image(properties, "w92", provider.logoPath())))
                .distinct()
                .toList();
    }
}

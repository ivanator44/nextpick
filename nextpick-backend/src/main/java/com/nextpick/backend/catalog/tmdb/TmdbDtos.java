package com.nextpick.backend.catalog.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public final class TmdbDtos {
    private TmdbDtos() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Page(
            int page,
            List<Media> results,
            @JsonProperty("total_pages") int totalPages,
            @JsonProperty("total_results") long totalResults
    ) {
        public Page {
            results = results == null ? List.of() : results;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Media(
            long id,
            String title,
            String name,
            String overview,
            @JsonProperty("poster_path") String posterPath,
            @JsonProperty("backdrop_path") String backdropPath,
            @JsonProperty("vote_average") Double voteAverage,
            @JsonProperty("release_date") String releaseDate,
            @JsonProperty("first_air_date") String firstAirDate,
            @JsonProperty("genre_ids") List<Integer> genreIds,
            @JsonProperty("media_type") String mediaType
    ) {
        public Media {
            genreIds = genreIds == null ? List.of() : genreIds;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Detail(
            long id,
            String title,
            String name,
            String overview,
            @JsonProperty("poster_path") String posterPath,
            @JsonProperty("backdrop_path") String backdropPath,
            @JsonProperty("vote_average") Double voteAverage,
            @JsonProperty("release_date") String releaseDate,
            @JsonProperty("first_air_date") String firstAirDate,
            List<Genre> genres,
            Videos videos,
            Page recommendations
    ) {
        public Detail {
            genres = genres == null ? List.of() : genres;
            videos = videos == null ? new Videos(List.of()) : videos;
            recommendations = recommendations == null ? new Page(1, List.of(), 1, 0) : recommendations;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Genre(int id, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Genres(List<Genre> genres) {
        public Genres {
            genres = genres == null ? List.of() : genres;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Videos(List<Video> results) {
        public Videos {
            results = results == null ? List.of() : results;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Video(String key, String name, String site, String type, boolean official) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProviderResponse(Map<String, CountryProviders> results) {
        public ProviderResponse {
            results = results == null ? Map.of() : results;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CountryProviders(
            String link,
            List<Provider> flatrate,
            List<Provider> free,
            List<Provider> ads,
            List<Provider> rent,
            List<Provider> buy
    ) {
        public CountryProviders {
            flatrate = flatrate == null ? List.of() : flatrate;
            free = free == null ? List.of() : free;
            ads = ads == null ? List.of() : ads;
            rent = rent == null ? List.of() : rent;
            buy = buy == null ? List.of() : buy;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Provider(
            @JsonProperty("provider_id") int providerId,
            @JsonProperty("provider_name") String providerName,
            @JsonProperty("logo_path") String logoPath,
            @JsonProperty("display_priority") int displayPriority
    ) {
    }
}

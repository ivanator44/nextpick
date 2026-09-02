package com.nextpick.backend.catalog;

import com.nextpick.backend.catalog.dto.*;
import com.nextpick.backend.catalog.mapper.MovieCatalogMapper;
import com.nextpick.backend.catalog.mapper.TvCatalogMapper;
import com.nextpick.backend.catalog.tmdb.TmdbClient;
import com.nextpick.backend.catalog.tmdb.TmdbDtos;
import com.nextpick.backend.config.CacheConfig;
import com.nextpick.backend.config.TmdbProperties;
import com.nextpick.backend.entity.MediaType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

@Service
public class CatalogService {
    private static final int MAX_PAGE = 500;

    private final TmdbClient client;
    private final MovieCatalogMapper movieMapper;
    private final TvCatalogMapper tvMapper;
    private final TmdbProperties properties;

    public CatalogService(TmdbClient client, MovieCatalogMapper movieMapper,
                          TvCatalogMapper tvMapper, TmdbProperties properties) {
        this.client = client;
        this.movieMapper = movieMapper;
        this.tvMapper = tvMapper;
        this.properties = properties;
    }

    @Cacheable(cacheNames = CacheConfig.CATALOG_LISTS, key = "#root.target.cacheKey('trending', #page)", sync = true)
    public CatalogPage trending(int page) {
        TmdbDtos.Page response = client.getPage("/trending/all/week", Map.of("page", tmdbPage(page)));
        return page(response, media -> switch (media.mediaType() == null ? "" : media.mediaType()) {
            case "movie" -> movieMapper.summary(media);
            case "tv" -> tvMapper.summary(media);
            default -> null;
        });
    }

    @Cacheable(cacheNames = CacheConfig.CATALOG_LISTS, key = "#root.target.cacheKey('popular-' + #type, #page)", sync = true)
    public CatalogPage popular(MediaType type, int page) {
        return typedPage("/" + tmdbType(type) + "/popular", type, page, Map.of());
    }

    @Cacheable(cacheNames = CacheConfig.CATALOG_LISTS, key = "#root.target.cacheKey('top-' + #type, #page)", sync = true)
    public CatalogPage topRated(MediaType type, int page) {
        return typedPage("/" + tmdbType(type) + "/top_rated", type, page, Map.of());
    }

    @Cacheable(cacheNames = CacheConfig.CATALOG_LISTS,
            key = "#root.target.cacheKey('discover-' + #type + '-' + (#genreId == null ? 'all' : #genreId), #page)", sync = true)
    public CatalogPage discover(MediaType type, Integer genreId, int page) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("page", tmdbPage(page));
        params.put("sort_by", "popularity.desc");
        params.put("include_adult", false);
        params.put(type == MediaType.MOVIE ? "region" : "watch_region", properties.region());
        if (genreId != null) {
            params.put("with_genres", genreId);
        }
        TmdbDtos.Page response = client.getPage("/discover/" + tmdbType(type), params);
        return page(response, mapper(type));
    }

    @Cacheable(cacheNames = CacheConfig.CATALOG_SEARCH,
            key = "#root.target.cacheKey('search-' + #query.toLowerCase(), #page)", sync = true)
    public CatalogPage search(String query, int page) {
        String normalized = query == null ? "" : query.trim();
        if (normalized.isEmpty()) {
            return new CatalogPage(List.of(), 0, 0, 0, true);
        }
        TmdbDtos.Page response = client.getPage("/search/multi", Map.of(
                "query", normalized,
                "page", tmdbPage(page),
                "include_adult", false
        ));
        return page(response, media -> switch (media.mediaType() == null ? "" : media.mediaType()) {
            case "movie" -> movieMapper.summary(media);
            case "tv" -> tvMapper.summary(media);
            default -> null;
        });
    }

    @Cacheable(cacheNames = CacheConfig.CATALOG_DETAILS,
            key = "#root.target.cacheKey('detail-' + #type + '-' + #tmdbId, 0)", sync = true)
    public CatalogDetail detail(MediaType type, long tmdbId) {
        String path = "/" + tmdbType(type) + "/" + tmdbId;
        TmdbDtos.Detail detail = client.getDetail(path, Map.of("append_to_response", "videos,recommendations"));
        TmdbDtos.ProviderResponse providers = client.getProviders(path + "/watch/providers");
        return type == MediaType.MOVIE
                ? movieMapper.detail(detail, movieMapper.providers(providers))
                : tvMapper.detail(detail, tvMapper.providers(providers));
    }

    @Cacheable(cacheNames = CacheConfig.CATALOG_CONFIGURATION,
            key = "#root.target.cacheKey('genres-' + #type, 0)", sync = true)
    public List<GenreDto> genres(MediaType type) {
        return client.getGenres("/genre/" + tmdbType(type) + "/list").genres().stream()
                .map(genre -> new GenreDto(genre.id(), genre.name()))
                .toList();
    }

    public HomeCatalogDto home(List<CatalogSummary> forYou) {
        List<CatalogSummary> trending = trending(0).content().stream().limit(20).toList();
        List<CatalogSummary> popular = merge(popular(MediaType.MOVIE, 0).content(), popular(MediaType.SERIES, 0).content());
        List<CatalogSummary> topRated = merge(topRated(MediaType.MOVIE, 0).content(), topRated(MediaType.SERIES, 0).content());
        CatalogDetail hero = trending.stream().findFirst()
                .map(item -> detail(item.mediaType(), item.tmdbId()))
                .orElse(null);
        return new HomeCatalogDto(hero, trending, popular, topRated, forYou);
    }

    public String cacheKey(String operation, int page) {
        return operation + ":" + page + ":" + properties.language() + ":" + properties.region();
    }

    private CatalogPage typedPage(String path, MediaType type, int page, Map<String, ?> extra) {
        Map<String, Object> params = new LinkedHashMap<>(extra);
        params.put("page", tmdbPage(page));
        params.put("region", properties.region());
        TmdbDtos.Page response = client.getPage(path, params);
        return page(response, mapper(type));
    }

    private CatalogPage page(TmdbDtos.Page response, Function<TmdbDtos.Media, CatalogSummary> mapper) {
        List<CatalogSummary> content = response.results().stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .filter(item -> item.title() != null && !item.title().isBlank())
                .distinct()
                .toList();
        int zeroBasedPage = Math.max(0, response.page() - 1);
        return new CatalogPage(content, zeroBasedPage, response.totalPages(), response.totalResults(),
                response.page() >= response.totalPages());
    }

    private Function<TmdbDtos.Media, CatalogSummary> mapper(MediaType type) {
        return type == MediaType.MOVIE ? movieMapper::summary : tvMapper::summary;
    }

    private String tmdbType(MediaType type) {
        return type == MediaType.MOVIE ? "movie" : "tv";
    }

    private int tmdbPage(int zeroBasedPage) {
        return Math.min(MAX_PAGE, Math.max(0, zeroBasedPage) + 1);
    }

    private List<CatalogSummary> merge(List<CatalogSummary> first, List<CatalogSummary> second) {
        LinkedHashMap<String, CatalogSummary> merged = new LinkedHashMap<>();
        for (int index = 0; index < Math.max(first.size(), second.size()); index++) {
            if (index < first.size()) merged.put(first.get(index).identity(), first.get(index));
            if (index < second.size()) merged.put(second.get(index).identity(), second.get(index));
        }
        return merged.values().stream().limit(20).toList();
    }
}

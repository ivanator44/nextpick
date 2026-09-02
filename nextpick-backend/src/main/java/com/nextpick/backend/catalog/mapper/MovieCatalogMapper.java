package com.nextpick.backend.catalog.mapper;

import com.nextpick.backend.catalog.dto.CatalogDetail;
import com.nextpick.backend.catalog.dto.CatalogSummary;
import com.nextpick.backend.catalog.dto.WatchProvidersDto;
import com.nextpick.backend.catalog.tmdb.TmdbDtos;
import com.nextpick.backend.config.TmdbProperties;
import com.nextpick.backend.entity.MediaType;
import org.springframework.stereotype.Component;

@Component
public class MovieCatalogMapper {
    private final TmdbProperties properties;

    public MovieCatalogMapper(TmdbProperties properties) {
        this.properties = properties;
    }

    public CatalogSummary summary(TmdbDtos.Media media) {
        return new CatalogSummary(media.id(), MediaType.MOVIE, media.title(), media.overview(),
                CatalogMappingSupport.image(properties, "w500", media.posterPath()),
                CatalogMappingSupport.image(properties, "original", media.backdropPath()),
                media.voteAverage(), CatalogMappingSupport.year(media.releaseDate()), media.genreIds());
    }

    public CatalogDetail detail(TmdbDtos.Detail detail, WatchProvidersDto providers) {
        return new CatalogDetail(detail.id(), MediaType.MOVIE, detail.title(), detail.overview(),
                CatalogMappingSupport.image(properties, "w500", detail.posterPath()),
                CatalogMappingSupport.image(properties, "original", detail.backdropPath()),
                detail.voteAverage(), CatalogMappingSupport.year(detail.releaseDate()),
                CatalogMappingSupport.genres(detail.genres()), CatalogMappingSupport.trailer(detail.videos()), providers,
                detail.recommendations().results().stream().map(this::summary).limit(12).toList(), false);
    }

    public WatchProvidersDto providers(TmdbDtos.ProviderResponse response) {
        return CatalogMappingSupport.providers(properties, response);
    }
}

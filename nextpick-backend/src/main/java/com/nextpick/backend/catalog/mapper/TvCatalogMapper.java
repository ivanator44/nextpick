package com.nextpick.backend.catalog.mapper;

import com.nextpick.backend.catalog.dto.CatalogDetail;
import com.nextpick.backend.catalog.dto.CatalogSummary;
import com.nextpick.backend.catalog.dto.WatchProvidersDto;
import com.nextpick.backend.catalog.tmdb.TmdbDtos;
import com.nextpick.backend.config.TmdbProperties;
import com.nextpick.backend.entity.MediaType;
import org.springframework.stereotype.Component;

@Component
public class TvCatalogMapper {
    private final TmdbProperties properties;

    public TvCatalogMapper(TmdbProperties properties) {
        this.properties = properties;
    }

    public CatalogSummary summary(TmdbDtos.Media media) {
        return new CatalogSummary(media.id(), MediaType.SERIES, media.name(), media.overview(),
                CatalogMappingSupport.image(properties, "w500", media.posterPath()),
                CatalogMappingSupport.image(properties, "original", media.backdropPath()),
                media.voteAverage(), CatalogMappingSupport.year(media.firstAirDate()), media.genreIds());
    }

    public CatalogDetail detail(TmdbDtos.Detail detail, WatchProvidersDto providers) {
        return new CatalogDetail(detail.id(), MediaType.SERIES, detail.name(), detail.overview(),
                CatalogMappingSupport.image(properties, "w500", detail.posterPath()),
                CatalogMappingSupport.image(properties, "original", detail.backdropPath()),
                detail.voteAverage(), CatalogMappingSupport.year(detail.firstAirDate()),
                CatalogMappingSupport.genres(detail.genres()), CatalogMappingSupport.trailer(detail.videos()), providers,
                detail.recommendations().results().stream().map(this::summary).limit(12).toList(), false);
    }

    public WatchProvidersDto providers(TmdbDtos.ProviderResponse response) {
        return CatalogMappingSupport.providers(properties, response);
    }
}

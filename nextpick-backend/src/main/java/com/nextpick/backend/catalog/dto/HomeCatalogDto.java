package com.nextpick.backend.catalog.dto;

import java.util.List;

public record HomeCatalogDto(
        CatalogDetail hero,
        List<CatalogSummary> trending,
        List<CatalogSummary> popular,
        List<CatalogSummary> topRated,
        List<CatalogSummary> forYou
) {
}

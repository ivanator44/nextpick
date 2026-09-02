package com.nextpick.backend.catalog.dto;

import java.util.List;

public record CatalogPage(
        List<CatalogSummary> content,
        int page,
        int totalPages,
        long totalElements,
        boolean last
) {
}

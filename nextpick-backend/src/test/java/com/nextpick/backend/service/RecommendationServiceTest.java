package com.nextpick.backend.service;

import com.nextpick.backend.catalog.CatalogService;
import com.nextpick.backend.catalog.dto.CatalogPage;
import com.nextpick.backend.catalog.dto.CatalogSummary;
import com.nextpick.backend.entity.MediaType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RecommendationServiceTest {
    private final FavoriteService favorites = mock(FavoriteService.class);
    private final CatalogService catalog = mock(CatalogService.class);
    private final RecommendationService service = new RecommendationService(favorites, catalog);

    @Test
    void newUsersFallBackToTrending() {
        CatalogSummary trend = title(2, MediaType.SERIES, List.of(18));
        when(favorites.list(7)).thenReturn(List.of());
        when(catalog.trending(0)).thenReturn(page(List.of(trend)));

        assertThat(service.forUser(7)).containsExactly(trend);
        verify(catalog, never()).discover(any(), any(), anyInt());
    }

    @Test
    void usesFavoriteGenresAndExcludesFavoritesAndDuplicates() {
        CatalogSummary saved = title(1, MediaType.MOVIE, List.of(18));
        CatalogSummary candidate = title(2, MediaType.SERIES, List.of(18));
        when(favorites.list(7)).thenReturn(List.of(saved));
        when(catalog.discover(MediaType.MOVIE, 18, 0)).thenReturn(page(List.of(saved, candidate)));
        when(catalog.discover(MediaType.SERIES, 18, 0)).thenReturn(page(List.of(candidate)));
        when(catalog.trending(0)).thenReturn(page(List.of(saved, candidate)));

        assertThat(service.forUser(7)).containsExactly(candidate);
    }

    @Test
    void prioritizesRelatedTitlesFromFavoriteDetails() {
        CatalogSummary saved = title(1, MediaType.MOVIE, List.of(18));
        CatalogSummary related = title(3, MediaType.MOVIE, List.of(28));
        when(favorites.list(7)).thenReturn(List.of(saved));
        when(catalog.detail(MediaType.MOVIE, 1)).thenReturn(new com.nextpick.backend.catalog.dto.CatalogDetail(
                1, MediaType.MOVIE, "Favorite", null, null, null, 8.0, 2024, List.of(), null,
                com.nextpick.backend.catalog.dto.WatchProvidersDto.empty("ES"), List.of(related), false));
        when(catalog.discover(any(), eq(18), eq(0))).thenReturn(page(List.of()));
        when(catalog.trending(0)).thenReturn(page(List.of()));

        assertThat(service.forUser(7)).containsExactly(related);
    }

    private CatalogSummary title(long id, MediaType type, List<Integer> genres) {
        return new CatalogSummary(id, type, "Title " + id, null, null, null, 8.0, 2024, genres);
    }

    private CatalogPage page(List<CatalogSummary> content) {
        return new CatalogPage(content, 0, 1, content.size(), true);
    }
}

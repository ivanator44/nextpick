package com.nextpick.backend.service;

import com.nextpick.backend.catalog.CatalogService;
import com.nextpick.backend.catalog.dto.*;
import com.nextpick.backend.entity.MediaType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ChatGroundingServiceTest {
    @Test
    void boundsQueryAndHydratesVerifiedSpanishProviders() {
        CatalogService catalog = mock(CatalogService.class);
        CatalogSummary summary = new CatalogSummary(603, MediaType.MOVIE, "The Matrix", null,
                "/poster.jpg", null, 8.2, 1999, List.of(28));
        when(catalog.search(anyString(), eq(0))).thenReturn(new CatalogPage(List.of(summary), 0, 1, 1, true));
        when(catalog.detail(MediaType.MOVIE, 603)).thenReturn(new CatalogDetail(603, MediaType.MOVIE,
                "The Matrix", "Sinopsis", "/poster.jpg", null, 8.2, 1999,
                List.of(new GenreDto(28, "Ciencia ficción")), null,
                new WatchProvidersDto("ES", null, List.of(new ProviderDto(1, "Max", null)),
                        List.of(), List.of(), List.of()), List.of(), false));

        List<ChatAiService.GroundedTitle> result = new ChatGroundingService(catalog)
                .ground("Matrix\u0000 " + "x".repeat(140));

        ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
        verify(catalog).search(query.capture(), eq(0));
        assertThat(query.getValue()).hasSize(100).doesNotContain("\u0000");
        assertThat(result).singleElement().satisfies(title -> {
            assertThat(title.title()).isEqualTo("The Matrix");
            assertThat(title.streamingProviders()).containsExactly("Max");
            assertThat(title.posterUrl()).isEqualTo("/poster.jpg");
        });
    }
}

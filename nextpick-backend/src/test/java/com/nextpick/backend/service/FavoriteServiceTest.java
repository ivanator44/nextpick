package com.nextpick.backend.service;

import com.nextpick.backend.catalog.CatalogService;
import com.nextpick.backend.catalog.dto.*;
import com.nextpick.backend.entity.Favorite;
import com.nextpick.backend.entity.MediaType;
import com.nextpick.backend.entity.User;
import com.nextpick.backend.exception.ConflictException;
import com.nextpick.backend.repository.FavoriteRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FavoriteServiceTest {
    private final FavoriteRepository repository = mock(FavoriteRepository.class);
    private final CatalogService catalog = mock(CatalogService.class);
    private final FavoriteService service = new FavoriteService(repository, catalog);

    @Test
    void hydratesCompositeFavoriteFromCatalog() {
        User user = User.builder().id(3L).build();
        when(repository.existsByUserIdAndTmdbIdAndMediaType(3L, 42L, MediaType.SERIES)).thenReturn(false);
        when(catalog.detail(MediaType.SERIES, 42L)).thenReturn(new CatalogDetail(42, MediaType.SERIES, "Serie",
                "Resumen", "/poster", "/backdrop", 8.0, 2024, List.of(new GenreDto(18, "Drama")),
                null, WatchProvidersDto.empty("ES"), List.of(), false));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CatalogSummary saved = service.add(user, 42, MediaType.SERIES);

        assertThat(saved.identity()).isEqualTo("SERIES:42");
        verify(repository).save(argThat(favorite -> favorite.getTmdbId() == 42
                && favorite.getMediaType() == MediaType.SERIES
                && "18".equals(favorite.getSnapshotGenreIds())));
    }

    @Test
    void preventsDuplicateForSameUserAndCompositeIdentity() {
        User user = User.builder().id(3L).build();
        when(repository.existsByUserIdAndTmdbIdAndMediaType(3L, 42L, MediaType.MOVIE)).thenReturn(true);

        assertThatThrownBy(() -> service.add(user, 42, MediaType.MOVIE)).isInstanceOf(ConflictException.class);
        verifyNoInteractions(catalog);
    }

    @Test
    void listsSnapshotsWithoutExternalCalls() {
        Favorite favorite = Favorite.builder().tmdbId(9L).mediaType(MediaType.MOVIE).snapshotTitle("Film")
                .snapshotGenreIds("28,12").build();
        when(repository.findByUserIdAndTmdbIdIsNotNullOrderByAddedAtDesc(1L)).thenReturn(List.of(favorite));

        assertThat(service.list(1L)).extracting(CatalogSummary::title).containsExactly("Film");
        verifyNoInteractions(catalog);
    }
}

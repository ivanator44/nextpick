package com.nextpick.backend.service;

import com.nextpick.backend.catalog.CatalogService;
import com.nextpick.backend.catalog.dto.CatalogDetail;
import com.nextpick.backend.catalog.dto.CatalogSummary;
import com.nextpick.backend.config.CacheConfig;
import com.nextpick.backend.entity.Favorite;
import com.nextpick.backend.entity.MediaType;
import com.nextpick.backend.entity.User;
import com.nextpick.backend.exception.ConflictException;
import com.nextpick.backend.repository.FavoriteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Service
public class FavoriteService {
    private final FavoriteRepository repository;
    private final CatalogService catalogService;

    public FavoriteService(FavoriteRepository repository, CatalogService catalogService) {
        this.repository = repository;
        this.catalogService = catalogService;
    }

    @Transactional(readOnly = true)
    public List<CatalogSummary> list(long userId) {
        return repository.findByUserIdAndTmdbIdIsNotNullOrderByAddedAtDesc(userId).stream()
                .map(this::snapshot)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(long userId, long tmdbId, MediaType mediaType) {
        return repository.existsByUserIdAndTmdbIdAndMediaType(userId, tmdbId, mediaType);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.RECOMMENDATIONS, key = "#user.id")
    public CatalogSummary add(User user, long tmdbId, MediaType mediaType) {
        if (exists(user.getId(), tmdbId, mediaType)) {
            throw new ConflictException("El título ya está en favoritos");
        }
        CatalogDetail detail = catalogService.detail(mediaType, tmdbId);
        Favorite favorite = Favorite.builder()
                .user(user)
                .tmdbId(tmdbId)
                .mediaType(mediaType)
                .snapshotTitle(detail.title())
                .snapshotPosterUrl(detail.posterUrl())
                .snapshotReleaseYear(detail.releaseYear())
                .snapshotRating(detail.rating())
                .snapshotGenreIds(detail.genres().stream().map(genre -> String.valueOf(genre.id()))
                        .collect(java.util.stream.Collectors.joining(",")))
                .snapshotUpdatedAt(Instant.now())
                .build();
        return snapshot(repository.save(favorite));
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.RECOMMENDATIONS, key = "#userId")
    public void remove(long userId, long tmdbId, MediaType mediaType) {
        if (repository.deleteByUserIdAndTmdbIdAndMediaType(userId, tmdbId, mediaType) == 0) {
            throw new EntityNotFoundException("El título no estaba en favoritos");
        }
    }

    private CatalogSummary snapshot(Favorite favorite) {
        List<Integer> genreIds = favorite.getSnapshotGenreIds() == null || favorite.getSnapshotGenreIds().isBlank()
                ? List.of()
                : Arrays.stream(favorite.getSnapshotGenreIds().split(","))
                    .map(Integer::valueOf)
                    .toList();
        return new CatalogSummary(favorite.getTmdbId(), favorite.getMediaType(), favorite.getSnapshotTitle(), null,
                favorite.getSnapshotPosterUrl(), null, favorite.getSnapshotRating(),
                favorite.getSnapshotReleaseYear(), genreIds);
    }
}

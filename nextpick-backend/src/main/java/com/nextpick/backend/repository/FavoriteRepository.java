package com.nextpick.backend.repository;

import com.nextpick.backend.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nextpick.backend.entity.MediaType;
import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserId(Long userId);
    Optional<Favorite> findByUserIdAndMovieId(Long userId, Long movieId);
    boolean existsByUserIdAndMovieId(Long userId, Long movieId);
    long deleteByUserIdAndMovieId(Long userId, Long movieId);

    List<Favorite> findByUserIdAndTmdbIdIsNotNullOrderByAddedAtDesc(Long userId);
    Optional<Favorite> findByUserIdAndTmdbIdAndMediaType(Long userId, Long tmdbId, MediaType mediaType);
    boolean existsByUserIdAndTmdbIdAndMediaType(Long userId, Long tmdbId, MediaType mediaType);
    long deleteByUserIdAndTmdbIdAndMediaType(Long userId, Long tmdbId, MediaType mediaType);
}

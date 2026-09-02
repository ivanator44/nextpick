package com.nextpick.backend.service;

import com.nextpick.backend.dto.movie.MovieDetailDto;
import com.nextpick.backend.dto.movie.MovieSummaryDto;
import com.nextpick.backend.entity.MediaType;
import com.nextpick.backend.entity.Movie;
import com.nextpick.backend.repository.FavoriteRepository;
import com.nextpick.backend.repository.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;

@Service
@Profile("legacy")
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final FavoriteRepository favoriteRepository;

    public Page<MovieSummaryDto> findByType(MediaType type, Pageable pageable) {
        return movieRepository.findByMediaType(type, pageable).map(MovieSummaryDto::fromEntity);
    }

    public Page<MovieSummaryDto> search(String query, Pageable pageable) {
        return movieRepository.findByTitleContainingIgnoreCase(query, pageable).map(MovieSummaryDto::fromEntity);
    }

    public Page<MovieSummaryDto> findByTag(String tag, Pageable pageable) {
        return movieRepository.findByTagsContainingIgnoreCase(tag, pageable).map(MovieSummaryDto::fromEntity);
    }

    public Page<MovieSummaryDto> findByGenre(String genre, Pageable pageable) {
        return movieRepository.findByGenres_NameIgnoreCase(genre, pageable).map(MovieSummaryDto::fromEntity);
    }

    public MovieDetailDto findById(Long id, Long userId) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Título no encontrado: " + id));

        boolean isFavorite = userId != null
                && favoriteRepository.existsByUserIdAndMovieId(userId, id);

        return MovieDetailDto.fromEntity(movie, isFavorite);
    }
}

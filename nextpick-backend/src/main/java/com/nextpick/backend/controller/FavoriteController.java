package com.nextpick.backend.controller;

import com.nextpick.backend.dto.movie.MovieSummaryDto;
import com.nextpick.backend.entity.Favorite;
import com.nextpick.backend.entity.Movie;
import com.nextpick.backend.entity.User;
import com.nextpick.backend.repository.FavoriteRepository;
import com.nextpick.backend.repository.MovieRepository;
import com.nextpick.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteRepository favoriteRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<MovieSummaryDto> getFavorites(Authentication authentication) {
        User user = currentUser(authentication);
        return favoriteRepository.findByUserId(user.getId()).stream()
                .map(fav -> MovieSummaryDto.fromEntity(fav.getMovie()))
                .toList();
    }

    @PostMapping("/{movieId}")
    public ResponseEntity<Void> addFavorite(@PathVariable Long movieId, Authentication authentication) {
        User user = currentUser(authentication);

        if (favoriteRepository.existsByUserIdAndMovieId(user.getId(), movieId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("Título no encontrado: " + movieId));

        favoriteRepository.save(Favorite.builder().user(user).movie(movie).build());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long movieId, Authentication authentication) {
        User user = currentUser(authentication);
        favoriteRepository.deleteByUserIdAndMovieId(user.getId(), movieId);
        return ResponseEntity.noContent().build();
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
    }
}

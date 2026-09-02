package com.nextpick.backend.controller;

import com.nextpick.backend.dto.movie.MovieDetailDto;
import com.nextpick.backend.dto.movie.MovieSummaryDto;
import com.nextpick.backend.entity.MediaType;
import com.nextpick.backend.entity.User;
import com.nextpick.backend.repository.UserRepository;
import com.nextpick.backend.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final UserRepository userRepository;

    @GetMapping("/movies-only")
    public Page<MovieSummaryDto> getMovies(Pageable pageable) {
        return movieService.findByType(MediaType.MOVIE, pageable);
    }

    @GetMapping("/series-only")
    public Page<MovieSummaryDto> getSeries(Pageable pageable) {
        return movieService.findByType(MediaType.SERIES, pageable);
    }

    @GetMapping("/search")
    public Page<MovieSummaryDto> search(@RequestParam String q, Pageable pageable) {
        return movieService.search(q, pageable);
    }

    @GetMapping("/tag/{tag}")
    public Page<MovieSummaryDto> byTag(@PathVariable String tag, Pageable pageable) {
        return movieService.findByTag(tag, pageable);
    }

    @GetMapping("/genre/{genre}")
    public Page<MovieSummaryDto> byGenre(@PathVariable String genre, Pageable pageable) {
        return movieService.findByGenre(genre, pageable);
    }

    @GetMapping("/{id}")
    public MovieDetailDto getDetail(@PathVariable Long id, Authentication authentication) {
        Long userId = resolveUserId(authentication);
        return movieService.findById(id, userId);
    }

    // Si hay un usuario autenticado, resolvemos su id para marcar isFavorite en el detalle
    private Long resolveUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        String email = authentication.getName();
        return userRepository.findByEmail(email).map(User::getId).orElse(null);
    }
}

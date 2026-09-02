package com.nextpick.backend.controller;

import com.nextpick.backend.catalog.CatalogService;
import com.nextpick.backend.catalog.dto.*;
import com.nextpick.backend.entity.MediaType;
import com.nextpick.backend.entity.User;
import com.nextpick.backend.repository.UserRepository;
import com.nextpick.backend.service.FavoriteService;
import com.nextpick.backend.service.RecommendationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Validated
@RestController
@RequestMapping("/api/catalog")
public class CatalogController {
    private final CatalogService catalogService;
    private final FavoriteService favoriteService;
    private final RecommendationService recommendationService;
    private final UserRepository userRepository;

    public CatalogController(CatalogService catalogService, FavoriteService favoriteService,
                             RecommendationService recommendationService, UserRepository userRepository) {
        this.catalogService = catalogService;
        this.favoriteService = favoriteService;
        this.recommendationService = recommendationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/home")
    public HomeCatalogDto home(Authentication authentication) {
        List<CatalogSummary> forYou = currentUser(authentication)
                .map(user -> recommendationService.forUser(user.getId()))
                .orElseGet(() -> catalogService.trending(0).content().stream().limit(20).toList());
        return catalogService.home(forYou);
    }

    @GetMapping("/trending")
    public CatalogPage trending(@RequestParam(defaultValue = "0") @Min(0) @Max(499) int page) {
        return catalogService.trending(page);
    }

    @GetMapping("/popular/{mediaType}")
    public CatalogPage popular(@PathVariable MediaType mediaType,
                               @RequestParam(defaultValue = "0") @Min(0) @Max(499) int page) {
        return catalogService.popular(mediaType, page);
    }

    @GetMapping("/top-rated/{mediaType}")
    public CatalogPage topRated(@PathVariable MediaType mediaType,
                                @RequestParam(defaultValue = "0") @Min(0) @Max(499) int page) {
        return catalogService.topRated(mediaType, page);
    }

    @GetMapping("/discover/{mediaType}")
    public CatalogPage discover(@PathVariable MediaType mediaType,
                                @RequestParam(required = false) @Min(1) Integer genreId,
                                @RequestParam(defaultValue = "0") @Min(0) @Max(499) int page) {
        return catalogService.discover(mediaType, genreId, page);
    }

    @GetMapping("/search")
    public CatalogPage search(@RequestParam @Size(min = 1, max = 100) String query,
                              @RequestParam(defaultValue = "0") @Min(0) @Max(499) int page) {
        return catalogService.search(query, page);
    }

    @GetMapping("/{mediaType}/{tmdbId}")
    public CatalogDetail detail(@PathVariable MediaType mediaType,
                                @PathVariable @Min(1) long tmdbId,
                                Authentication authentication) {
        CatalogDetail detail = catalogService.detail(mediaType, tmdbId);
        boolean favorite = currentUser(authentication)
                .map(user -> favoriteService.exists(user.getId(), tmdbId, mediaType))
                .orElse(false);
        return detail.withFavorite(favorite);
    }

    @GetMapping("/genres/{mediaType}")
    public List<GenreDto> genres(@PathVariable MediaType mediaType) {
        return catalogService.genres(mediaType);
    }

    @GetMapping("/recommendations")
    public List<CatalogSummary> recommendations(Authentication authentication) {
        User user = currentUser(authentication)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Autenticación necesaria"));
        return recommendationService.forUser(user.getId());
    }

    private Optional<User> currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        return userRepository.findByEmail(authentication.getName());
    }
}

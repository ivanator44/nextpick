package com.nextpick.backend.controller;

import com.nextpick.backend.catalog.dto.CatalogSummary;
import com.nextpick.backend.entity.MediaType;
import com.nextpick.backend.entity.User;
import com.nextpick.backend.repository.UserRepository;
import com.nextpick.backend.service.FavoriteService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/favorites")
public class FavoriteApiController {
    private final FavoriteService favoriteService;
    private final UserRepository userRepository;

    public FavoriteApiController(FavoriteService favoriteService, UserRepository userRepository) {
        this.favoriteService = favoriteService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<CatalogSummary> list(Authentication authentication) {
        return favoriteService.list(currentUser(authentication).getId());
    }

    @PostMapping("/{mediaType}/{tmdbId}")
    public ResponseEntity<CatalogSummary> add(@PathVariable MediaType mediaType,
                                               @PathVariable @Min(1) long tmdbId,
                                               Authentication authentication) {
        CatalogSummary favorite = favoriteService.add(currentUser(authentication), tmdbId, mediaType);
        return ResponseEntity.status(201).body(favorite);
    }

    @DeleteMapping("/{mediaType}/{tmdbId}")
    public ResponseEntity<Void> remove(@PathVariable MediaType mediaType,
                                       @PathVariable @Min(1) long tmdbId,
                                       Authentication authentication) {
        favoriteService.remove(currentUser(authentication).getId(), tmdbId, mediaType);
        return ResponseEntity.noContent().build();
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
    }
}

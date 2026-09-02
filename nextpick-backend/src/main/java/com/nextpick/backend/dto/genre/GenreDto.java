package com.nextpick.backend.dto.genre;

import com.nextpick.backend.entity.Genre;

public record GenreDto(Long id, String name) {
    public static GenreDto fromEntity(Genre genre) {
        return new GenreDto(genre.getId(), genre.getName());
    }
}

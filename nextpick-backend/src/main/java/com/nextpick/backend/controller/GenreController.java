package com.nextpick.backend.controller;

import com.nextpick.backend.dto.genre.GenreDto;
import com.nextpick.backend.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
public class GenreController {

    private final GenreRepository genreRepository;

    @GetMapping
    public List<GenreDto> getGenres() {
        return genreRepository.findAll().stream().map(GenreDto::fromEntity).toList();
    }
}

package com.nextpick.backend.repository;

import com.nextpick.backend.entity.MediaType;
import com.nextpick.backend.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    Page<Movie> findByMediaType(MediaType mediaType, Pageable pageable);

    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Movie> findByTagsContainingIgnoreCase(String tag, Pageable pageable);

    Page<Movie> findByGenres_NameIgnoreCase(String genreName, Pageable pageable);
}

package com.nextpick.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "legacy_movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    private String synopsis;

    private Integer releaseYear;

    @Column(precision = 3)
    private Double rating; // 0.0 - 10.0

    private String posterUrl;
    private String backdropUrl;
    private String trailerUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MediaType mediaType;

    // Para carruseles tipo "Tendencias", "Top 10", "Animes"...
    @ElementCollection
    @CollectionTable(name = "legacy_movie_tags", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "tag")
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "legacy_movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();
}

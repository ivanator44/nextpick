package com.nextpick.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "favorites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "legacy_movie_id")
    private Long legacyMovieId;

    /** Read-only bridge for pre-TMDB rows; new writes use tmdbId/mediaType. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "legacy_movie_id", insertable = false, updatable = false)
    private Movie movie;

    @Column(name = "tmdb_id")
    private Long tmdbId;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", length = 20)
    private MediaType mediaType;

    @Column(name = "snapshot_title", length = 200)
    private String snapshotTitle;

    @Column(name = "snapshot_poster_url", length = 500)
    private String snapshotPosterUrl;

    private Integer snapshotReleaseYear;
    private Double snapshotRating;

    @Column(name = "snapshot_genre_ids", length = 500)
    private String snapshotGenreIds;

    private Instant snapshotUpdatedAt;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private Instant addedAt = Instant.now();
}

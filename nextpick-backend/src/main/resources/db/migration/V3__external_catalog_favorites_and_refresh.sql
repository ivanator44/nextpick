-- TMDB becomes the catalog source of truth. The old seeded catalog is retained
-- only as legacy data and is no longer exposed by the application.
ALTER TABLE movies RENAME TO legacy_movies;
ALTER TABLE genres RENAME TO legacy_genres;
ALTER TABLE movie_tags RENAME TO legacy_movie_tags;
ALTER TABLE movie_genres RENAME TO legacy_movie_genres;
ALTER TABLE favorites RENAME COLUMN movie_id TO legacy_movie_id;
ALTER TABLE favorites ALTER COLUMN legacy_movie_id DROP NOT NULL;

ALTER TABLE favorites
    ADD COLUMN tmdb_id BIGINT,
    ADD COLUMN media_type VARCHAR(20),
    ADD COLUMN snapshot_title VARCHAR(200),
    ADD COLUMN snapshot_poster_url VARCHAR(500),
    ADD COLUMN snapshot_release_year INT,
    ADD COLUMN snapshot_rating DOUBLE PRECISION,
    ADD COLUMN snapshot_genre_ids VARCHAR(500),
    ADD COLUMN snapshot_updated_at TIMESTAMP;

ALTER TABLE favorites DROP CONSTRAINT IF EXISTS favorites_user_id_movie_id_key;
CREATE UNIQUE INDEX uq_favorites_user_tmdb_media
    ON favorites(user_id, tmdb_id, media_type)
    WHERE tmdb_id IS NOT NULL;
CREATE INDEX idx_favorites_identity ON favorites(tmdb_id, media_type);

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);

CREATE TABLE user_preferences (
    user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    preferred_genre_ids VARCHAR(500),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

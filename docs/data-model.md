# Modelo de datos

```mermaid
erDiagram
  USERS ||--o{ FAVORITES : guarda
  USERS ||--o{ REFRESH_TOKENS : mantiene
  USERS ||--o| USER_PREFERENCES : configura
  USERS ||--o{ CHAT_CONVERSATIONS : posee
  CHAT_CONVERSATIONS ||--o{ CHAT_MESSAGES : contiene

  USERS { bigint id PK
    varchar email UK
    varchar password
    varchar role
  }
  FAVORITES { bigint id PK
    bigint user_id FK
    bigint tmdb_id
    varchar media_type
    timestamp added_at
    varchar snapshot_title
    varchar snapshot_poster_url
    int snapshot_release_year
    double snapshot_rating
    varchar snapshot_genre_ids
  }
  REFRESH_TOKENS { bigint id PK
    bigint user_id FK
    varchar token_hash UK
    timestamp expires_at
    timestamp revoked_at
  }
  USER_PREFERENCES { bigint user_id PK_FK
    varchar preferred_genre_ids
  }
  CHAT_CONVERSATIONS { bigint id PK
    bigint user_id FK
    varchar title
    timestamp updated_at
  }
  CHAT_MESSAGES { bigint id PK
    bigint conversation_id FK
    varchar sender
    text content
  }
```

La identidad de catálogo es `(tmdb_id, media_type)`, donde el código conserva `MOVIE` y `SERIES`. El índice único parcial de favoritos añade `user_id`. Los campos `legacy_*` conservan filas de V1/V2 sin inventar una identidad TMDB y no se exponen en la API nueva.

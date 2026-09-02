# NextPick backend

API Java 21/Spring Boot 3.3.4. Requiere `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` y `TMDB_ACCESS_TOKEN`; acepta las demás variables descritas en `../.env.example`.

```powershell
.\mvnw.cmd verify
.\mvnw.cmd spring-boot:run
```

Flyway aplica V1, V2 y V3 en orden. V3 conserva el catálogo sembrado como tablas `legacy_*`, añade la identidad TMDB compuesta, snapshots de favoritos, refresh tokens rotables y preferencias. No edites migraciones ya aplicadas.

Rutas principales: `/api/catalog`, `/api/favorites`, `/api/auth`, `/api/chat`; contrato interactivo en `/swagger-ui.html`. El token TMDB se usa exclusivamente en `TmdbClient` y nunca se devuelve al cliente.

Para una demo sin proveedor IA usa deliberadamente `SPRING_PROFILES_ACTIVE=mock`. El perfil normal no simula respuestas.

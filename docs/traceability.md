# Trazabilidad

| Requisito | Caso de uso | Implementación | Prueba/evidencia |
|---|---|---|---|
| RF-01, RNF-03 | CU-03 | `AuthController`, `AuthService`, `JwtUtil`, interceptores Angular | `AuthServiceTest`, `error.interceptor.spec.ts` |
| RF-02 | CU-01 | `CatalogController.home`, `CatalogService.home`, `HomeComponent` | mapper/cache tests, build |
| RF-03 | CU-01 | `/discover/{mediaType}`, Movies/Series | `MovieService` spec |
| RF-04 | CU-02 | `/catalog/search`, `SearchComponent` | `MovieService` spec, E2E |
| RF-05 | CU-02 | mappers, detalle/proveedores, `MovieModalComponent` | mapper/client tests, E2E |
| RF-06 | CU-04 | V3, `FavoriteService`, `FavoriteApiController`, Signal Angular | backend/frontend favorite tests, E2E |
| RF-07 | CU-04 | `RecommendationService` y eviction de favoritos | `RecommendationServiceTest` |
| RF-08 | CU-05 | `GeminiChatAiService`/`OpenAiChatAiService`, `ChatGroundingService`, `ChatController`, parser SSE y componentes chat | Gemini/OpenAI provider tests, grounding/ownership tests, parser specs, E2E mock |
| RNF-01 | CU-01/02 | `TmdbClient`, variables backend, ausencia de TMDB en Angular | client test y búsqueda de secretos |
| RNF-02 | todos | idioma/región, focus/Escape, estados, reduced-motion | lint/build; revisión visual pendiente |
| RNF-04 | CU-01/02/05 | Caffeine, timeouts, reintentos acotados y `Retry-After` | `CatalogCacheTest`, `TmdbClientTest`, `OpenAiChatAiServiceTest` |
| RNF-05 | todos | validación y `GlobalExceptionHandler` | backend tests parciales |
| RNF-06/07 | todos | fixtures, `.env.example`, Wrapper/lockfile | comandos en `testing.md` |

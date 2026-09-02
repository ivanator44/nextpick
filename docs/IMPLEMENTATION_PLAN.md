# NextPick MVP implementation plan

Status legend: `[ ]` pending, `[~]` in progress, `[x]` verified.

## Baseline audit

- [x] Inventory backend and frontend files, endpoints, entities, migrations and configuration.
- [x] Confirm repository state: the delivered workspace has no `.git` metadata.
- [x] Baseline frontend production build passes (Angular 18.2).
- [x] Locate usable Maven distribution bundled with IntelliJ; system `PATH` has no `mvn`.
- [x] Identify catalog seed (`V2__seed_data.sql`) and local `movies` table as the current source.
- [x] Identify embedded OpenAI and JWT secrets, fixed CORS and incomplete refresh/logout.
- [x] Identify deterministic production chat mock and fragile line-based SSE parser.
- [x] Identify placeholders/inactive controls: “Para ti”, Settings, chat profile, trailer action and `alert()`.

## Phase 1 — Configuration, contract and TMDB catalog

Files/areas: backend `pom.xml`, `application*.yml`, `config/`, `catalog/`, `controller/`, `.env.example`.

- [x] Remove embedded secrets and experimental Spring AI milestone dependency.
- [x] Add validated typed configuration for TMDB, AI, CORS, JWT and timeouts.
- [x] Implement typed `TmdbClient`, raw TMDB DTOs, movie/TV mappers and resilient error mapping.
- [x] Implement `/api/catalog` home, lists, discover, unified search, genres, detail, provider and related-title contracts.
- [x] Add Caffeine caches with configuration-specific keys and no caching of errors/nulls.
- [x] Add Actuator health and OpenAPI documentation.

Risks: TMDB quota/429 handling; movie/TV field differences; provider data may be absent in ES; no live credential is used in automated tests.

Verification: mapper fixtures, simulated HTTP server client tests, cache call-count test, controller tests.

## Phase 2 — Persistence, favorites and recommendations

Files/areas: new Flyway migration, `entity/`, `repository/`, `service/FavoriteService`, controllers.

- [x] Preserve applied V1/V2 migrations and move their catalog tables to a clearly legacy namespace via V3; verified on PostgreSQL from both a clean schema and an existing V1/V2 schema with legacy data.
- [x] Store favorites by unique `(user_id, tmdb_id, media_type)` with a minimal hydrated snapshot.
- [x] Avoid N+1 external requests when listing favorites.
- [x] Build content-based “Para ti” from favorite genres/related titles, exclude favorites/duplicates and fall back to trends.
- [x] Invalidate recommendation cache when favorites change.

Risks: existing legacy favorites have no trustworthy TMDB identity and must be preserved but excluded rather than fabricated.

Verification: clean/existing-schema migration reasoning, repository uniqueness and user-isolation tests, recommendation service tests.

## Phase 3 — Authentication and API safety

Files/areas: auth DTOs/controllers/services, JWT filter, security config, exception contract, Angular auth/interceptors.

- [x] Use rotated, revocable refresh tokens in an HttpOnly SameSite cookie; keep short-lived access tokens client-side.
- [x] Implement register, login, single-flight refresh, retry-original-request and logout.
- [x] Ensure malformed/expired tokens return consistent 401 without masking upstream TMDB/AI errors.
- [x] Add request length validation and consistent 400/401/403/404/409/429/500 JSON errors.
- [x] Externalize CORS and cookie security settings.

Risks: local frontend/backend use different ports but are same-site; production HTTPS requires `Secure=true`.

Verification: auth service/controller/security tests and frontend interceptor concurrency tests.

## Phase 4 — Real grounded AI chat

Files/areas: AI provider interface/client, chat service/controller/entities, SSE protocol, Angular chat service/components.

- [x] Replace the production mock with selectable Google AI Studio (`streamGenerateContent`) and OpenAI Responses (`store:false`) integrations.
- [x] Keep deterministic mock only under the `mock`/test profile.
- [x] Ground each turn with at most four verified `CatalogService` results and emit structured title references.
- [x] Limit history, enforce entertainment-only system instructions and validate message size.
- [x] Stream named JSON SSE events, handle cancellation/timeouts, and persist only complete non-empty replies.
- [x] Classify provider failures and expose only controlled generic messages; raw Google/OpenAI error text never reaches the interface.
- [x] Centralize public AI errors, sanitize the disabled fallback and cover Google/OpenAI/internal-configuration leakage with deterministic tests.
- [x] Add reactive conversation search/profile controls; rename/delete are intentionally not exposed by the MVP interface.

Risks: external provider quotas and model deprecations must be handled explicitly; disconnect detection differs by servlet container. Recovered messages do not persist their structured reference cards.

Verification: provider parser fixture, stream success/error/cancellation tests, conversation ownership/controller tests.

## Phase 5 — Angular product completion

Files/areas: catalog/auth/favorite/chat services and models; home/search/list/detail/shared components; global styles.

- [x] Consume one aggregated Home endpoint and display real TMDB backdrops/trailers.
- [x] Keep movie and TV discovery type-safe; make global search unified.
- [x] Show detail, genres, year, rating, type, all ES provider modes, trailer and related titles.
- [x] Make favorite state immediate and composite-keyed across all views.
- [x] Add loading/error/empty/retry states, skeletons, image fallbacks and accessible toast/dialog behavior.
- [x] Fix SSE chunk parsing, cancellation, message state ownership and reactive conversation search.
- [x] Remove inactive controls; add TMDB/JustWatch credits.
- [x] Add keyboard/focus/Escape behavior, responsive layouts and reduced-motion rules; review screenshots at 390/768/1440 px and prevent horizontal overflow.

Verification: unit tests for services/parser/interceptor/components and production build.

## Phase 6 — Reproducibility, tests and TFG documentation

Files/areas: Maven wrapper, npm scripts/lockfile, test sources, Playwright, Docker Compose, README and `docs/`.

- [x] Add deterministic controller/security/repository/migration and Angular component tests. The Testcontainers migration test is skipped automatically when Docker is unavailable; both migration paths were also verified against PostgreSQL 17.
- [x] Add Maven Wrapper and reproducible npm scripts/lockfile.
- [x] Add PostgreSQL Docker Compose, development/production config and executable quickstart.
- [x] Create and synchronize requirements, use cases, architecture, data model, ADR, testing, traceability, user manual and evidence checklist.
- [x] Run backend `test`/`verify`, frontend test/lint/build and E2E where tooling permits.
- [x] Review changed-file inventory for secrets, dead code, TODO/FIXME, inactive controls and regressions; remaining blockers are recorded in `docs/testing.md`.
- [x] Make direct IntelliJ/Spring Boot startup load an optional repository `.env` and document the exact local PostgreSQL/TMDB setup.

### Final verification and user actions

- [x] Repeat backend `verify`, frontend tests/lint/build and mocked E2E after adding the deferred tests.
- [x] Validate the repository `.env`, connect to the user's PostgreSQL database and complete a live catalog acceptance with the configured `TMDB_ACCESS_TOKEN` (Actuator `UP`, Flyway V3 and 20 trending results).
- [x] Switch the configured provider to Google AI Studio, replace the retired `gemini-1.5-flash` model with `gemini-3.6-flash`, verify a real HTTP 200 generation and start Spring Boot with the Gemini `ChatAiService` bean.

## Completion gate

All required MVP criteria are implemented and verified. Google AI Studio is the active live provider; OpenAI remains an optional alternative whose configured organization has no API credits. Deterministic provider, streaming, grounding, cancellation and provider-error tests cover both code paths. Test results in `docs/testing.md` reflect commands actually run.

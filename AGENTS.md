# NextPick workspace guide

## Structure

- `nextpick-backend/`: Java 21, Spring Boot 3.3, Security/JWT, JPA, Flyway, PostgreSQL.
- `nextpick-frontend/`: Angular 18 standalone components, Signals, Router and SCSS.
- `docs/`: implementation plan and TFG evidence based on the real code.

## Commands

- Backend tests: `cd nextpick-backend && ./mvnw test` (Windows: `.\mvnw.cmd test`).
- Backend verification: `cd nextpick-backend && ./mvnw verify`.
- Backend run: `cd nextpick-backend && ./mvnw spring-boot:run`.
- Frontend install: `cd nextpick-frontend && npm ci`.
- Frontend test: `npm test -- --watch=false`.
- Frontend build: `npm run build -- --configuration production`.
- E2E: `npm run e2e`.

## Conventions

- Keep TMDB and AI credentials only in backend environment variables.
- Internal media types are `MOVIE` and `SERIES`; identities use `(tmdbId, mediaType)`.
- Angular never calls TMDB or the AI provider directly.
- Do not edit applied Flyway migrations; add a new versioned migration.
- Preserve Spanish UI copy, default `es-ES` language and `ES` region.
- Prefer typed DTOs, constructor injection, Signals for view state and cancellable RxJS flows.
- Tests must use deterministic fixtures; never consume real TMDB or LLM quotas.

## Definition of done

- No production mock, embedded secret, inactive visible control, `TODO` or `FIXME`.
- Catalog, detail, providers, favorites, recommendations, auth refresh and chat history work end to end.
- Backend `verify`, frontend tests/build and the mocked E2E pass, or the exact external blocker is documented.
- Migrations support both an existing V1/V2 database and a clean database.
- Documentation and `.env.example` match the implemented code.

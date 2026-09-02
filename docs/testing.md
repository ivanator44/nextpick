# Estrategia y resultados de pruebas

Fecha de ejecución: 2026-09-02. Las suites automatizadas no consumen TMDB, Google AI Studio ni OpenAI: utilizan fixtures, Mockito, MockWebServer e interceptación de red. Se realizaron además una aceptación manual del catálogo, una petición mínima de diagnóstico a OpenAI y una generación mínima satisfactoria con Google AI Studio.

## Backend

MockWebServer verifica autenticación, idioma, parseo y reintento 429 de `TmdbClient`, además del cuerpo, streaming, privacidad y errores de configuración de Google AI Studio y OpenAI. Las pruebas reproducen respuestas internas de alta demanda, cuota agotada y fallo durante el stream, y comprueban que no se filtren mensajes, modelos, instrucciones de configuración ni detalles de facturación del proveedor. También cubren mapeo movie/TV, caché, autenticación y refresh, favoritos, recomendaciones por títulos relacionados, grounding, historial, errores REST 400/401/403/409/429, manejadores de seguridad y persistencia del chat solo cuando el stream termina correctamente.

| Comando | Resultado real |
|---|---|
| `.\mvnw.cmd verify` | PASS tras sanear los errores de IA: 33 tests, 0 fallos, 0 errores, 1 omitido y JAR generado |

La prueba omitida es `PostgresMigrationIntegrationTest`: necesita un daemon Docker y se omite de forma explícita cuando el comando `docker` no está disponible. La prueba está implementada y, cuando Docker existe, comprueba migración V2→V3, conservación de datos legacy, aislamiento entre usuarios y unicidad de `(user_id, tmdb_id, media_type)`.

### Migraciones PostgreSQL

Además de la prueba condicional, se verificaron manualmente las dos rutas requeridas con PostgreSQL 17.10:

| Escenario | Resultado real |
|---|---|
| Base vacía temporal | PASS: Flyway aplicó V1, V2 y V3; Hibernate validó el esquema y Actuator respondió `UP` |
| Base existente en V2 | PASS: se insertó un usuario y favorito legacy, Flyway aplicó solo V3, Hibernate validó el esquema y Actuator respondió `UP` |
| Conservación e identidad | PASS: el favorito mantuvo `legacy_movie_id` y su relación con `legacy_movies`; se creó el índice parcial único `uq_favorites_user_tmdb_media` |
| Base local del usuario | PASS: conexión a `nextpick`, validación de las tres migraciones y esquema en V3 sin cambios pendientes |

## Frontend

Jasmine/Karma cubre rutas tipadas de catálogo, búsqueda sin llamada directa a TMDB, actualización inmediata de favoritos, refresh concurrente único, parser SSE con chunks arbitrarios y referencias partidas entre chunks. Las nuevas pruebas de componentes comprueban semántica y cierre por teclado del diálogo de autenticación, títulos relacionados y todos los modos de proveedor en detalle, y estado de error/reintento del listado.

| Comando | Resultado real |
|---|---|
| `npm run test:ci` | PASS tras la corrección del chat: 14 specs en Chrome Headless |
| `npm run lint` | PASS: todos los archivos |
| `npm run build -- --configuration production` | PASS: bundle inicial 333,42 kB raw |
| `npm run e2e` | PASS: 4 pruebas en 11,2 s; incluye una aserción del ancho útil del chat |
| `npm audit --omit=dev` | Resultado previo: 8 vulnerabilidades high en Angular 18; npm solo proponía Angular 21, actualización mayor fuera del alcance solicitado |

## E2E

Playwright intercepta `/api/**` con fixtures y recorre login → búsqueda → detalle → favorito → Favoritos → mensaje SSE → historial. También conserva capturas mock de Home y verifica ausencia de desbordamiento horizontal en móvil, tablet y escritorio. No necesita PostgreSQL, TMDB o LLM. Usa Chrome instalado.

Los comandos npm muestran avisos no bloqueantes del runtime administrado de IntelliJ (`min-release-age` desconocido en npm 11 y conflicto `NO_COLOR`/`FORCE_COLOR`). No proceden de la configuración versionada del proyecto.

## Aceptación local

Se arrancó el JAR desde `nextpick-backend` usando el `.env` real del repositorio, sin exponer sus secretos:

- PostgreSQL 17.10: conexión correcta, Flyway validó V1–V3 y no encontró migraciones pendientes.
- `GET /actuator/health`: `UP`.
- `GET /api/catalog/trending?page=0`: 20 elementos y 500 páginas informadas por TMDB.
- El backend se detuvo después de la comprobación mediante cierre ordenado.

## Aceptación de proveedores reales

- En la comprobación anterior de la alternativa OpenAI, una petición mínima llegó correctamente al endpoint Responses y devolvió HTTP 429: `You have no credits remaining`. Esa integración permanece disponible, pero requeriría añadir saldo a su organización.
- La interfaz conserva el error dentro de la conversación, pero solo muestra el mensaje público de su categoría (alta demanda, límite temporal, timeout, petición no válida, seguridad, configuración o indisponibilidad). Nunca recibe el texto interno de Google AI Studio/OpenAI. Las tarjetas TMDB solo se hacen visibles cuando comienza a llegar texto real, por lo que ya no aparecen durante unos segundos para después desaparecer.
- El proveedor activo se cambió a `AI_PROVIDER=gemini`. Google confirmó que `gemini-1.5-flash` está retirado y que `gemini-2.5-flash` no está disponible para este proyecto nuevo; el modelo se actualizó a `gemini-3.6-flash` siguiendo la recomendación devuelta por su API.
- `gemini-3.6-flash`: PASS real con HTTP 200, `finishReason=STOP` y texto generado. Spring Boot arrancó después con el bean `GeminiChatAiService`, Flyway V3 y Actuator `UP`.

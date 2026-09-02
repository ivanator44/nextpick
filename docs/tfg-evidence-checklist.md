# Checklist de evidencias para la memoria

## Capturas pendientes

- [ ] Inicio desktop con hero backdrop y las secciones reales.
- [ ] Películas y Series con el mismo género demostrando separación de tipos.
- [ ] Búsqueda unificada y detalle con proveedores ES/tráiler.
- [ ] Antes/después de añadir un favorito y sección “Para ti”.
- [ ] Login y cookie HttpOnly visible en DevTools sin exponer su valor en la memoria.
- [ ] Chat durante streaming, historial y búsqueda de conversaciones.
- [ ] Estados loading, vacío y error/reintento.
- [ ] Vista móvil (390 px), tablet (768 px) y escritorio; foco de teclado visible.
- [ ] Swagger y Actuator health.
- [ ] Salida de `mvn verify`, tests/lint/build Angular y E2E.

## Diagramas

- [ ] Exportar los Mermaid de `architecture.md` y `data-model.md`.
- [ ] Añadir diagrama de secuencia login/refresh y chat SSE si la memoria lo requiere.

## Ejemplos de código representativos

1. `TmdbClient`: timeouts, Bearer, Retry-After y errores.
2. `MovieCatalogMapper` / `TvCatalogMapper`: normalización de fuentes heterogéneas.
3. `FavoriteService`: snapshot, clave compuesta e invalidación de recomendación.
4. Interceptor Angular: renovación single-flight y reintento.
5. `OpenAiChatAiService` + `ChatGroundingService` + parser SSE.

## Antes de capturar

- [ ] Configurar variables reales sin mostrarlas.
- [x] Ejecutar migraciones en PostgreSQL limpio y copia V1/V2 (PostgreSQL 17.10, 2026-09-02; detalle en `testing.md`).
- [x] Configurar Google AI Studio y verificar una generación real mínima con `gemini-3.6-flash`; queda pendiente capturar una recomendación grounded completa desde la interfaz para la memoria.
- [ ] Limpiar consola del navegador y repetir tests.
- [ ] No presentar como superadas las limitaciones registradas en `testing.md`.

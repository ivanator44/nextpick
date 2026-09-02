# ADR-001: TMDB como fuente de verdad

- Estado: aceptada.
- Fecha: 2026-09-01.

## Contexto

El prototipo usaba un catálogo local sembrado que no podía ofrecer búsqueda amplia, datos actuales, vídeos ni proveedores fiables.

## Decisión

TMDB es la fuente canónica. Solo el backend usa el Read Access Token. Se normalizan movie/TV tras una interfaz propia. PostgreSQL guarda usuarios, favoritos, preferencias y conversaciones; un favorito incluye solo un snapshot mínimo. Caffeine reduce latencia y cuota sin introducir Redis ni sincronizar masivamente TMDB.

## Consecuencias

La aplicación depende de la disponibilidad/cuota de TMDB y los datos de proveedores pueden no existir. A cambio, elimina mantenimiento del catálogo y evita exponer credenciales. Las filas sembradas antiguas se conservan como `legacy_*` para no destruir instalaciones existentes.

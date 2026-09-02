# Requisitos de NextPick

## Funcionales

- RF-01. Registrar usuario, iniciar sesión, renovar la sesión y cerrarla.
- RF-02. Mostrar una portada agregada con hero, tendencias, populares y mejor valoradas.
- RF-03. Descubrir por tipo y género sin mezclar películas y series.
- RF-04. Buscar películas y series conjuntamente, excluyendo personas.
- RF-05. Consultar sinopsis, género, año, valoración, tráiler, similares y proveedores para España.
- RF-06. Añadir, listar y eliminar favoritos identificados por `(tmdbId, mediaType)`.
- RF-07. Recomendar “Para ti” según géneros de favoritos, excluyendo títulos guardados, y usar tendencias para usuarios nuevos.
- RF-08. Crear y recuperar conversaciones propias y recibir una respuesta incremental del asistente.
- RF-09. Mostrar atribución a TMDB y JustWatch.

## No funcionales

- RNF-01. TMDB es la fuente de verdad y sus credenciales solo existen en backend.
- RNF-02. Idioma `es-ES`, región `ES`; interfaz responsive, operable por teclado y compatible con reducción de movimiento.
- RNF-03. JWT de acceso corto y refresh token rotado, revocable, hasheado y enviado en cookie HttpOnly.
- RNF-04. Caché Caffeine por operación y contexto; timeouts y reintentos limitados para errores transitorios.
- RNF-05. Errores HTTP consistentes; entradas de búsqueda y chat limitadas.
- RNF-06. Builds y pruebas no consumen TMDB ni LLM reales.
- RNF-07. Configuración y secretos se externalizan; no existen credenciales por defecto versionadas.

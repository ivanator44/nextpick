# Estrategia de control de versiones

## Situación de partida

El repositorio Git se crea cuando NextPick ya dispone de una versión funcional. Por tanto, el primer commit se documentará como una **importación del estado actual**, no como si todo el desarrollo se hubiera realizado en ese instante ni como una reconstrucción artificial de fechas pasadas.

Si se conservan copias reales del prototipo Vanilla o de estados intermedios, pueden incorporarse después manteniendo su autoría y fecha real conocida. Sin esas copias, la evolución anterior se acredita mediante documentación, memoria, capturas y decisiones de arquitectura, no mediante commits ficticios.

## Incrementos del proyecto

| Incremento | Alcance | Resultado verificable |
|---|---|---|
| 0. Exploración y prototipo | Idea, primeras interfaces y datos ficticios en Vanilla | Validación visual del concepto; se incorporará como evidencia si se conserva el prototipo |
| 1. Replanteamiento arquitectónico | Aprendizaje obtenido en prácticas, límites del prototipo y decisión de migración | Decisión razonada de separar frontend, backend, persistencia y servicios externos |
| 2. Base arquitectónica | Angular, Spring Boot, API REST, JPA, PostgreSQL y Flyway | Estructura mantenible y esquema de datos versionado |
| 3. Identidad y datos privados | Registro, login, JWT con refresh, seguridad, usuarios y favoritos | Primera versión funcional con autenticación y persistencia por usuario |
| 4. Catálogo externo | TMDB, búsqueda, filtros, detalle, proveedores y caché | Catálogo real normalizado por el backend |
| 5. Asistente de recomendación | Gemini/OpenAI, grounding con catálogo, streaming SSE e historial | Recomendación conversacional con referencias verificadas |
| 6. Consolidación y entrega | Responsive, accesibilidad, errores seguros, pruebas y documentación | MVP validado y preparado para entrega |

Estos incrementos describen la evolución del producto. A partir de la creación de Git, cada nuevo cambio se registrará en commits pequeños, coherentes y comprobables.

## Flujo de trabajo desde ahora

1. Crear una rama para cada cambio: `feature/...`, `fix/...` o `docs/...`.
2. Hacer commits centrados en una sola intención, sin mezclar cambios ajenos.
3. Usar mensajes claros, por ejemplo: `feat(chat): añade reintento de mensajes` o `fix(auth): renueva el token una sola vez`.
4. Ejecutar las verificaciones relacionadas antes de integrar el cambio.
5. Integrar la rama en `main` mediante pull request cuando el repositorio remoto esté configurado.
6. Crear etiquetas solo para estados demostrables, por ejemplo `v1.0.0-mvp` para la entrega validada.

No es necesario hacer varios `push` para demostrar incrementalidad: Git conserva cada commit aunque todos se publiquen juntos. Lo importante es que cada commit represente un cambio real y que el historial no contenga secretos.

## Archivos que nunca deben publicarse

- `.env` y cualquier variante local con credenciales.
- Claves de TMDB, Google AI Studio, OpenAI, JWT o PostgreSQL.
- `.idea`, `node_modules`, `target`, `dist`, informes y logs generados.

`.env.example` sí se versiona porque contiene únicamente nombres de variables y valores de ejemplo.

## Publicación inicial

Una vez creado un repositorio vacío en GitHub, la publicación se realiza desde la raíz de NextPick:

```powershell
git add .
git status
git commit -m "chore: importa el MVP actual de NextPick"
git remote add origin https://github.com/USUARIO/nextpick.git
git push -u origin main
```

Antes del commit hay que revisar `git status` y confirmar que `.env`, `.idea`, `node_modules`, `target` y `dist` no aparecen. El repositorio remoto debe crearse vacío, sin README, `.gitignore` ni licencia generados por GitHub, porque esos archivos ya existen localmente.

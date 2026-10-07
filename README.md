# NextPick

NextPick es una aplicación web para descubrir películas y series desde TMDB, consultar detalles y disponibilidad en España, guardar favoritos y mantener conversaciones de recomendación. Angular nunca conoce las credenciales externas: todo el catálogo se normaliza en Spring Boot y PostgreSQL almacena únicamente datos privados y snapshots mínimos de favoritos.

## Requisitos

- Java 21 (el repositorio incluye Maven Wrapper).
- Node.js 20 LTS o 22 LTS y npm.
- PostgreSQL 16, local o mediante Docker Compose.
- Un Read Access Token de TMDB.
- Una clave del proveedor de IA solo cuando se habilite la integración real.

## Inicio rápido

1. Copia `.env.example` a `.env`. El archivo queda ignorado por Git y Spring Boot lo carga automáticamente al arrancar desde la raíz o desde `nextpick-backend`.
2. Sustituye `DB_PASSWORD`, `JWT_SECRET` y `TMDB_ACCESS_TOKEN`. Puedes generar el secreto JWT en PowerShell:

   ```powershell
   $bytes = New-Object byte[] 32
   [Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
   [Convert]::ToBase64String($bytes)
   ```

3. Prepara PostgreSQL con una de estas opciones:

   - Docker: `docker compose up -d postgres`.
   - PostgreSQL local: crea una base `nextpick` y un usuario `nextpick`, y haz coincidir su contraseña con `DB_PASSWORD`. Si utilizas el usuario `postgres` instalado en Windows, cambia también `DB_USER=postgres`.

4. Deja `AI_PROVIDER=disabled` para probar catálogo, autenticación y favoritos sin una clave de IA. Para Google AI Studio usa `AI_PROVIDER=gemini`, `AI_MODEL=gemini-3.6-flash` y define `GEMINI_API_KEY` (también se acepta la variable genérica `AI_API_KEY`). OpenAI continúa disponible con `AI_PROVIDER=openai`, `AI_MODEL=gpt-4o-mini` y `AI_API_KEY`.
5. En PowerShell, arranca el backend:

   ```powershell
   cd nextpick-backend
   .\mvnw.cmd spring-boot:run
   ```

6. En otra terminal, arranca Angular:

   ```powershell
   cd nextpick-frontend
   npm ci
   npm start
   ```

La web queda en `http://localhost:4200`; health en `http://localhost:8080/actuator/health`; Swagger UI en `http://localhost:8080/swagger-ui.html`.

El perfil determinista de demostración del chat se activa expresamente con `SPRING_PROFILES_ACTIVE=mock`. No se activa de forma silenciosa. Con `AI_PROVIDER=gemini`, el backend usa el streaming SSE de Google AI Studio; con `AI_PROVIDER=openai`, usa OpenAI Responses con `store:false`. Ambos conservan como máximo 12 mensajes recientes y reciben hasta cuatro referencias verificadas mediante TMDB. Con `AI_PROVIDER=disabled`, el chat devuelve un error claro y no fabrica recomendaciones.

### Ejecutar desde IntelliJ

Importa `nextpick-backend/pom.xml`, selecciona JDK 21 y ejecuta `NextpickApplication`. Usa como directorio de trabajo la raíz del repositorio o `nextpick-backend`; ambos localizan el `.env` automáticamente. El arranque correcto termina con `Tomcat started on port 8080` y `http://localhost:8080/actuator/health` responde `{"status":"UP"}`.

Si el arranque falla antes de Tomcat, revisa el primer `Caused by` del log:

- `Connection refused` o autenticación PostgreSQL: la base no está arrancada o `DB_URL`/usuario/contraseña no coinciden.
- error de `JWT_SECRET`: no es Base64 válido o contiene menos de 32 bytes.
- error de `TMDB_ACCESS_TOKEN`: falta el token; un placeholder permite arrancar, pero el catálogo devolverá error al consultarlo.
- error de chat indicando que OpenAI no tiene créditos: la clave es reconocida, pero su organización no tiene saldo de API. Añade créditos en la facturación de OpenAI y reinicia el backend; una suscripción a ChatGPT no incluye crédito de API.
- error indicando que el modelo Gemini no está disponible: usa `AI_MODEL=gemini-3.6-flash`; `gemini-1.5-flash` fue retirado y `gemini-2.5-flash` puede no estar habilitado para proyectos nuevos.

## Verificación

```powershell
cd nextpick-backend
.\mvnw.cmd verify

cd ..\nextpick-frontend
npm ci
npm run test:ci
npm run lint
npm run build -- --configuration production
npm run e2e
```

Para Angular en IntelliJ, abre `nextpick-frontend/package.json`, ejecuta `npm ci` y después el script `start`.

Consulta [arquitectura](docs/architecture.md), [pruebas](docs/testing.md), [manual](docs/user-manual.md) y [estrategia de control de versiones](docs/version-control.md).

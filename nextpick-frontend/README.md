# NextPick frontend

Angular 18.2 con componentes standalone, Signals, Router, RxJS y SCSS. Solo consume la API propia definida en `src/environments`; no contiene claves de TMDB ni de IA.

```powershell
npm ci
npm start
npm run test:ci
npm run lint
npm run build -- --configuration production
npm run e2e
```

El E2E intercepta todas las APIs con fixtures deterministas y recorre login, búsqueda, detalle, favorito e historial de chat. Playwright usa el canal Chrome instalado; para usar Chromium administrado ejecuta `npm run e2e:install` y ajusta `playwright.config.ts`.

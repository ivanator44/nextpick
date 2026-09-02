# Casos de uso

## Actores

- Visitante: explora, busca y consulta detalles.
- Usuario autenticado: además gestiona favoritos, recomendaciones y conversaciones.
- TMDB: suministra catálogo, vídeos y proveedores.
- Google AI Studio u OpenAI: genera texto mediante la implementación elegida en `AI_PROVIDER` cuando su clave está configurada.

## Casos principales

### CU-01 Explorar catálogo

El visitante abre Inicio. El backend sirve un agregado cacheable normalizado desde TMDB. Puede entrar en Películas o Series y filtrar por género. Si TMDB falla, ve un estado de error con reintento.

### CU-02 Buscar y consultar detalle

El visitante escribe al menos dos caracteres. Tras debounce, Angular cancela búsquedas obsoletas y muestra solo películas/series. Al abrir una tarjeta obtiene el detalle compuesto, tráiler si existe y proveedores ES; Escape o el botón cerrar devuelven el foco al flujo.

### CU-03 Autenticarse

El visitante registra o valida sus credenciales. Recibe un access token y una cookie HttpOnly de refresh. Si varias llamadas caducan a la vez, solo se renueva una vez y se reintentan; si falla, se elimina la sesión local.

### CU-04 Gestionar favorito

El usuario abre un detalle y añade el título. El backend valida/hidrata TMDB, persiste un snapshot mínimo y actualiza todas las vistas inmediatamente. Un duplicado devuelve conflicto. Al eliminarlo se invalida “Para ti”.

### CU-05 Conversar

El usuario crea o recupera una conversación propia y envía un mensaje de hasta 2.000 caracteres. Recibe eventos SSE `meta`, `delta`, `done` o `error`. Una desconexión no persiste una respuesta vacía/truncada. En perfil normal, si el proveedor no está disponible, se informa expresamente; `mock` solo sirve para demo/pruebas.

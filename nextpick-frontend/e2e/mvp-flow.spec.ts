import { expect, test } from '@playwright/test';

const title = {
  tmdbId: 603, mediaType: 'MOVIE', title: 'The Matrix', overview: 'Una hacker descubre la verdad.',
  posterUrl: null, backdropUrl: null, rating: 8.2, releaseYear: 1999, genreIds: [28],
};

test('login, search, detail, favorite and persisted chat history', async ({ page }) => {
  let authenticated = false;
  let favorite = false;
  let conversationExists = false;

  await page.route('**/api/**', async (route) => {
    const url = new URL(route.request().url());
    const path = url.pathname;
    if (path === '/api/auth/refresh') {
      if (!authenticated) return route.fulfill({ status: 401, json: { error: 'No session' } });
      return route.fulfill({ json: { accessToken: 'test-token', userId: 1, name: 'Ada', email: 'ada@example.test', avatarUrl: null } });
    }
    if (path === '/api/auth/login') {
      authenticated = true;
      return route.fulfill({ json: { accessToken: 'test-token', userId: 1, name: 'Ada', email: 'ada@example.test', avatarUrl: null } });
    }
    if (path === '/api/catalog/home') return route.fulfill({ json: {
      hero: { ...title, genres: [{ id: 28, name: 'Acción' }], trailer: null,
        providers: { region: 'ES', link: null, streaming: [], free: [], rent: [], buy: [] }, recommendations: [], favorite: false },
      trending: [title], popular: [title], topRated: [], forYou: [],
    } });
    if (path === '/api/catalog/search') return route.fulfill({ json: {
      content: [title], totalPages: 1, totalElements: 1, page: 0, last: true,
    } });
    if (path === '/api/catalog/MOVIE/603') return route.fulfill({ json: {
      ...title, genres: [{ id: 28, name: 'Acción' }], trailer: null,
      providers: { region: 'ES', link: null, streaming: [], free: [], rent: [], buy: [] },
      recommendations: [], favorite,
    } });
    if (path === '/api/favorites/MOVIE/603' && route.request().method() === 'POST') {
      favorite = true;
      return route.fulfill({ json: title });
    }
    if (path === '/api/favorites') return route.fulfill({ json: favorite ? [title] : [] });
    if (path === '/api/chat/conversations') return route.fulfill({ json: conversationExists
      ? [{ id: 7, title: 'Ciencia ficción', updatedAt: new Date().toISOString() }] : [] });
    if (path === '/api/chat/conversations/7/messages') return route.fulfill({ json: [
      { id: 1, sender: 'USER', content: 'Recomiéndame ciencia ficción', createdAt: new Date().toISOString() },
      { id: 2, sender: 'ASSISTANT', content: 'Prueba The Matrix.', createdAt: new Date().toISOString() },
    ] });
    if (path === '/api/chat/stream') {
      conversationExists = true;
      return route.fulfill({
        status: 200,
        headers: { 'content-type': 'text/event-stream' },
        body: 'event: meta\ndata: {"conversationId":7}\n\nevent: references\ndata: [{"tmdbId":603,"mediaType":"MOVIE","title":"The Matrix","posterUrl":null,"year":1999,"rating":8.2}]\n\nevent: delta\ndata: {"text":"Prueba The Matrix."}\n\nevent: done\ndata: {}\n\n',
      });
    }
    return route.fulfill({ status: 404, json: { error: `Fixture ausente: ${path}` } });
  });

  await page.goto('/');
  await page.getByRole('button', { name: 'Iniciar sesión' }).click();
  await page.getByLabel('Email').fill('ada@example.test');
  await page.getByLabel('Contraseña').fill('correct-horse');
  await page.getByRole('button', { name: 'Entrar' }).click();

  await page.getByRole('button', { name: 'Buscar' }).click();
  await page.getByPlaceholder('Buscar títulos…').fill('Matrix');
  await page.getByPlaceholder('Buscar títulos…').press('Enter');
  await expect(page.getByRole('heading', { name: /Matrix/ })).toBeVisible();
  await page.getByRole('button', { name: 'Abrir detalles de The Matrix' }).click();
  await expect(page.getByRole('heading', { name: 'The Matrix' })).toBeVisible();
  await page.getByRole('button', { name: 'Añadir a favoritos' }).click();
  await page.getByRole('button', { name: 'Cerrar' }).click();

  await page.getByRole('link', { name: 'Favoritos' }).click();
  await expect(page.getByRole('button', { name: 'Abrir detalles de The Matrix' })).toBeVisible();

  await page.getByRole('link', { name: 'Chat IA' }).click();
  const chatBounds = await page.locator('.chat-window').boundingBox();
  const viewport = page.viewportSize();
  expect(chatBounds).not.toBeNull();
  expect(chatBounds!.width).toBeGreaterThan(viewport!.width * 0.65);
  await page.getByPlaceholder('Pregúntame por una película o serie…').fill('Recomiéndame ciencia ficción');
  await page.getByRole('button', { name: 'Enviar mensaje' }).click();
  await expect(page.getByText('Prueba The Matrix.')).toBeVisible();
  await expect(page.getByRole('link', { name: /The Matrix/ })).toBeVisible();
  await page.getByRole('button', { name: 'Ciencia ficción' }).click();
  await expect(page.getByText('Recomiéndame ciencia ficción')).toBeVisible();
});

for (const viewport of [
  { name: 'mobile', width: 390, height: 844 },
  { name: 'tablet', width: 768, height: 1024 },
  { name: 'desktop', width: 1440, height: 900 },
]) {
  test(`home sin desbordamiento horizontal en ${viewport.name}`, async ({ page }, testInfo) => {
    await page.setViewportSize({ width: viewport.width, height: viewport.height });
    await page.route('**/api/**', async (route) => {
      const path = new URL(route.request().url()).pathname;
      if (path === '/api/auth/refresh') {
        return route.fulfill({ status: 401, json: { error: 'No session' } });
      }
      if (path === '/api/catalog/home') {
        return route.fulfill({ json: {
          hero: { ...title, genres: [{ id: 28, name: 'Acción' }], trailer: null,
            providers: { region: 'ES', link: null, streaming: [], free: [], rent: [], buy: [] }, recommendations: [], favorite: false },
          trending: [title], popular: [title], topRated: [], forYou: [],
        } });
      }
      return route.fulfill({ status: 404, json: { error: `Fixture ausente: ${path}` } });
    });

    await page.goto('/');
    await expect(page.getByRole('heading', { name: 'The Matrix' })).toBeVisible();
    const dimensions = await page.evaluate(() => ({
      documentWidth: document.documentElement.scrollWidth,
      viewportWidth: document.documentElement.clientWidth,
    }));
    expect(dimensions.documentWidth).toBeLessThanOrEqual(dimensions.viewportWidth);
    await page.screenshot({ path: testInfo.outputPath(`home-${viewport.width}.png`), fullPage: true });
  });
}

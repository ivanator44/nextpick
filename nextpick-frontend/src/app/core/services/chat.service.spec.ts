import { HttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';
import { ChatService } from './chat.service';

describe('ChatService', () => {
  it('procesa referencias y deltas aunque los eventos lleguen partidos', async () => {
    const encoder = new TextEncoder();
    const chunks = [
      'event: meta\ndata: {"conversationId":9}\n\nevent: refe',
      'rences\ndata: [{"tmdbId":603,"mediaType":"MOVIE","title":"The Matrix","posterUrl":"/p.jpg"}]\n\n' +
        'event: delta\ndata: {"text":"Hola "}\n\n',
      'event: delta\ndata: {"text":"mundo"}\n\nevent: done\ndata: {"conversationId":9}\n\n',
    ];
    const stream = new ReadableStream<Uint8Array>({
      start(controller) {
        chunks.forEach((chunk) => controller.enqueue(encoder.encode(chunk)));
        controller.close();
      },
    });
    spyOn(window, 'fetch').and.resolveTo(new Response(stream, { status: 200 }));
    const auth = { getAccessToken: () => 'access-token' } as AuthService;
    const service = new ChatService({} as HttpClient, auth);
    const deltas: string[] = [];
    let references: { tmdbId: number; title: string }[] = [];
    let conversationId = 0;

    await service.sendMessageStreaming(
      null,
      'Recomiéndame ciencia ficción',
      (delta) => deltas.push(delta),
      (id) => (conversationId = id),
      (items) => (references = items),
    );

    expect(deltas).toEqual(['Hola ', 'mundo']);
    expect(references).toEqual([
      jasmine.objectContaining({ tmdbId: 603, title: 'The Matrix' }),
    ]);
    expect(conversationId).toBe(9);
  });

  it('conserva el motivo enviado por el backend cuando falla el stream', async () => {
    const encoder = new TextEncoder();
    const stream = new ReadableStream<Uint8Array>({
      start(controller) {
        controller.enqueue(encoder.encode(
          'event: references\ndata: [{"tmdbId":603,"mediaType":"MOVIE","title":"The Matrix"}]\n\n' +
          'event: error\ndata: {"message":"La cuenta de OpenAI no tiene créditos disponibles"}\n\n'
        ));
        controller.close();
      },
    });
    spyOn(window, 'fetch').and.resolveTo(new Response(stream, { status: 200 }));
    const auth = { getAccessToken: () => 'access-token' } as AuthService;
    const service = new ChatService({} as HttpClient, auth);

    await expectAsync(service.sendMessageStreaming(
      null, 'Recomiéndame algo', () => undefined, () => undefined, () => undefined,
    )).toBeRejectedWithError('La cuenta de OpenAI no tiene créditos disponibles');
  });

  it('rechaza un stream que termina sin el evento done', async () => {
    const encoder = new TextEncoder();
    const stream = new ReadableStream<Uint8Array>({
      start(controller) {
        controller.enqueue(encoder.encode('event: delta\ndata: {"text":"Parcial"}\n\n'));
        controller.close();
      },
    });
    spyOn(window, 'fetch').and.resolveTo(new Response(stream, { status: 200 }));
    const auth = { getAccessToken: () => 'access-token' } as AuthService;
    const service = new ChatService({} as HttpClient, auth);

    await expectAsync(service.sendMessageStreaming(
      null, 'Hola', () => undefined, () => undefined, () => undefined,
    )).toBeRejectedWithError('La conexión con el asistente terminó antes de completar la respuesta');
  });
});

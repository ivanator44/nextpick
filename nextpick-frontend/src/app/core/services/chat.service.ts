import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { ChatConversationSummary, ChatMessage, ChatReference } from '../models/chat.model';
import { AuthService } from './auth.service';
import { firstValueFrom } from 'rxjs';
import { SseEvent, SseParser } from '../utils/sse-parser';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly baseUrl = `${environment.apiUrl}/chat`;

  readonly conversations = signal<ChatConversationSummary[]>([]);
  readonly activeMessages = signal<ChatMessage[]>([]);

  constructor(private http: HttpClient, private authService: AuthService) {}

  loadConversations() {
    return this.http.get<ChatConversationSummary[]>(`${this.baseUrl}/conversations`);
  }

  loadMessages(conversationId: number) {
    return this.http.get<ChatMessage[]>(`${this.baseUrl}/conversations/${conversationId}/messages`);
  }

  /**
   * Envía un mensaje y consume la respuesta de la IA en streaming vía SSE.
   * Se usa fetch + ReadableStream (en vez de EventSource nativo) porque
   * necesitamos enviar el JWT en la cabecera Authorization, algo que
   * EventSource no permite de forma nativa.
   */
  async sendMessageStreaming(
    conversationId: number | null,
    content: string,
    onChunk: (text: string) => void,
    onDone: (finalConversationId: number) => void,
    onReferences: (references: ChatReference[]) => void,
    signal?: AbortSignal
  ): Promise<void> {
    return this.streamRequest(conversationId, content, onChunk, onDone, onReferences, signal, false);
  }

  private async streamRequest(
    conversationId: number | null,
    content: string,
    onChunk: (text: string) => void,
    onDone: (finalConversationId: number) => void,
    onReferences: (references: ChatReference[]) => void,
    signal: AbortSignal | undefined,
    retried: boolean
  ): Promise<void> {
    const token = this.authService.getAccessToken();

    const response = await fetch(`${this.baseUrl}/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ conversationId, content }),
      credentials: 'include',
      signal,
    });

    if (response.status === 401 && !retried) {
      await firstValueFrom(this.authService.refreshAccessToken());
      return this.streamRequest(conversationId, content, onChunk, onDone, onReferences, signal, true);
    }
    if (!response.ok) {
      let message = `El servidor del chat respondió con el estado ${response.status}`;
      try {
        const body = await response.json() as { message?: string };
        if (body.message) message = body.message;
      } catch {
        // La respuesta puede no incluir JSON (por ejemplo, un proxy intermedio).
      }
      throw new Error(message);
    }
    if (!response.body) throw new Error('El servidor no devolvió un stream');

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    const parser = new SseParser();
    let resolvedConversationId = conversationId ?? 0;
    let completed = false;

    const processEvent = (event: SseEvent) => {
      if (event.event === 'meta') {
        resolvedConversationId = Number(JSON.parse(event.data).conversationId);
      } else if (event.event === 'delta') {
        onChunk(JSON.parse(event.data).text);
      } else if (event.event === 'references') {
        onReferences(JSON.parse(event.data) as ChatReference[]);
      } else if (event.event === 'error') {
        throw new Error(JSON.parse(event.data).message ?? 'Error de streaming');
      } else if (event.event === 'done') {
        const done = JSON.parse(event.data) as { conversationId?: number };
        if (done.conversationId) resolvedConversationId = done.conversationId;
        completed = true;
      } else if (event.data) {
        onChunk(event.data);
      }
    };

    try {
      while (true) {
        const { value, done } = await reader.read();
        if (done) break;
        for (const event of parser.feed(decoder.decode(value, { stream: true }))) {
          processEvent(event);
        }
      }
      for (const event of parser.feed(decoder.decode(), true)) processEvent(event);
    } finally {
      reader.releaseLock();
    }

    if (!completed) throw new Error('La conexión con el asistente terminó antes de completar la respuesta');
    onDone(resolvedConversationId);
  }
}

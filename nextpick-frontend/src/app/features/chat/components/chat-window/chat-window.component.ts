import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ChatMessage } from '../../../../core/models/chat.model';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-chat-window',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="chat-window">
      <button class="back-btn" (click)="backToCatalog.emit()">Volver al catálogo</button>

      @if (messages.length === 0) {
        <!-- Estado inicial: todo centrado matemáticamente en la pantalla -->
        <div class="initial-state">
          <svg width="42" height="42" viewBox="0 0 24 24" fill="none">
            <path d="M5 3L19 12L5 21V3Z" fill="var(--accent)" />
          </svg>
          <h1>¿Qué estás buscando?</h1>
          <div class="input-bar">
            <input
              type="text"
              placeholder="Pregúntame por una película o serie…"
              [(ngModel)]="draft"
              (keyup.enter)="submit()"
            />
            <button [disabled]="!draft.trim() || sending" (click)="submit()" aria-label="Enviar mensaje">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M2 21l21-9L2 3v7l15 2-15 2z"/></svg>
            </button>
          </div>
        </div>
      } @else {
        <!-- Estado activo: historial arriba, input anclado abajo -->
        <div class="messages">
          @for (msg of messages; track $index) {
            <div class="message" [class.user]="msg.sender === 'USER'" [class.assistant]="msg.sender === 'ASSISTANT'"
                 [class.failed]="msg.error">
              <div class="bubble">
                @if (msg.streaming && !msg.content) {
                  <p class="thinking" aria-live="polite"><span></span><span></span><span></span><em>Consultando al asistente…</em></p>
                } @else {
                  <p>{{ msg.content }}</p>
                }
                @if (msg.references?.length) {
                  <div class="references" aria-label="Títulos verificados mencionados">
                    @for (reference of msg.references; track reference.mediaType + ':' + reference.tmdbId) {
                      <a class="reference" [routerLink]="['/search']" [queryParams]="{ q: reference.title }">
                        <img [src]="reference.posterUrl || placeholder" [alt]="reference.title"
                          (error)="imageFailed($event)" />
                        <span><strong>{{ reference.title }}</strong>
                          <small>{{ reference.mediaType === 'MOVIE' ? 'Película' : 'Serie' }} @if (reference.year) { · {{ reference.year }} }</small>
                        </span>
                      </a>
                    }
                  </div>
                }
              </div>
            </div>
          }
        </div>

        <div class="input-bar docked">
          <input
            type="text"
            placeholder="Escribe un mensaje…"
            [(ngModel)]="draft"
            (keyup.enter)="submit()"
          />
          <button [disabled]="!draft.trim() || sending" (click)="submit()" aria-label="Enviar mensaje">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M2 21l21-9L2 3v7l15 2-15 2z"/></svg>
          </button>
        </div>
      }
    </div>
  `,
  styles: [`
    :host { display: block; flex: 1 1 auto; min-width: 0; }
    .chat-window {
      width: 100%; min-width: 0; height: 100dvh; position: relative;
      display: flex; flex-direction: column;
      background: var(--bg-primary);
    }
    .back-btn {
      position: absolute; top: 1.2rem; right: 1.5rem; z-index: 5;
      background: var(--bg-surface-alt); color: var(--text-secondary);
      padding: 0.5rem 1rem; border-radius: 20px; font-size: 0.82rem;
    }
    .back-btn:hover { color: var(--text-primary); }

    /* Estado inicial: centrado matemático absoluto */
    .initial-state {
      flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center;
      gap: 1.1rem; padding: 0 1.5rem;
    }
    .initial-state h1 { font-size: 1.6rem; margin: 0; font-weight: 600; }

    .input-bar {
      display: flex; align-items: center; gap: 0.6rem;
      width: min(820px, calc(100% - 3rem));
      background: var(--bg-surface); border: 1px solid var(--border-subtle);
      border-radius: 28px; padding: 0.4rem 0.4rem 0.4rem 1.2rem;
    }
    .input-bar input { flex: 1; background: transparent; border: none; color: var(--text-primary); font-size: 0.95rem; }
    .input-bar input:focus { outline: none; }
    .input-bar button {
      width: 38px; height: 38px; border-radius: 50%; flex-shrink: 0;
      background: var(--accent); color: #fff;
      display: flex; align-items: center; justify-content: center;
      transition: opacity var(--transition-fast);
    }
    .input-bar button:disabled { opacity: 0.4; cursor: not-allowed; }

    /* Estado activo */
    .messages {
      flex: 1; overflow-y: auto;
      display: flex; flex-direction: column; gap: 1rem;
      padding: 4.5rem clamp(1rem, 4vw, 3.5rem) 1.5rem;
      max-width: 1180px; width: 100%; margin: 0 auto;
    }
    .message { display: flex; }
    .message.user { justify-content: flex-end; }
    .message.assistant { justify-content: flex-start; }
    .bubble {
      max-width: min(88%, 900px); padding: 0.9rem 1.15rem; border-radius: var(--radius-md);
      font-size: 0.95rem; line-height: 1.55;
    }
    .bubble p { margin: 0; white-space: pre-wrap; }
    .references { display: grid; grid-template-columns: repeat(auto-fit, minmax(170px, 1fr)); gap: .65rem; margin-top: .9rem; }
    .reference { display: flex; align-items: center; gap: .55rem; min-width: 0; padding: .45rem;
      border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); background: rgba(0,0,0,.18); }
    .reference:hover, .reference:focus-visible { border-color: var(--accent); }
    .reference img { width: 36px; height: 54px; object-fit: cover; border-radius: 4px; flex: 0 0 auto; }
    .reference span { display: grid; min-width: 0; }
    .reference strong { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: .78rem; }
    .reference small { color: var(--text-secondary); font-size: .68rem; }
    .message.user .bubble { background: var(--accent); color: #fff; }
    .message.assistant .bubble { background: var(--bg-surface); }
    .message.failed .bubble { color: #ffd0d0; background: rgba(229, 9, 20, .12); border: 1px solid rgba(229, 9, 20, .35); }
    .thinking { display: flex; align-items: center; gap: .3rem; color: var(--text-secondary); }
    .thinking span { width: 6px; height: 6px; border-radius: 50%; background: currentColor; animation: pulse 1s infinite alternate; }
    .thinking span:nth-child(2) { animation-delay: .15s; }
    .thinking span:nth-child(3) { animation-delay: .3s; }
    .thinking em { margin-left: .35rem; font-style: normal; font-size: .82rem; }

    .input-bar.docked {
      /* Se desliza fluidamente hacia el pie de pantalla al activarse el chat */
      margin: 0 auto 1.5rem; animation: slide-up var(--transition-base);
    }
    @keyframes slide-up {
      from { transform: translateY(20px); opacity: 0; }
      to { transform: translateY(0); opacity: 1; }
    }
    @keyframes pulse { to { opacity: .25; transform: translateY(-2px); } }
    @media (max-width: 720px) {
      .messages { padding: 4rem .8rem 1rem; }
      .bubble { max-width: 94%; }
      .input-bar { width: calc(100% - 1.5rem); }
      .references { grid-template-columns: 1fr; }
      .back-btn { top: .8rem; right: .8rem; }
    }
  `],
})
export class ChatWindowComponent {
  readonly placeholder = 'assets/poster-placeholder.svg';
  @Input() messages: ChatMessage[] = [];
  @Input() conversationId: number | null = null;
  @Input() sending = false;

  @Output() backToCatalog = new EventEmitter<void>();
  @Output() sendRequested = new EventEmitter<string>();

  draft = '';

  imageFailed(event: Event): void {
    (event.target as HTMLImageElement).src = this.placeholder;
  }

  submit(): void {
    const content = this.draft.trim();
    if (!content || this.sending) return;

    this.draft = '';
    this.sendRequested.emit(content);
  }
}

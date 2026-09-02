import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ChatConversationSummary } from '../../../../core/models/chat.model';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-chat-sidebar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <aside class="sidebar" [class.collapsed]="collapsed">
      <!-- Cabecera: el isotipo muta a icono de "expandir" al hacer hover mientras está colapsada.
           Se logra superponiendo ambos iconos en la misma celda y cruzando su opacidad,
           de forma que no hay desplazamiento de layout entre estados. -->
      <div class="sidebar-header">
        <a routerLink="/" class="logo-slot">
          <svg class="isotype" width="26" height="26" viewBox="0 0 24 24" fill="none">
            <path d="M5 3L19 12L5 21V3Z" fill="var(--accent)" />
          </svg>
          @if (collapsed) {
            <svg class="expand-icon" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 (click)="expandClicked($event)">
              <polyline points="9 18 15 12 9 6" />
            </svg>
          }
        </a>
        @if (!collapsed) {
          <span class="logo-text">NextPick</span>
          <button class="collapse-btn" (click)="toggleCollapsed.emit()" aria-label="Colapsar">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="15 18 9 12 15 6" />
            </svg>
          </button>
        }
      </div>

      <div class="sidebar-actions">
        <button class="action-btn" (click)="newChat.emit()">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" />
          </svg>
          @if (!collapsed) { <span>Nuevo chat</span> }
        </button>

        @if (!collapsed) {
          <button class="action-btn" (click)="searchOpen.set(!searchOpen())">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="7" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
            <span>Buscar conversaciones</span>
          </button>
        }
      </div>

      @if (!collapsed) {
        @if (searchOpen()) {
          <input class="search-input" placeholder="Buscar…" [ngModel]="searchTerm()"
            (ngModelChange)="searchTerm.set($event)" aria-label="Buscar conversaciones" />
        }

        <div class="history">
          @if (groupedHistory().thisMonth.length > 0) {
            <p class="group-label">Este mes</p>
            @for (conv of groupedHistory().thisMonth; track conv.id) {
              <button class="history-item" [class.active]="conv.id === activeConversationId"
                      (click)="conversationSelected.emit(conv.id)">
                {{ conv.title }}
              </button>
            }
          }

          @if (groupedHistory().lastMonth.length > 0) {
            <p class="group-label">Mes pasado</p>
            @for (conv of groupedHistory().lastMonth; track conv.id) {
              <button class="history-item" [class.active]="conv.id === activeConversationId"
                      (click)="conversationSelected.emit(conv.id)">
                {{ conv.title }}
              </button>
            }
          }
        </div>
      }

      <!-- Bloque de perfil anclado abajo: actúa como botón para el menú de usuario -->
      <button class="profile-block" (click)="profileClicked.emit()">
        <span class="avatar-fallback">{{ initials() }}</span>
        @if (!collapsed) { <span class="profile-name">{{ authService.currentUser()?.name }}</span> }
      </button>
    </aside>
  `,
  styles: [`
    .sidebar {
      width: 280px; height: 100vh;
      background: var(--bg-surface);
      border-right: 1px solid var(--border-subtle);
      display: flex; flex-direction: column;
      transition: width var(--transition-base);
      flex-shrink: 0;
    }
    .sidebar.collapsed { width: 72px; }

    .sidebar-header {
      display: flex; align-items: center; gap: 0.6rem;
      padding: 1.1rem 1rem; position: relative;
    }
    .logo-slot { position: relative; width: 26px; height: 26px; display: flex; align-items: center; justify-content: center; }
    .isotype, .expand-icon { position: absolute; transition: opacity var(--transition-fast); }
    .logo-slot:hover .isotype { opacity: 0; }
    .logo-slot .expand-icon { opacity: 0; }
    .logo-slot:hover .expand-icon { opacity: 1; }
    .logo-text { font-weight: 700; flex: 1; }
    .collapse-btn { color: var(--text-secondary); }
    .collapse-btn:hover { color: var(--text-primary); }

    .sidebar-actions { display: flex; flex-direction: column; gap: 0.3rem; padding: 0 0.7rem; margin-bottom: 0.8rem; }
    .action-btn {
      display: flex; align-items: center; gap: 0.7rem;
      padding: 0.6rem 0.7rem; border-radius: var(--radius-sm);
      color: var(--text-primary); font-size: 0.88rem;
    }
    .action-btn:hover { background: var(--bg-surface-alt); }

    .search-input {
      margin: 0 1rem 0.8rem; padding: 0.5rem 0.8rem;
      background: var(--bg-surface-alt); border: 1px solid var(--border-subtle);
      border-radius: var(--radius-sm); color: var(--text-primary); font-size: 0.85rem;
    }

    .history { flex: 1; overflow-y: auto; padding: 0 0.7rem; }
    .group-label { font-size: 0.75rem; color: var(--text-secondary); margin: 0.8rem 0.6rem 0.3rem; }
    .history-item {
      display: block; width: 100%; text-align: left;
      padding: 0.55rem 0.7rem; border-radius: var(--radius-sm);
      font-size: 0.85rem; color: var(--text-primary);
      white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
    }
    .history-item:hover { background: var(--bg-surface-alt); }
    .history-item.active { background: var(--bg-surface-alt); color: var(--accent); }

    .profile-block {
      display: flex; align-items: center; gap: 0.7rem;
      padding: 0.9rem 1rem; border-top: 1px solid var(--border-subtle);
    }
    .profile-block:hover { background: var(--bg-surface-alt); }
    .avatar-fallback {
      width: 30px; height: 30px; border-radius: 50%; flex-shrink: 0;
      background: var(--accent); color: #fff; font-size: 0.78rem; font-weight: 600;
      display: flex; align-items: center; justify-content: center;
    }
    .profile-name { font-size: 0.85rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  `],
})
export class ChatSidebarComponent {
  @Input() collapsed = false;
  private readonly conversationsSignal = signal<ChatConversationSummary[]>([]);
  @Input() set conversations(value: ChatConversationSummary[]) {
    this.conversationsSignal.set(value);
  }
  @Input() activeConversationId: number | null = null;

  @Output() toggleCollapsed = new EventEmitter<void>();
  @Output() newChat = new EventEmitter<void>();
  @Output() conversationSelected = new EventEmitter<number>();
  @Output() profileClicked = new EventEmitter<void>();

  readonly searchOpen = signal(false);
  readonly searchTerm = signal('');

  readonly groupedHistory = computed(() => {
    const now = new Date();
    const thisMonth: ChatConversationSummary[] = [];
    const lastMonth: ChatConversationSummary[] = [];

    for (const conv of this.conversationsSignal()) {
      const term = this.searchTerm().trim().toLowerCase();
      if (term && !conv.title.toLowerCase().includes(term)) continue;

      const updated = new Date(conv.updatedAt);
      const sameMonth = updated.getMonth() === now.getMonth() && updated.getFullYear() === now.getFullYear();
      (sameMonth ? thisMonth : lastMonth).push(conv);
    }

    return { thisMonth, lastMonth };
  });

  constructor(public authService: AuthService) {}

  initials(): string {
    const name = this.authService.currentUser()?.name ?? '';
    return name.split(' ').map((n) => n[0]).slice(0, 2).join('').toUpperCase();
  }

  expandClicked(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.toggleCollapsed.emit();
  }
}

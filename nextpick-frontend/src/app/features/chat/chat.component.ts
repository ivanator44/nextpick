import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ChatSidebarComponent } from './components/chat-sidebar/chat-sidebar.component';
import { ChatWindowComponent } from './components/chat-window/chat-window.component';
import { ChatService } from '../../core/services/chat.service';
import { ChatConversationSummary, ChatMessage, ChatReference } from '../../core/models/chat.model';
import { ToastService } from '../../core/services/toast.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, ChatSidebarComponent, ChatWindowComponent],
  template: `
    <div class="chat-layout">
      <app-chat-sidebar
        [collapsed]="sidebarCollapsed()"
        [conversations]="conversations()"
        [activeConversationId]="activeConversationId()"
        (toggleCollapsed)="sidebarCollapsed.set(!sidebarCollapsed())"
        (newChat)="startNewChat()"
        (conversationSelected)="selectConversation($event)"
        (profileClicked)="profileOpen.set(!profileOpen())"
      />

      <app-chat-window
        [messages]="messages()"
        [conversationId]="activeConversationId()"
        [sending]="sending()"
        (backToCatalog)="router.navigate(['/'])"
        (sendRequested)="send($event)"
      />
      @if (profileOpen()) {
        <div class="profile-menu" role="menu">
          <strong>{{ authService.currentUser()?.name }}</strong>
          <span>{{ authService.currentUser()?.email }}</span>
          <button (click)="router.navigate(['/'])">Volver al catálogo</button>
          <button (click)="authService.logout()">Cerrar sesión</button>
        </div>
      }
    </div>
  `,
  styles: [`
    .chat-layout { display: flex; width: 100%; min-width: 0; height: 100dvh; overflow: hidden; position: relative; }
    app-chat-window { display: block; flex: 1 1 auto; min-width: 0; }
    .profile-menu { position: absolute; z-index: 10; left: 1rem; bottom: 4.5rem; width: 250px; display: grid; gap: .6rem;
      padding: 1rem; background: var(--bg-surface-alt); border: 1px solid var(--border-subtle); border-radius: var(--radius-md); box-shadow: var(--shadow-elevated); }
    .profile-menu span { color: var(--text-secondary); font-size: .8rem; overflow: hidden; text-overflow: ellipsis; }
    .profile-menu button { text-align: left; padding: .45rem; border-radius: var(--radius-sm); }
    .profile-menu button:hover { background: var(--bg-surface); }
  `],
})
export class ChatComponent implements OnInit, OnDestroy {
  readonly sidebarCollapsed = signal(false);
  readonly conversations = signal<ChatConversationSummary[]>([]);
  readonly activeConversationId = signal<number | null>(null);
  readonly messages = signal<ChatMessage[]>([]);
  readonly sending = signal(false);
  readonly profileOpen = signal(false);
  private activeRequest: AbortController | null = null;

  constructor(private chatService: ChatService, private toast: ToastService,
              public authService: AuthService,
              public router: Router) {}

  ngOnInit(): void {
    this.refreshConversations();
  }

  refreshConversations(): void {
    this.chatService.loadConversations().subscribe((convs) => this.conversations.set(convs));
  }

  startNewChat(): void {
    this.activeRequest?.abort();
    this.activeConversationId.set(null);
    this.messages.set([]);
  }

  selectConversation(id: number): void {
    this.activeRequest?.abort();
    this.activeConversationId.set(id);
    this.chatService.loadMessages(id).subscribe((msgs) =>
      this.messages.set(msgs.map((m) => ({ sender: m.sender as 'USER' | 'ASSISTANT', content: m.content })))
    );
  }

  async send(content: string): Promise<void> {
    if (this.sending()) return;
    this.sending.set(true);
    this.messages.update((messages) => [...messages,
      { sender: 'USER', content },
      { sender: 'ASSISTANT', content: '', streaming: true },
    ]);
    this.activeRequest = new AbortController();
    let pendingReferences: ChatReference[] = [];
    try {
      await this.chatService.sendMessageStreaming(
        this.activeConversationId(),
        content,
        (chunk) => this.messages.update((messages) => {
          const updated = [...messages];
          const last = updated.at(-1);
          if (last?.sender === 'ASSISTANT') {
            updated[updated.length - 1] = {
              ...last,
              content: last.content + chunk,
              references: pendingReferences,
            };
          }
          return updated;
        }),
        (conversationId) => {
          this.activeConversationId.set(conversationId);
          this.messages.update((messages) => messages.map((message, index) =>
            index === messages.length - 1 ? { ...message, streaming: false } : message));
          this.refreshConversations();
        },
        (references) => { pendingReferences = references; },
        this.activeRequest.signal
      );
    } catch (error) {
      if ((error as Error).name !== 'AbortError') {
        const reason = error instanceof Error && error.message
          ? error.message
          : 'No se pudo completar la respuesta del asistente';
        this.messages.update((messages) => messages.map((message, index) =>
          index === messages.length - 1 && message.sender === 'ASSISTANT'
            ? { sender: 'ASSISTANT', content: reason, streaming: false, error: true }
            : message));
        this.toast.show(reason, 'error');
      }
    } finally {
      this.sending.set(false);
      this.activeRequest = null;
    }
  }

  ngOnDestroy(): void {
    this.activeRequest?.abort();
  }
}

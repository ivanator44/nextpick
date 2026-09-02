import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, ElementRef, EventEmitter, HostListener, OnDestroy, Output, ViewChild, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-auth-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="overlay backdrop-blur" (click)="close()">
      <div #dialog class="modal" role="dialog" aria-modal="true" aria-labelledby="auth-title"
        tabindex="-1" (click)="$event.stopPropagation()">
        <button class="close-btn" type="button" (click)="close()" aria-label="Cerrar">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 6l12 12M18 6L6 18"/></svg>
        </button>

        <h2 id="auth-title">{{ isRegisterMode() ? 'Crear cuenta' : 'Iniciar sesión' }}</h2>

        <form (ngSubmit)="submit()">
          @if (isRegisterMode()) {
            <label>
              Nombre
              <input type="text" [(ngModel)]="name" name="name" required />
            </label>
          }

          <label>
            Email
            <input type="email" [(ngModel)]="email" name="email" required />
          </label>

          <label>
            Contraseña
            <input type="password" [(ngModel)]="password" name="password" required minlength="8" />
          </label>

          @if (errorMessage()) {
            <p class="error" role="alert">{{ errorMessage() }}</p>
          }

          <button type="submit" class="submit-btn" [disabled]="loading()">
            {{ loading() ? 'Procesando…' : (isRegisterMode() ? 'Registrarme' : 'Entrar') }}
          </button>
        </form>

        <p class="toggle-mode">
          {{ isRegisterMode() ? '¿Ya tienes cuenta?' : '¿Aún no tienes cuenta?' }}
          <button type="button" class="mode-btn" (click)="toggleMode()">{{ isRegisterMode() ? 'Inicia sesión' : 'Regístrate' }}</button>
        </p>
      </div>
    </div>
  `,
  styles: [`
    .overlay {
      position: fixed; inset: 0;
      background: var(--bg-overlay);
      display: flex; align-items: center; justify-content: center;
      z-index: 1000;
    }
    .modal {
      background: var(--bg-surface);
      border: 1px solid var(--border-subtle);
      border-radius: var(--radius-lg);
      padding: 2.5rem;
      width: 100%;
      max-width: 380px;
      position: relative;
      box-shadow: var(--shadow-elevated);
    }
    .close-btn {
      position: absolute; top: 1rem; right: 1rem;
      color: var(--text-secondary);
      font-size: 1.1rem;
    }
    .close-btn:hover { color: var(--text-primary); }
    h2 { margin: 0 0 1.5rem; font-size: 1.4rem; }
    form { display: flex; flex-direction: column; gap: 1rem; }
    label {
      display: flex; flex-direction: column; gap: 0.4rem;
      font-size: 0.85rem; color: var(--text-secondary);
    }
    input {
      background: var(--bg-surface-alt);
      border: 1px solid var(--border-subtle);
      border-radius: var(--radius-sm);
      padding: 0.65rem 0.8rem;
      color: var(--text-primary);
      font-size: 0.95rem;
    }
    input:focus { outline: 1px solid var(--accent); }
    .submit-btn {
      margin-top: 0.5rem;
      background: var(--accent);
      color: #fff;
      font-weight: 600;
      padding: 0.7rem;
      border-radius: var(--radius-sm);
      transition: background var(--transition-fast);
    }
    .submit-btn:hover:not(:disabled) { background: var(--accent-hover); }
    .submit-btn:disabled { opacity: 0.6; cursor: not-allowed; }
    .error { color: var(--accent); font-size: 0.85rem; margin: 0; }
    .toggle-mode {
      margin-top: 1.5rem; text-align: center;
      font-size: 0.85rem; color: var(--text-secondary);
    }
    .mode-btn { color: var(--text-primary); font-weight: 600; margin-left: 0.3rem; padding: 0; }
    .mode-btn:hover { color: var(--accent); }
  `],
})
export class AuthModalComponent implements AfterViewInit, OnDestroy {
  @ViewChild('dialog') dialog?: ElementRef<HTMLElement>;
  @Output() closed = new EventEmitter<void>();

  readonly isRegisterMode = signal(false);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  name = '';
  email = '';
  password = '';
  private readonly previouslyFocused = document.activeElement as HTMLElement | null;

  constructor(private authService: AuthService) {}

  ngAfterViewInit(): void {
    queueMicrotask(() => {
      const input = this.dialog?.nativeElement.querySelector<HTMLElement>('input');
      (input ?? this.dialog?.nativeElement)?.focus();
    });
  }

  toggleMode(): void {
    this.isRegisterMode.set(!this.isRegisterMode());
    this.errorMessage.set(null);
  }

  submit(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    const request$ = this.isRegisterMode()
      ? this.authService.register({ name: this.name, email: this.email, password: this.password })
      : this.authService.login({ email: this.email, password: this.password });

    request$.subscribe({
      next: () => {
        this.loading.set(false);
        this.closed.emit();
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.error ?? 'Ha ocurrido un error. Inténtalo de nuevo.');
      },
    });
  }

  close(): void {
    this.closed.emit();
  }

  @HostListener('document:keydown', ['$event'])
  handleKeyboard(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      event.preventDefault();
      this.close();
      return;
    }
    if (event.key !== 'Tab' || !this.dialog) return;
    const focusable = this.dialog.nativeElement.querySelectorAll<HTMLElement>(
      'button:not([disabled]), input:not([disabled]), [tabindex]:not([tabindex="-1"])'
    );
    if (!focusable.length) return;
    const first = focusable[0];
    const last = focusable[focusable.length - 1];
    if (event.shiftKey && document.activeElement === first) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && document.activeElement === last) {
      event.preventDefault();
      first.focus();
    }
  }

  ngOnDestroy(): void {
    this.previouslyFocused?.focus();
  }
}

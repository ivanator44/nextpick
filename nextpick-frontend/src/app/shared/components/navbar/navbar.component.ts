import { CommonModule } from '@angular/common';
import { Component, DestroyRef, EventEmitter, Output, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { Subject, debounceTime, distinctUntilChanged, map } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive],
  template: `
    <header class="navbar">
      <div class="navbar-left">
        <a routerLink="/" class="brand">
          <!-- Isotipo simple en SVG: evita depender de un asset externo -->
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
            <path d="M5 3L19 12L5 21V3Z" fill="var(--accent)" />
          </svg>
          <span>NextPick</span>
        </a>

        <nav class="nav-links">
          <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">Inicio</a>
          <a routerLink="/movies" routerLinkActive="active">Películas</a>
          <a routerLink="/series" routerLinkActive="active">Series</a>
          <a routerLink="/favorites" routerLinkActive="active">Favoritos</a>
          <a routerLink="/chat" routerLinkActive="active">Chat IA</a>
        </nav>
      </div>

      <div class="navbar-right">
        <!-- Buscador expandible: al hacer foco se ensancha; con Enter navega a /movies?q= -->
        <div class="search-box" [class.expanded]="searchExpanded()">
          <button class="search-icon-btn" (click)="toggleSearch()" aria-label="Buscar">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="7" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
          </button>
          <input
            #searchInput
            type="text"
            placeholder="Buscar títulos…"
            [(ngModel)]="searchQuery"
            (ngModelChange)="searchChanges.next($event)"
            (keyup.enter)="submitSearch()"
            (blur)="onSearchBlur()"
          />
        </div>

        @if (authService.isAuthenticated()) {
          <div class="user-menu">
            <button class="avatar-btn" (click)="menuOpen.set(!menuOpen())">
              @if (authService.currentUser()?.avatarUrl) {
                <img [src]="authService.currentUser()?.avatarUrl" alt="avatar" />
              } @else {
                <span class="avatar-fallback">{{ initials() }}</span>
              }
            </button>

            @if (menuOpen()) {
              <div class="dropdown backdrop-blur">
                <button (click)="logout()">Cerrar sesión</button>
              </div>
            }
          </div>
        } @else {
          <button class="login-btn" (click)="loginRequested.emit()">Iniciar sesión</button>
        }
      </div>
    </header>
  `,
  styles: [`
    .navbar {
      position: sticky; top: 0; z-index: 100;
      display: flex; align-items: center; justify-content: space-between;
      padding: 0.9rem 2.5rem;
      background: linear-gradient(to bottom, rgba(20,20,20,0.95), rgba(20,20,20,0.75));
      backdrop-filter: blur(6px);
    }
    .navbar-left { display: flex; align-items: center; gap: 2.5rem; }
    .brand { display: flex; align-items: center; gap: 0.5rem; font-weight: 700; font-size: 1.2rem; }
    .nav-links { display: flex; gap: 1.6rem; }
    .nav-links a { color: var(--text-secondary); font-size: 0.92rem; transition: color var(--transition-fast); }
    .nav-links a:hover, .nav-links a.active { color: var(--text-primary); }

    .navbar-right { display: flex; align-items: center; gap: 1.2rem; }

    .search-box {
      display: flex; align-items: center;
      background: rgba(0,0,0,0.4);
      border: 1px solid transparent;
      border-radius: var(--radius-sm);
      transition: width var(--transition-base), border-color var(--transition-base);
      width: 40px; overflow: hidden;
    }
    .search-box.expanded { width: 220px; border-color: var(--border-subtle); }
    .search-icon-btn { padding: 0.5rem; color: var(--text-primary); flex-shrink: 0; }
    .search-box input {
      background: transparent; border: none; color: var(--text-primary);
      width: 100%; padding-right: 0.8rem; font-size: 0.9rem;
    }
    .search-box input:focus { outline: none; }

    .login-btn {
      background: var(--accent); color: #fff; font-weight: 600;
      padding: 0.5rem 1.1rem; border-radius: var(--radius-sm);
      font-size: 0.88rem; transition: background var(--transition-fast);
    }
    .login-btn:hover { background: var(--accent-hover); }

    .user-menu { position: relative; }
    .avatar-btn img { width: 34px; height: 34px; border-radius: 50%; object-fit: cover; }
    .avatar-fallback {
      width: 34px; height: 34px; border-radius: 50%;
      background: var(--accent); color: #fff; font-weight: 600; font-size: 0.85rem;
      display: flex; align-items: center; justify-content: center;
    }
    .dropdown {
      position: absolute; right: 0; top: 44px;
      background: var(--bg-surface); border: 1px solid var(--border-subtle);
      border-radius: var(--radius-sm); min-width: 160px;
      display: flex; flex-direction: column; overflow: hidden;
      box-shadow: var(--shadow-elevated);
    }
    .dropdown button { padding: 0.7rem 1rem; text-align: left; font-size: 0.88rem; }
    .dropdown button:hover { background: var(--bg-surface-alt); }

    @media (max-width: 820px) {
      .navbar { padding: .7rem 1rem; backdrop-filter: none; }
      .navbar-left { gap: 0; }
      .navbar-right { gap: .45rem; }
      .nav-links {
        position: fixed; left: 0; right: 0; bottom: 0; z-index: 101;
        display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 0;
        padding: .55rem .2rem max(.55rem, env(safe-area-inset-bottom));
        background: rgba(20,20,20,.97); border-top: 1px solid var(--border-subtle);
        backdrop-filter: blur(8px);
      }
      .nav-links a { min-width: 0; text-align: center; font-size: .72rem; white-space: nowrap; }
      .login-btn { padding: .5rem .7rem; white-space: nowrap; }
      .search-box.expanded {
        position: absolute; left: 1rem; right: 1rem; top: .55rem;
        z-index: 2; width: auto; background: var(--bg-surface);
      }
    }

    @media (max-width: 430px) {
      .brand span { display: none; }
    }
  `],
})
export class NavbarComponent {
  private readonly destroyRef = inject(DestroyRef);
  @Output() loginRequested = new EventEmitter<void>();

  readonly searchExpanded = signal(false);
  readonly menuOpen = signal(false);
  searchQuery = '';
  readonly searchChanges = new Subject<string>();

  constructor(public authService: AuthService, private router: Router) {
    this.searchChanges.pipe(
      map((value) => value.trim()),
      debounceTime(350),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe((query) => {
      if (query.length >= 2) this.router.navigate(['/search'], { queryParams: { q: query } });
    });
  }

  initials(): string {
    const name = this.authService.currentUser()?.name ?? '';
    return name.split(' ').map((n) => n[0]).slice(0, 2).join('').toUpperCase();
  }

  toggleSearch(): void {
    this.searchExpanded.set(!this.searchExpanded());
  }

  onSearchBlur(): void {
    // Pequeño retardo para permitir que el clic en el icono no cierre antes de expandirse
    if (!this.searchQuery) {
      setTimeout(() => this.searchExpanded.set(false), 150);
    }
  }

  submitSearch(): void {
    if (!this.searchQuery.trim()) return;
    this.router.navigate(['/search'], { queryParams: { q: this.searchQuery.trim() } });
  }

  logout(): void {
    this.menuOpen.set(false);
    this.authService.logout();
  }
}

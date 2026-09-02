import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { MovieCardComponent } from '../../shared/components/movie-card/movie-card.component';
import { MovieModalComponent } from '../../shared/components/movie-modal/movie-modal.component';
import { FavoriteService } from '../../core/services/favorite.service';
import { MovieSummary } from '../../core/models/movie.model';

@Component({
  selector: 'app-favorites',
  standalone: true,
  imports: [CommonModule, MovieCardComponent, MovieModalComponent],
  template: `
    <div class="page">
      <h1>Mis favoritos</h1>

      @if (loading()) {
        <div class="grid" aria-label="Cargando favoritos">
          @for (_ of skeletons; track $index) { <div class="poster-skeleton skeleton"></div> }
        </div>
      } @else if (error()) {
        <div class="empty-state" role="alert">
          <h2>No se pudieron cargar tus favoritos</h2>
          <p>{{ error() }}</p>
          <button (click)="load()">Reintentar</button>
        </div>
      } @else if (favoriteService.favorites().length > 0) {
        <div class="grid">
          @for (movie of favoriteService.favorites(); track movie.mediaType + ':' + movie.tmdbId) {
            <app-movie-card [movie]="movie" (opened)="selectedMovie.set($event)" />
          }
        </div>
      } @else {
        <div class="empty-state">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 21s-6.7-4.35-9.3-8.1C.9 10.1 1.7 6.6 4.6 5.1c2.2-1.1 4.6-.4 6 1.3l1.4 1.6 1.4-1.6c1.4-1.7 3.8-2.4 6-1.3 2.9 1.5 3.7 5 1.9 7.8C18.7 16.65 12 21 12 21z"/>
          </svg>
          <h2>Aún no tienes favoritos</h2>
          <p>Explora el catálogo y guarda los títulos que quieras ver más tarde.</p>
          <button (click)="router.navigate(['/movies'])">Explorar catálogo</button>
        </div>
      }
    </div>

    @if (selectedMovie()) {
      <app-movie-modal [tmdbId]="selectedMovie()!.tmdbId" [mediaType]="selectedMovie()!.mediaType"
        (closed)="selectedMovie.set(null)" />
    }
  `,
  styles: [`
    .page { padding: 2rem 2.5rem 3rem; }
    h1 { font-size: 1.8rem; margin-bottom: 1.5rem; }
    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
      gap: 1.2rem;
    }
    .grid app-movie-card { width: 100%; }
    .poster-skeleton { aspect-ratio: 2/3; border-radius: var(--radius-sm); }

    .empty-state {
      display: flex; flex-direction: column; align-items: center; text-align: center;
      gap: 0.6rem; padding: 5rem 1rem; color: var(--text-secondary);
    }
    .empty-state svg { color: var(--accent); margin-bottom: 0.5rem; }
    .empty-state h2 { color: var(--text-primary); margin: 0; font-size: 1.3rem; }
    .empty-state button {
      margin-top: 1rem; background: var(--accent); color: #fff; font-weight: 600;
      padding: 0.7rem 1.4rem; border-radius: var(--radius-sm);
    }
    .empty-state button:hover { background: var(--accent-hover); }
  `],
})
export class FavoritesComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  readonly selectedMovie = signal<MovieSummary | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly skeletons = Array.from({ length: 10 });

  constructor(public favoriteService: FavoriteService, public router: Router) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.favoriteService.loadFavorites().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => this.loading.set(false),
      error: () => {
        this.error.set('Comprueba tu conexión e inténtalo de nuevo.');
        this.loading.set(false);
      },
    });
  }
}

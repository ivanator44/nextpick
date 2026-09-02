import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subscription } from 'rxjs';
import { MovieCardComponent } from '../../shared/components/movie-card/movie-card.component';
import { MovieModalComponent } from '../../shared/components/movie-modal/movie-modal.component';
import { MovieService } from '../../core/services/movie.service';
import { Genre, MovieSummary } from '../../core/models/movie.model';

@Component({
  selector: 'app-movies',
  standalone: true,
  imports: [CommonModule, MovieCardComponent, MovieModalComponent],
  template: `
    <div class="page">
      <div class="page-header">
        <h1>Películas</h1>

        <!-- Selector de género: desplegable simple sin librerías externas -->
        <select [value]="selectedGenre()" (change)="onGenreChange($event)">
          <option value="">Todos los géneros</option>
          @for (genre of genres(); track genre.id) {
            <option [value]="genre.id">{{ genre.name }}</option>
          }
        </select>
      </div>

      @if (loading() && movies().length === 0) {
        <div class="grid" aria-label="Cargando películas">
          @for (_ of skeletons; track $index) { <div class="poster-skeleton skeleton"></div> }
        </div>
      } @else if (error()) {
        <div class="state" role="alert"><p>{{ error() }}</p><button (click)="retry()">Reintentar</button></div>
      } @else if (movies().length === 0) {
        <p class="empty">No se han encontrado títulos con estos filtros.</p>
      } @else {
        <div class="grid">
          @for (movie of movies(); track movie.mediaType + ':' + movie.tmdbId) {
            <app-movie-card [movie]="movie" (opened)="selectedMovie.set($event)" />
          }
        </div>
      }

      @if (!lastPage() && !error()) {
        <button class="load-more" [disabled]="loading()" (click)="loadMore()">
          {{ loading() ? 'Cargando…' : 'Cargar más' }}
        </button>
      }
    </div>

    @if (selectedMovie()) {
      <app-movie-modal [tmdbId]="selectedMovie()!.tmdbId" [mediaType]="selectedMovie()!.mediaType"
        (closed)="selectedMovie.set(null)" />
    }
  `,
  styles: [`
    .page { padding: 2rem 2.5rem 3rem; }
    .page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1.5rem; }
    h1 { font-size: 1.8rem; margin: 0; }
    select {
      background: var(--bg-surface-alt); color: var(--text-primary);
      border: 1px solid var(--border-subtle); border-radius: var(--radius-sm);
      padding: 0.5rem 0.9rem; font-size: 0.9rem;
    }
    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
      gap: 1.2rem;
    }
    .grid app-movie-card { width: 100%; }
    .poster-skeleton { aspect-ratio: 2/3; border-radius: var(--radius-sm); }
    .state { min-height: 40vh; display: grid; place-content: center; text-align: center; color: var(--text-secondary); }
    .state button { margin: auto; padding: .65rem 1rem; border-radius: var(--radius-sm); background: var(--accent); color: #fff; }
    .empty { color: var(--text-secondary); margin-top: 2rem; }
    .load-more {
      display: block; margin: 2rem auto 0;
      background: var(--bg-surface-alt); border: 1px solid var(--border-subtle);
      padding: 0.7rem 1.6rem; border-radius: var(--radius-sm); font-size: 0.9rem;
    }
    .load-more:hover { border-color: var(--accent); }
    .load-more:disabled { opacity: .55; cursor: wait; }
  `],
})
export class MoviesComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  readonly movies = signal<MovieSummary[]>([]);
  readonly genres = signal<Genre[]>([]);
  readonly selectedGenre = signal<number | null>(null);
  readonly selectedMovie = signal<MovieSummary | null>(null);
  readonly lastPage = signal(true);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly skeletons = Array.from({ length: 10 });

  private currentPage = 0;
  private activeLoad?: Subscription;
  constructor(private movieService: MovieService) {}

  ngOnInit(): void {
    this.movieService.getGenres('MOVIE')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((genres) => this.genres.set(genres));
    this.fetch(false);
  }

  onGenreChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.selectedGenre.set(value ? Number(value) : null);
    this.currentPage = 0;
    this.fetch(false);
  }

  loadMore(): void {
    if (this.loading()) return;
    this.currentPage++;
    this.fetch(true);
  }

  retry(): void {
    this.fetch(this.currentPage > 0 && this.movies().length > 0);
  }

  private fetch(append: boolean): void {
    this.activeLoad?.unsubscribe();
    this.loading.set(true);
    this.error.set(null);
    const request$ = this.selectedGenre()
        ? this.movieService.getByGenre('MOVIE', this.selectedGenre()!, this.currentPage)
        : this.movieService.getByType('MOVIE', this.currentPage);

    this.activeLoad = request$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (res) => {
        this.movies.set(append ? [...this.movies(), ...res.content] : res.content);
        this.lastPage.set(res.last);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar las películas.');
        this.loading.set(false);
      },
    });
  }
}

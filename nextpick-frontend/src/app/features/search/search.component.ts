import { Component, DestroyRef, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { catchError, debounceTime, distinctUntilChanged, map, of, switchMap, tap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MovieService } from '../../core/services/movie.service';
import { MovieSummary, PagedResponse } from '../../core/models/movie.model';
import { MovieCardComponent } from '../../shared/components/movie-card/movie-card.component';
import { MovieModalComponent } from '../../shared/components/movie-modal/movie-modal.component';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [CommonModule, MovieCardComponent, MovieModalComponent],
  template: `
    <main class="page">
      <h1>Resultados para “{{ query() }}”</h1>
      @if (loading()) {
        <div class="grid" aria-label="Buscando títulos">
          @for (_ of skeletons; track $index) { <div class="poster-skeleton skeleton"></div> }
        </div>
      } @else if (error()) {
        <div class="state" role="alert"><p>{{ error() }}</p><button (click)="retry()">Reintentar</button></div>
      } @else if (results().length === 0) {
        <p class="state">No se encontraron películas o series para esta búsqueda.</p>
      } @else {
        <div class="grid">
          @for (item of results(); track item.mediaType + ':' + item.tmdbId) {
            <div class="result"><span>{{ item.mediaType === 'MOVIE' ? 'Película' : 'Serie' }}</span>
              <app-movie-card [movie]="item" (opened)="selected.set($event)" />
            </div>
          }
        </div>
      }
    </main>
    @if (selected()) {
      <app-movie-modal [tmdbId]="selected()!.tmdbId" [mediaType]="selected()!.mediaType" (closed)="selected.set(null)" />
    }
  `,
  styles: [`
    .page { padding: 6rem 2.5rem 3rem; }
    h1 { font-size: 1.65rem; }
    .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 1.2rem; }
    .result { position: relative; }
    .result > span { position: absolute; z-index: 3; top: .5rem; left: .5rem; padding: .2rem .45rem;
      border-radius: 4px; background: rgba(0,0,0,.78); font-size: .7rem; }
    app-movie-card { width: 100%; }
    .poster-skeleton { aspect-ratio: 2/3; border-radius: var(--radius-sm); }
    .state { color: var(--text-secondary); padding: 3rem 0; }
    .state button { background: var(--accent); padding: .6rem 1rem; border-radius: var(--radius-sm); }
  `],
})
export class SearchComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly movieService = inject(MovieService);
  private readonly destroyRef = inject(DestroyRef);
  readonly query = signal('');
  readonly results = signal<MovieSummary[]>([]);
  readonly selected = signal<MovieSummary | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly skeletons = Array.from({ length: 10 });
  private readonly emptyPage: PagedResponse<MovieSummary> = {
    content: [], totalPages: 0, totalElements: 0, page: 0, last: true,
  };

  constructor() {
    this.route.queryParamMap.pipe(
      map((params) => (params.get('q') ?? '').trim()),
      debounceTime(250),
      distinctUntilChanged(),
      tap((query) => { this.query.set(query); this.loading.set(query.length > 0); this.error.set(null); }),
      switchMap((query) => query
        ? this.movieService.search(query).pipe(catchError(() => {
            this.error.set('No se pudo completar la búsqueda.');
            return of(this.emptyPage);
          }))
        : of(this.emptyPage)),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe((page) => { this.results.set(page.content); this.loading.set(false); });
  }

  retry(): void {
    const query = this.query();
    if (!query) return;
    this.loading.set(true);
    this.error.set(null);
    this.movieService.search(query).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (page) => { this.results.set(page.content); this.loading.set(false); },
      error: () => { this.error.set('No se pudo completar la búsqueda.'); this.loading.set(false); },
    });
  }
}

import { CommonModule } from '@angular/common';
import { Component, DestroyRef, ElementRef, EventEmitter, HostListener, Input, OnChanges, OnDestroy, Output, ViewChild, inject, signal } from '@angular/core';
import { MediaType, MovieDetail, MovieSummary, Provider } from '../../../core/models/movie.model';
import { MovieService } from '../../../core/services/movie.service';
import { FavoriteService } from '../../../core/services/favorite.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { DomSanitizer } from '@angular/platform-browser';
import { Observable } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MovieCardComponent } from '../movie-card/movie-card.component';

@Component({
  selector: 'app-movie-modal',
  standalone: true,
  imports: [CommonModule, MovieCardComponent],
  template: `
    <div class="overlay backdrop-blur" (click)="close()">
      <div #dialog class="modal" role="dialog" aria-modal="true" aria-labelledby="detail-title"
        tabindex="-1" (click)="$event.stopPropagation()">
        <button class="close-btn" type="button" (click)="close()" aria-label="Cerrar">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 6l12 12M18 6L6 18"/></svg>
        </button>

        @if (loading()) { <div class="modal-state skeleton" aria-label="Cargando detalle"></div> }
        @if (error()) { <div class="modal-state" role="alert"><p>{{ error() }}</p><button (click)="load()">Reintentar</button></div> }

        @if (detail) {
          <div class="backdrop" [style.backgroundImage]="detail.backdropUrl ? 'url(' + detail.backdropUrl + ')' : null">
            <div class="backdrop-gradient"></div>
          </div>

          <div class="content">
            <h2 id="detail-title">{{ detail.title }}</h2>

            <div class="meta">
              @if (detail.rating) { <span class="rating"><svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><path d="M12 2.5l2.9 5.9 6.5.9-4.7 4.6 1.1 6.5-5.8-3-5.8 3 1.1-6.5-4.7-4.6 6.5-.9L12 2.5z"/></svg> {{ detail.rating.toFixed(1) }}</span> }
              @if (detail.releaseYear) { <span>{{ detail.releaseYear }}</span> }
              <span class="badge">{{ detail.mediaType === 'MOVIE' ? 'Película' : 'Serie' }}</span>
            </div>

            <div class="genres">
              @for (genre of detail.genres; track genre.id) {
                <span class="genre-chip">{{ genre.name }}</span>
              }
            </div>

            <p class="synopsis">{{ detail.overview || 'Sin sinopsis disponible.' }}</p>

            @if (detail.trailer) {
              <button class="trailer-btn" type="button" (click)="trailerOpen.set(!trailerOpen())">
                {{ trailerOpen() ? 'Cerrar tráiler' : 'Reproducir tráiler' }}
              </button>
              @if (trailerOpen()) {
                <iframe class="trailer" [src]="trustedTrailerUrl()" title="Tráiler" allow="autoplay; encrypted-media" allowfullscreen></iframe>
              }
            }

            @if (hasProviders()) {
              <section class="providers" aria-labelledby="providers-title">
                <h3 id="providers-title">Disponible en España</h3>
                @for (group of providerGroups(); track group.label) {
                  @if (group.providers.length) {
                    <h4>{{ group.label }}</h4>
                    <div class="provider-list">
                      @for (provider of group.providers; track provider.providerId) {
                        <span class="provider">
                          @if (provider.logoUrl) { <img [src]="provider.logoUrl" alt="" (error)="hideBrokenImage($event)" /> }
                          {{ provider.name }}
                        </span>
                      }
                    </div>
                  }
                }
                <small>Disponibilidad proporcionada por JustWatch.</small>
              </section>
            }

            @if (detail.recommendations.length) {
              <section class="related" aria-labelledby="related-title">
                <h3 id="related-title">Títulos similares</h3>
                <div class="related-list">
                  @for (related of detail.recommendations; track related.mediaType + ':' + related.tmdbId) {
                    <app-movie-card [movie]="related" (opened)="openRelated($event)" />
                  }
                </div>
              </section>
            }

            <button class="fav-btn" type="button" [class.active]="detail.favorite" (click)="toggleFavorite()">
              <svg width="18" height="18" viewBox="0 0 24 24" [attr.fill]="detail.favorite ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                <path d="M12 21s-6.7-4.35-9.3-8.1C.9 10.1 1.7 6.6 4.6 5.1c2.2-1.1 4.6-.4 6 1.3l1.4 1.6 1.4-1.6c1.4-1.7 3.8-2.4 6-1.3 2.9 1.5 3.7 5 1.9 7.8C18.7 16.65 12 21 12 21z"/>
              </svg>
              {{ detail.favorite ? 'En tu lista' : 'Añadir a favoritos' }}
            </button>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .overlay {
      position: fixed; inset: 0; z-index: 1000;
      background: var(--bg-overlay);
      display: flex; align-items: center; justify-content: center;
      padding: 1.5rem;
    }
    .modal {
      background: var(--bg-surface);
      border-radius: var(--radius-lg);
      max-width: 640px; width: 100%;
      max-height: 90vh; overflow-y: auto;
      position: relative;
      box-shadow: var(--shadow-elevated);
    }
    .modal-state { min-height: 360px; display: grid; place-content: center; padding: 2rem; text-align: center; }
    .close-btn {
      position: absolute; top: 1rem; right: 1rem; z-index: 2;
      width: 32px; height: 32px; border-radius: 50%;
      background: rgba(0,0,0,0.6); color: #fff;
    }
    .backdrop { height: 260px; background-size: cover; background-position: center; position: relative; border-radius: var(--radius-lg) var(--radius-lg) 0 0; }
    .backdrop-gradient { position: absolute; inset: 0; background: linear-gradient(to top, var(--bg-surface), transparent 70%); }

    .content { padding: 0 2rem 2rem; margin-top: -2rem; position: relative; }
    h2 { font-size: 1.6rem; margin: 0 0 0.5rem; }
    .meta { display: flex; align-items: center; gap: 0.9rem; color: var(--text-secondary); font-size: 0.9rem; margin-bottom: 0.8rem; }
    .rating { color: #f5c518; display: inline-flex; align-items: center; gap: .2rem; }
    .badge { border: 1px solid var(--border-subtle); padding: 0.1rem 0.6rem; border-radius: 4px; font-size: 0.75rem; }

    .genres { display: flex; gap: 0.5rem; flex-wrap: wrap; margin-bottom: 1rem; }
    .genre-chip { background: var(--bg-surface-alt); padding: 0.25rem 0.7rem; border-radius: 20px; font-size: 0.78rem; color: var(--text-secondary); }

    .synopsis { line-height: 1.6; color: var(--text-primary); margin-bottom: 1.5rem; }
    .trailer-btn { padding: .65rem 1rem; background: #fff; color: #111; border-radius: var(--radius-sm); margin-bottom: 1rem; }
    .trailer { width: 100%; aspect-ratio: 16/9; border: 0; border-radius: var(--radius-sm); margin-bottom: 1rem; }
    .providers { margin: 1rem 0 1.5rem; }
    .providers h3 { font-size: 1rem; }
    .providers h4 { margin: .8rem 0 .45rem; color: var(--text-secondary); font-size: .78rem; text-transform: uppercase; }
    .provider-list { display: flex; flex-wrap: wrap; gap: .6rem; }
    .provider { display: flex; align-items: center; gap: .4rem; background: var(--bg-surface-alt); padding: .35rem .6rem; border-radius: var(--radius-sm); font-size: .8rem; }
    .provider img { width: 26px; height: 26px; border-radius: 5px; }
    .providers small { display: block; color: var(--text-secondary); margin-top: .7rem; }
    .related { margin: 1.25rem 0; }
    .related h3 { font-size: 1rem; }
    .related-list { display: flex; gap: .8rem; overflow-x: auto; padding: .2rem .2rem .8rem; }
    .related-list app-movie-card { flex: 0 0 auto; }

    .fav-btn {
      display: flex; align-items: center; gap: 0.6rem;
      border: 1px solid var(--border-subtle);
      padding: 0.65rem 1.2rem; border-radius: var(--radius-sm);
      font-weight: 600; font-size: 0.9rem;
      transition: all var(--transition-fast);
    }
    .fav-btn:hover { border-color: var(--accent); }
    .fav-btn.active { background: var(--accent); border-color: var(--accent); color: #fff; }
  `],
})
export class MovieModalComponent implements OnChanges, OnDestroy {
  private readonly destroyRef = inject(DestroyRef);
  @ViewChild('dialog') dialog?: ElementRef<HTMLElement>;
  @Input({ required: true }) tmdbId!: number;
  @Input({ required: true }) mediaType!: MediaType;
  @Output() closed = new EventEmitter<void>();

  detail: MovieDetail | null = null;
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly trailerOpen = signal(false);
  readonly placeholder = 'assets/poster-placeholder.svg';
  private readonly previouslyFocused = document.activeElement as HTMLElement | null;

  constructor(
    private movieService: MovieService,
    private favoriteService: FavoriteService,
    private authService: AuthService,
    private toast: ToastService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnChanges(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.movieService.getDetail(this.tmdbId, this.mediaType)
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (detail) => {
        this.detail = detail;
        this.loading.set(false);
        queueMicrotask(() => this.dialog?.nativeElement.focus());
      },
      error: () => {
        this.error.set('No se pudo cargar la información del título.');
        this.loading.set(false);
      },
    });
  }

  openRelated(movie: MovieSummary): void {
    this.tmdbId = movie.tmdbId;
    this.mediaType = movie.mediaType;
    this.detail = null;
    this.trailerOpen.set(false);
    this.dialog?.nativeElement.scrollTo({ top: 0 });
    this.load();
  }

  toggleFavorite(): void {
    if (!this.detail) return;

    if (!this.authService.isAuthenticated()) {
      this.toast.show('Inicia sesión para guardar títulos en favoritos.', 'info');
      return;
    }

    const action$: Observable<unknown> = this.detail.favorite
      ? this.favoriteService.remove(this.detail.tmdbId, this.detail.mediaType)
      : this.favoriteService.add(this.detail.tmdbId, this.detail.mediaType);

    action$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      if (this.detail) this.detail = { ...this.detail, favorite: !this.detail.favorite };
    });
  }

  trustedTrailerUrl() {
    return this.sanitizer.bypassSecurityTrustResourceUrl(this.detail?.trailer?.url ?? '');
  }

  hasProviders(): boolean {
    return this.providerGroups().some((group) => group.providers.length > 0);
  }

  providerGroups(): Array<{ label: string; providers: Provider[] }> {
    if (!this.detail) return [];
    return [
      { label: 'Suscripción', providers: this.detail.providers.streaming },
      { label: 'Gratis', providers: this.detail.providers.free },
      { label: 'Alquiler', providers: this.detail.providers.rent },
      { label: 'Compra', providers: this.detail.providers.buy },
    ];
  }

  hideBrokenImage(event: Event): void {
    (event.target as HTMLImageElement).hidden = true;
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
      'button:not([disabled]), a[href], input:not([disabled]), select:not([disabled]), [tabindex]:not([tabindex="-1"])'
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

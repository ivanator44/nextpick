import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HeroComponent } from '../../shared/components/hero/hero.component';
import { CarouselComponent } from '../../shared/components/carousel/carousel.component';
import { MovieModalComponent } from '../../shared/components/movie-modal/movie-modal.component';
import { MovieService } from '../../core/services/movie.service';
import { AuthService } from '../../core/services/auth.service';
import { MovieDetail, MovieSummary } from '../../core/models/movie.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, HeroComponent, CarouselComponent, MovieModalComponent],
  template: `
    @if (loading()) { <div class="hero-skeleton skeleton" aria-label="Cargando catálogo"></div> }
    @if (error()) {
      <div class="home-error" role="alert"><p>{{ error() }}</p><button (click)="load()">Reintentar</button></div>
    } @else {
    <app-hero [movie]="heroMovie()" (infoRequested)="openModal($event)" />

    <div class="rows">
      <app-carousel
        title="Tendencias ahora"
        [movies]="trending()"
        (opened)="openModal($event)"
      />

      <!-- Fila de recomendaciones personalizadas: bloqueada si no hay sesión -->
      <app-carousel
        title="Para ti"
        [movies]="forYou()"
        (opened)="openModal($event)"
      />

      <app-carousel title="Mejor valoradas" [movies]="top10()" (opened)="openModal($event)" />
      <app-carousel title="Populares" [movies]="animes()" (opened)="openModal($event)" />
    </div>
    }

    @if (selectedMovie()) {
      <app-movie-modal [tmdbId]="selectedMovie()!.tmdbId" [mediaType]="selectedMovie()!.mediaType"
        (closed)="selectedMovie.set(null)" />
    }
  `,
  styles: [`
    .rows { padding-bottom: 3rem; }
    .hero-skeleton { height: 78vh; }
    .home-error { min-height: 50vh; display: grid; place-content: center; text-align: center; }
    .home-error button { margin: auto; padding: .7rem 1.2rem; background: var(--accent); border-radius: var(--radius-sm); }
  `],
})
export class HomeComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  readonly heroMovie = signal<MovieDetail | null>(null);
  readonly trending = signal<MovieSummary[]>([]);
  readonly forYou = signal<MovieSummary[]>([]);
  readonly top10 = signal<MovieSummary[]>([]);
  readonly animes = signal<MovieSummary[]>([]);
  readonly selectedMovie = signal<MovieSummary | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  constructor(private movieService: MovieService, public authService: AuthService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.movieService.getHome().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (home) => {
        this.heroMovie.set(home.hero);
        this.trending.set(home.trending);
        this.forYou.set(home.forYou);
        this.top10.set(home.topRated);
        this.animes.set(home.popular);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el catálogo. Comprueba la configuración de TMDB.');
        this.loading.set(false);
      },
    });
  }

  openModal(movie: MovieSummary): void {
    this.selectedMovie.set(movie);
  }
}

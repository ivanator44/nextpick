import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MovieDetail } from '../../../core/models/movie.model';

@Component({
  selector: 'app-hero',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (movie) {
      <section class="hero" [style.backgroundImage]="'url(' + (movie.backdropUrl || movie.posterUrl || '') + ')'">
        <div class="hero-gradient"></div>
        <div class="hero-content">
          <h1>{{ movie.title }}</h1>
          <div class="hero-meta">
            @if (movie.rating) {
              <span class="rating"><svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><path d="M12 2.5l2.9 5.9 6.5.9-4.7 4.6 1.1 6.5-5.8-3-5.8 3 1.1-6.5-4.7-4.6 6.5-.9L12 2.5z"/></svg> {{ movie.rating.toFixed(1) }}</span>
            }
            @if (movie.releaseYear) {
              <span>{{ movie.releaseYear }}</span>
            }
          </div>
          @if (movie.overview) { <p class="overview">{{ movie.overview }}</p> }
          <div class="hero-actions">
            @if (movie.trailer) {
              <a class="btn-primary" [href]="movie.trailer.url" target="_blank" rel="noopener noreferrer">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
                Ver tráiler
              </a>
            }
            <button class="btn-secondary" (click)="infoRequested.emit(movie)">Más información</button>
          </div>
        </div>
      </section>
    }
  `,
  styles: [`
    .hero {
      position: relative;
      height: 78vh;
      background-size: cover;
      background-position: center 20%;
      display: flex; align-items: flex-end;
    }
    .hero-gradient {
      position: absolute; inset: 0;
      background:
        linear-gradient(to top, var(--bg-primary) 5%, transparent 55%),
        linear-gradient(to right, rgba(20,20,20,0.85) 10%, transparent 60%);
    }
    .hero-content { position: relative; padding: 0 2.5rem 4rem; max-width: 640px; }
    h1 { font-size: 3rem; margin: 0 0 0.8rem; font-weight: 700; line-height: 1.1; }
    .hero-meta { display: flex; gap: 1rem; color: var(--text-secondary); margin-bottom: 1.2rem; }
    .overview { line-height: 1.55; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
    .rating { color: #f5c518; }

    .hero-actions { display: flex; gap: 0.9rem; }
    .btn-primary, .btn-secondary {
      display: flex; align-items: center; gap: 0.5rem;
      padding: 0.75rem 1.4rem; border-radius: var(--radius-sm);
      font-weight: 600; font-size: 0.95rem;
      transition: background var(--transition-fast), opacity var(--transition-fast);
    }
    .btn-primary { background: #fff; color: #000; }
    .btn-primary:hover { background: #d9d9d9; }
    .btn-secondary { background: rgba(115,115,115,0.5); color: #fff; }
    .btn-secondary:hover { background: rgba(115,115,115,0.7); }
  `],
})
export class HeroComponent {
  @Input() movie: MovieDetail | null = null;
  @Output() infoRequested = new EventEmitter<MovieDetail>();
}

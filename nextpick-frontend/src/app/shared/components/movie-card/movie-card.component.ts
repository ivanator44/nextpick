import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MovieSummary } from '../../../core/models/movie.model';

@Component({
  selector: 'app-movie-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button type="button" class="card" (click)="opened.emit(movie)" [attr.aria-label]="'Abrir detalles de ' + movie.title">
      <img [src]="movie.posterUrl || placeholder" [alt]="movie.title" loading="lazy" (error)="imageFailed($event)" />

      <div class="overlay">
        <span class="title">{{ movie.title }}</span>
        <div class="meta">
          @if (movie.rating) {
            <span class="rating"><svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><path d="M12 2.5l2.9 5.9 6.5.9-4.7 4.6 1.1 6.5-5.8-3-5.8 3 1.1-6.5-4.7-4.6 6.5-.9L12 2.5z"/></svg> {{ movie.rating.toFixed(1) }}</span>
          }
          @if (movie.releaseYear) {
            <span class="year">{{ movie.releaseYear }}</span>
          }
        </div>
      </div>
    </button>
  `,
  styles: [`
    .card {
      position: relative;
      flex-shrink: 0;
      width: 180px;
      aspect-ratio: 2 / 3;
      border-radius: var(--radius-sm);
      overflow: hidden;
      cursor: pointer;
      transition: transform var(--transition-base);
    }
    .card:hover { transform: scale(1.06); z-index: 2; }
    .card:focus-visible { outline: 3px solid var(--accent); outline-offset: 3px; }
    .card img { width: 100%; height: 100%; object-fit: cover; display: block; }

    .overlay {
      position: absolute; inset: 0;
      background: linear-gradient(to top, rgba(0,0,0,0.9) 0%, transparent 55%);
      display: flex; flex-direction: column; justify-content: flex-end;
      padding: 0.7rem;
      opacity: 0;
      transition: opacity var(--transition-fast);
    }
    .card:hover .overlay { opacity: 1; }

    .title { font-size: 0.85rem; font-weight: 600; line-height: 1.2; }
    .meta { display: flex; gap: 0.6rem; margin-top: 0.3rem; font-size: 0.75rem; color: var(--text-secondary); }
    .rating { color: #f5c518; display: inline-flex; align-items: center; gap: .2rem; }
  `],
})
export class MovieCardComponent {
  readonly placeholder = 'assets/poster-placeholder.svg';
  @Input({ required: true }) movie!: MovieSummary;
  @Output() opened = new EventEmitter<MovieSummary>();

  imageFailed(event: Event): void {
    const image = event.target as HTMLImageElement;
    if (!image.src.endsWith(this.placeholder)) image.src = this.placeholder;
  }
}

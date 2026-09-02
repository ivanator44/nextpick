import { CommonModule } from '@angular/common';
import { Component, ElementRef, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { MovieSummary } from '../../../core/models/movie.model';
import { MovieCardComponent } from '../movie-card/movie-card.component';

@Component({
  selector: 'app-carousel',
  standalone: true,
  imports: [CommonModule, MovieCardComponent],
  template: `
    <section class="carousel-section">
      <h3>{{ title }}</h3>

      <div class="carousel-wrapper">
        <!-- Sistema de bloqueo: si "locked" es true, se desenfoca el contenido y se
             muestra un candado invitando a iniciar sesión (recomendaciones personalizadas) -->
        <div class="carousel-track" [class.blurred]="locked" #track>
          @for (movie of movies; track movie.mediaType + ':' + movie.tmdbId) {
            <app-movie-card [movie]="movie" (opened)="opened.emit($event)" />
          }
        </div>

        @if (locked) {
          <div class="lock-overlay backdrop-blur" (click)="loginRequested.emit()">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="5" y="11" width="14" height="9" rx="2" />
              <path d="M8 11V7a4 4 0 0 1 8 0v4" />
            </svg>
            <span>Inicia sesión para ver tus recomendaciones</span>
          </div>
        } @else {
          <button class="scroll-btn left" (click)="scroll(track, -600)" aria-label="Anterior">‹</button>
          <button class="scroll-btn right" (click)="scroll(track, 600)" aria-label="Siguiente">›</button>
        }
      </div>
    </section>
  `,
  styles: [`
    .carousel-section { margin: 2rem 0; }
    h3 { font-size: 1.1rem; margin: 0 0 0.8rem 2.5rem; }

    .carousel-wrapper { position: relative; }
    .carousel-track {
      display: flex; gap: 0.9rem;
      overflow-x: auto; scroll-behavior: smooth;
      padding: 0.3rem 2.5rem 0.6rem;
      scrollbar-width: none;
    }
    .carousel-track::-webkit-scrollbar { display: none; }
    .carousel-track.blurred { filter: blur(6px); pointer-events: none; user-select: none; }

    .scroll-btn {
      position: absolute; top: 0; bottom: 0.6rem;
      width: 48px;
      background: linear-gradient(to right, rgba(20,20,20,0.9), transparent);
      color: var(--text-primary); font-size: 2rem;
      display: flex; align-items: center; justify-content: center;
      opacity: 0; transition: opacity var(--transition-fast);
    }
    .carousel-wrapper:hover .scroll-btn { opacity: 1; }
    .scroll-btn.left { left: 0; }
    .scroll-btn.right {
      right: 0;
      background: linear-gradient(to left, rgba(20,20,20,0.9), transparent);
    }

    .lock-overlay {
      position: absolute; inset: 0 2.5rem;
      display: flex; flex-direction: column; align-items: center; justify-content: center;
      gap: 0.6rem; cursor: pointer;
      color: var(--text-primary); font-size: 0.9rem; text-align: center;
    }
  `],
})
export class CarouselComponent {
  @Input({ required: true }) title = '';
  @Input({ required: true }) movies: MovieSummary[] = [];
  @Input() locked = false; // true cuando la fila requiere autenticación
  @Output() opened = new EventEmitter<MovieSummary>();
  @Output() loginRequested = new EventEmitter<void>();

  scroll(track: HTMLElement, amount: number): void {
    track.scrollBy({ left: amount, behavior: 'smooth' });
  }
}

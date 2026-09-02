import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { FavoriteService } from '../../core/services/favorite.service';
import { MovieService } from '../../core/services/movie.service';
import { ToastService } from '../../core/services/toast.service';
import { MovieDetail } from '../../core/models/movie.model';
import { MoviesComponent } from '../../features/movies/movies.component';
import { AuthModalComponent } from './auth-modal/auth-modal.component';
import { MovieModalComponent } from './movie-modal/movie-modal.component';

describe('componentes de finalización de producto', () => {
  afterEach(() => TestBed.resetTestingModule());

  it('expone el diálogo de autenticación y permite cerrarlo con Escape', () => {
    TestBed.configureTestingModule({
      imports: [AuthModalComponent],
      providers: [{ provide: AuthService, useValue: {} }],
    });
    const fixture = TestBed.createComponent(AuthModalComponent);
    fixture.detectChanges();
    spyOn(fixture.componentInstance.closed, 'emit');

    const dialog = fixture.nativeElement.querySelector('[role="dialog"]') as HTMLElement;
    expect(dialog.getAttribute('aria-modal')).toBe('true');
    fixture.componentInstance.handleKeyboard(new KeyboardEvent('keydown', { key: 'Escape' }));
    expect(fixture.componentInstance.closed.emit).toHaveBeenCalled();
  });

  it('muestra títulos similares y todos los modos de proveedor', () => {
    const related = {
      tmdbId: 2, mediaType: 'MOVIE' as const, title: 'Relacionada', overview: null,
      posterUrl: null, backdropUrl: null, rating: 7, releaseYear: 2020, genreIds: [18],
    };
    const detail: MovieDetail = {
      tmdbId: 1, mediaType: 'MOVIE', title: 'Principal', overview: 'Resumen',
      posterUrl: null, backdropUrl: null, rating: 8, releaseYear: 2024, genreIds: [18],
      genres: [{ id: 18, name: 'Drama' }], trailer: null, favorite: false,
      providers: {
        region: 'ES', link: null,
        streaming: [{ providerId: 1, name: 'Stream', logoUrl: null }],
        free: [{ providerId: 2, name: 'Gratis', logoUrl: null }],
        rent: [{ providerId: 3, name: 'Alquiler', logoUrl: null }],
        buy: [{ providerId: 4, name: 'Compra', logoUrl: null }],
      },
      recommendations: [related],
    };
    TestBed.configureTestingModule({
      imports: [MovieModalComponent],
      providers: [
        { provide: MovieService, useValue: { getDetail: () => of(detail) } },
        { provide: FavoriteService, useValue: { add: () => of(related), remove: () => of(undefined) } },
        { provide: AuthService, useValue: { isAuthenticated: () => true } },
        { provide: ToastService, useValue: { show: () => undefined } },
      ],
    });
    const fixture = TestBed.createComponent(MovieModalComponent);
    fixture.componentRef.setInput('tmdbId', 1);
    fixture.componentRef.setInput('mediaType', 'MOVIE');
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Títulos similares');
    expect(text).toContain('Relacionada');
    expect(text).toContain('Suscripción');
    expect(text).toContain('Gratis');
    expect(text).toContain('Alquiler');
    expect(text).toContain('Compra');
  });

  it('muestra un error recuperable cuando falla el listado de películas', () => {
    TestBed.configureTestingModule({
      imports: [MoviesComponent],
      providers: [{
        provide: MovieService,
        useValue: {
          getGenres: () => of([]),
          getByType: () => throwError(() => new Error('network')),
        },
      }],
    });
    const fixture = TestBed.createComponent(MoviesComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('No se pudieron cargar las películas');
    expect(fixture.nativeElement.querySelector('button').textContent).toContain('Reintentar');
  });
});

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { FavoriteService } from './favorite.service';
import { MovieSummary } from '../models/movie.model';
import { environment } from '../../../environments/environment';

describe('FavoriteService', () => {
  let service: FavoriteService;
  let http: HttpTestingController;
  const movie: MovieSummary = {
    tmdbId: 42, mediaType: 'MOVIE', title: 'Título', overview: null,
    posterUrl: null, backdropUrl: null, rating: 8, releaseYear: 2024, genreIds: [18],
  };

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(FavoriteService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('updates every local view immediately after adding and removing a composite favorite', () => {
    service.add(42, 'MOVIE').subscribe();
    http.expectOne(`${environment.apiUrl}/favorites/MOVIE/42`).flush(movie);
    expect(service.isFavorite(42, 'MOVIE')).toBeTrue();
    expect(service.isFavorite(42, 'SERIES')).toBeFalse();
    expect(service.favorites()).toEqual([movie]);

    service.remove(42, 'MOVIE').subscribe();
    http.expectOne(`${environment.apiUrl}/favorites/MOVIE/42`).flush(null);
    expect(service.isFavorite(42, 'MOVIE')).toBeFalse();
    expect(service.favorites()).toEqual([]);
  });
});

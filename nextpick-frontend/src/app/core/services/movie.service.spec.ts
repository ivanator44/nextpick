import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { MovieService } from './movie.service';
import { environment } from '../../../environments/environment';

describe('MovieService', () => {
  let service: MovieService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(MovieService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('keeps movie discovery separate from TV discovery', () => {
    service.getByType('MOVIE').subscribe();
    const movieRequest = http.expectOne(`${environment.apiUrl}/catalog/discover/MOVIE?page=0`);
    expect(movieRequest.request.method).toBe('GET');
    movieRequest.flush({ content: [] });

    service.getByType('SERIES').subscribe();
    const seriesRequest = http.expectOne(`${environment.apiUrl}/catalog/discover/SERIES?page=0`);
    expect(seriesRequest.request.url).toContain('/SERIES');
    seriesRequest.flush({ content: [] });
  });

  it('uses the unified backend search and never TMDB directly', () => {
    service.search('Dune').subscribe();
    const request = http.expectOne(`${environment.apiUrl}/catalog/search?query=Dune&page=0`);
    expect(request.request.url).not.toContain('themoviedb.org');
    request.flush({ content: [], totalPages: 0, totalElements: 0, page: 0, last: true });
  });
});

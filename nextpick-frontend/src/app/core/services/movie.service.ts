import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Genre, MediaType, MovieDetail, MovieSummary, PagedResponse } from '../models/movie.model';
import { HomeCatalog } from '../models/catalog.model';

@Injectable({ providedIn: 'root' })
export class MovieService {
  private readonly baseUrl = `${environment.apiUrl}/catalog`;

  constructor(private http: HttpClient) {}

  getByType(type: MediaType, page = 0, size = 20) {
    const params = new HttpParams().set('page', page);
    return this.http.get<PagedResponse<MovieSummary>>(`${this.baseUrl}/discover/${type}`, { params });
  }

  search(query: string, page = 0, size = 20) {
    const params = new HttpParams().set('query', query).set('page', page);
    return this.http.get<PagedResponse<MovieSummary>>(`${this.baseUrl}/search`, { params });
  }

  getHome() {
    return this.http.get<HomeCatalog>(`${this.baseUrl}/home`);
  }

  getByGenre(type: MediaType, genreId: number, page = 0) {
    const params = new HttpParams().set('page', page).set('genreId', genreId);
    return this.http.get<PagedResponse<MovieSummary>>(`${this.baseUrl}/discover/${type}`, { params });
  }

  getDetail(tmdbId: number, mediaType: MediaType) {
    return this.http.get<MovieDetail>(`${this.baseUrl}/${mediaType}/${tmdbId}`);
  }

  getGenres(mediaType: MediaType) {
    return this.http.get<Genre[]>(`${this.baseUrl}/genres/${mediaType}`);
  }

  getRecommendations() {
    return this.http.get<MovieSummary[]>(`${this.baseUrl}/recommendations`);
  }
}

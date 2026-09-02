import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MediaType, MovieSummary } from '../models/movie.model';
import { mediaKey } from '../models/catalog.model';

@Injectable({ providedIn: 'root' })
export class FavoriteService {
  private readonly baseUrl = `${environment.apiUrl}/favorites`;

  // Lista completa para el grid de /favorites
  private readonly favoritesSignal = signal<MovieSummary[]>([]);
  readonly favorites = this.favoritesSignal.asReadonly();

  // Set de IDs para que cualquier movie-card compruebe pertenencia en O(1)
  private readonly favoriteIdsSignal = signal<Set<string>>(new Set());
  readonly favoriteIds = this.favoriteIdsSignal.asReadonly();

  constructor(private http: HttpClient) {}

  loadFavorites() {
    return this.http.get<MovieSummary[]>(this.baseUrl).pipe(
      tap((favorites) => {
        this.favoritesSignal.set(favorites);
        this.favoriteIdsSignal.set(new Set(favorites.map(mediaKey)));
      })
    );
  }

  isFavorite(tmdbId: number, mediaType: MediaType): boolean {
    return this.favoriteIdsSignal().has(`${mediaType}:${tmdbId}`);
  }

  add(tmdbId: number, mediaType: MediaType) {
    return this.http.post<MovieSummary>(`${this.baseUrl}/${mediaType}/${tmdbId}`, {}).pipe(
      tap((favorite) => {
        const updated = new Set(this.favoriteIdsSignal());
        updated.add(mediaKey(favorite));
        this.favoriteIdsSignal.set(updated);
        this.favoritesSignal.update((items) => [favorite, ...items.filter((item) => mediaKey(item) !== mediaKey(favorite))]);
      })
    );
  }

  remove(tmdbId: number, mediaType: MediaType) {
    const key = `${mediaType}:${tmdbId}`;
    return this.http.delete<void>(`${this.baseUrl}/${mediaType}/${tmdbId}`).pipe(
      tap(() => {
        const updated = new Set(this.favoriteIdsSignal());
        updated.delete(key);
        this.favoriteIdsSignal.set(updated);
        this.favoritesSignal.set(this.favoritesSignal().filter((item) => mediaKey(item) !== key));
      })
    );
  }
}

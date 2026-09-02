export type MediaType = 'MOVIE' | 'SERIES';

export interface MovieSummary {
  tmdbId: number;
  mediaType: MediaType;
  title: string;
  overview: string | null;
  posterUrl: string | null;
  backdropUrl: string | null;
  rating: number | null;
  releaseYear: number | null;
  genreIds: number[];
}

export interface MovieDetail extends Omit<MovieSummary, never> {
  genres: Genre[];
  trailer: Trailer | null;
  providers: WatchProviders;
  recommendations: MovieSummary[];
  favorite: boolean;
}

export interface Genre { id: number; name: string; }
export interface Trailer { key: string; name: string; url: string; }
export interface Provider { providerId: number; name: string; logoUrl: string | null; }
export interface WatchProviders {
  region: string;
  link: string | null;
  streaming: Provider[];
  free: Provider[];
  rent: Provider[];
  buy: Provider[];
}

export interface PagedResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  page: number;
  last: boolean;
}

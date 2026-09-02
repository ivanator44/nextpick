import { MovieDetail, MovieSummary } from './movie.model';

export interface HomeCatalog {
  hero: MovieDetail | null;
  trending: MovieSummary[];
  popular: MovieSummary[];
  topRated: MovieSummary[];
  forYou: MovieSummary[];
}

export function mediaKey(item: Pick<MovieSummary, 'tmdbId' | 'mediaType'>): string {
  return `${item.mediaType}:${item.tmdbId}`;
}

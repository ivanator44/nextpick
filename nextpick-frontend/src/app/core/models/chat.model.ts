export interface ChatConversationSummary {
  id: number;
  title: string;
  updatedAt: string;
}

export interface ChatMessage {
  id?: number;
  sender: 'USER' | 'ASSISTANT';
  content: string;
  createdAt?: string;
  streaming?: boolean; // true mientras llega el texto por SSE, para animar el "typing"
  references?: ChatReference[];
  error?: boolean;
}

export interface ChatReference {
  tmdbId: number;
  mediaType: 'MOVIE' | 'SERIES';
  title: string;
  posterUrl: string | null;
  year: number | null;
  rating: number | null;
}

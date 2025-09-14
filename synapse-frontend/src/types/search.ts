export interface SearchRequest {
  query: string;
  language?: string;
  limit?: number;
  offset?: number;
  filters?: SearchFilters;
}

export interface SearchFilters {
  documentTypes?: string[];
  sources?: string[];
  dateRange?: {
    start: string;
    end: string;
  };
  languages?: string[];
}

export interface SearchResult {
  id: string;
  title: string;
  content: string;
  score: number;
  documentId: string;
  language: string;
  source: string;
  createdAt: string;
  highlights?: string[];
}

export interface SearchResponse {
  results: SearchResult[];
  totalCount: number;
  query: string;
  language: string;
  processingTime: number;
}

export interface SearchHistory {
  id: string;
  query: string;
  language: string;
  timestamp: string;
  resultCount: number;
}

export interface SavedSearch {
  id: string;
  name: string;
  query: string;
  filters: SearchFilters;
  createdAt: string;
}
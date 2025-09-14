import apiClient from './api';
import { SearchRequest, SearchResponse, SearchHistory, SavedSearch } from '../types/search';

export const searchApi = {
  search: async (request: SearchRequest): Promise<SearchResponse> => {
    const response = await apiClient.post('/search', request);
    return response.data;
  },

  getSearchHistory: async (): Promise<SearchHistory[]> => {
    const response = await apiClient.get('/search/history');
    return response.data;
  },

  getSavedSearches: async (): Promise<SavedSearch[]> => {
    const response = await apiClient.get('/search/saved');
    return response.data;
  },

  saveSearch: async (search: Omit<SavedSearch, 'id' | 'createdAt'>): Promise<SavedSearch> => {
    const response = await apiClient.post('/search/saved', search);
    return response.data;
  },

  deleteSavedSearch: async (id: string): Promise<void> => {
    await apiClient.delete(`/search/saved/${id}`);
  },

  provideFeedback: async (searchId: string, resultId: string, feedback: 'helpful' | 'not_helpful'): Promise<void> => {
    await apiClient.post('/search/feedback', {
      searchId,
      resultId,
      feedback
    });
  },
};
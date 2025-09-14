import { apiClient } from './apiClient';
import { QARequest, QAResponse, Conversation, FeedbackRequest } from '../types/qa';

export const qaApi = {
  askQuestion: async (request: QARequest): Promise<QAResponse> => {
    const response = await apiClient.post('/api/qa', request);
    return response.data;
  },

  getConversationHistory: async (): Promise<Conversation[]> => {
    const response = await apiClient.get('/api/qa/conversations');
    return response.data;
  },

  getConversation: async (conversationId: string): Promise<Conversation> => {
    const response = await apiClient.get(`/api/qa/conversations/${conversationId}`);
    return response.data;
  },

  createConversation: async (): Promise<Conversation> => {
    const response = await apiClient.post('/api/qa/conversations');
    return response.data;
  },

  deleteConversation: async (conversationId: string): Promise<void> => {
    await apiClient.delete(`/api/qa/conversations/${conversationId}`);
  },

  provideFeedback: async (feedback: FeedbackRequest): Promise<void> => {
    await apiClient.post('/api/qa/feedback', feedback);
  },

  bookmarkMessage: async (messageId: string): Promise<void> => {
    await apiClient.post(`/api/qa/messages/${messageId}/bookmark`);
  },

  getSearchSuggestions: async (query: string): Promise<string[]> => {
    const response = await apiClient.get('/api/qa/suggestions', {
      params: { q: query }
    });
    return response.data;
  },

  getAnalytics: async (timeRange?: string): Promise<any> => {
    const response = await apiClient.get('/api/qa/analytics', {
      params: { timeRange }
    });
    return response.data;
  },

  exportConversation: async (conversationId: string, format: 'pdf' | 'txt' | 'json'): Promise<Blob> => {
    const response = await apiClient.get(`/api/qa/conversations/${conversationId}/export`, {
      params: { format },
      responseType: 'blob'
    });
    return response.data;
  }
};
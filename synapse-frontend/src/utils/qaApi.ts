import apiClient from './api';
import { QARequest, QAResponse, Conversation } from '../types/qa';

export const qaApi = {
  askQuestion: async (request: QARequest): Promise<QAResponse> => {
    const response = await apiClient.post('/qa', request);
    return response.data;
  },

  getConversations: async (): Promise<Conversation[]> => {
    const response = await apiClient.get('/qa/conversations');
    return response.data;
  },

  getConversation: async (id: string): Promise<Conversation> => {
    const response = await apiClient.get(`/qa/conversations/${id}`);
    return response.data;
  },

  deleteConversation: async (id: string): Promise<void> => {
    await apiClient.delete(`/qa/conversations/${id}`);
  },

  bookmarkConversation: async (id: string, bookmarked: boolean): Promise<void> => {
    await apiClient.put(`/qa/conversations/${id}/bookmark`, { bookmarked });
  },

  provideFeedback: async (messageId: string, feedback: 'helpful' | 'not_helpful'): Promise<void> => {
    await apiClient.post('/qa/feedback', { messageId, feedback });
  },
};
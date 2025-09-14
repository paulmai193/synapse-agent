import apiClient from './api';
import { Document, DocumentUploadRequest } from '../types/document';

export const documentApi = {
  uploadDocument: async (file: File, metadata: DocumentUploadRequest) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', metadata.title);
    formData.append('visibility', metadata.visibility);
    
    if (metadata.departmentIds) {
      metadata.departmentIds.forEach(id => formData.append('departmentIds', id));
    }
    
    if (metadata.tags) {
      metadata.tags.forEach(tag => formData.append('tags', tag));
    }

    const response = await apiClient.post('/documents/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  getDocuments: async (page = 0, size = 10) => {
    const response = await apiClient.get(`/documents?page=${page}&size=${size}`);
    return response.data;
  },

  getDocument: async (id: string) => {
    const response = await apiClient.get(`/documents/${id}`);
    return response.data;
  },

  updateDocumentStatus: async (id: string, status: string) => {
    const response = await apiClient.put(`/documents/${id}/status`, { status });
    return response.data;
  },

  deleteDocument: async (id: string) => {
    const response = await apiClient.delete(`/documents/${id}`);
    return response.data;
  },
};
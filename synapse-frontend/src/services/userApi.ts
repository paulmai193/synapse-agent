import { apiClient } from './apiClient';
import { User, CreateUserRequest, UpdateUserRequest, BulkUpdateRequest } from '../types/user';

export const userApi = {
  getUsers: async (params?: { page?: number; size?: number; search?: string }) => {
    const response = await apiClient.get('/api/users', { params });
    return response.data;
  },

  getUser: async (userId: string): Promise<User> => {
    const response = await apiClient.get(`/api/users/${userId}`);
    return response.data;
  },

  createUser: async (userData: CreateUserRequest): Promise<User> => {
    const response = await apiClient.post('/api/users', userData);
    return response.data;
  },

  updateUser: async (userId: string, userData: UpdateUserRequest): Promise<User> => {
    const response = await apiClient.put(`/api/users/${userId}`, userData);
    return response.data;
  },

  deleteUser: async (userId: string): Promise<void> => {
    await apiClient.delete(`/api/users/${userId}`);
  },

  bulkUpdateUsers: async (bulkData: BulkUpdateRequest): Promise<void> => {
    await apiClient.put('/api/users/bulk', bulkData);
  },

  importUsers: async (formData: FormData): Promise<any> => {
    const response = await apiClient.post('/api/users/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    return response.data;
  },

  exportUsers: async (params: { userIds?: string[]; format: string }): Promise<Blob> => {
    const response = await apiClient.get('/api/users/export', {
      params,
      responseType: 'blob'
    });
    return response.data;
  },

  assignRole: async (userId: string, roleId: string): Promise<void> => {
    await apiClient.post(`/api/users/${userId}/roles`, { roleId });
  },

  removeRole: async (userId: string, roleId: string): Promise<void> => {
    await apiClient.delete(`/api/users/${userId}/roles/${roleId}`);
  },

  assignProject: async (userId: string, projectId: string): Promise<void> => {
    await apiClient.post(`/api/users/${userId}/project`, { projectId });
  },

  removeProject: async (userId: string): Promise<void> => {
    await apiClient.delete(`/api/users/${userId}/project`);
  },

  assignDepartment: async (userId: string, departmentId: string): Promise<void> => {
    await apiClient.post(`/api/users/${userId}/departments`, { departmentId });
  },

  removeDepartment: async (userId: string, departmentId: string): Promise<void> => {
    await apiClient.delete(`/api/users/${userId}/departments/${departmentId}`);
  }
};
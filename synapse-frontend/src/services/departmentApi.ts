import { apiClient } from './apiClient';
import { Department, CreateDepartmentRequest, UpdateDepartmentRequest } from '../types/department';

export const departmentApi = {
  getDepartments: async () => {
    const response = await apiClient.get('/api/departments');
    return response.data;
  },

  getDepartment: async (departmentId: string): Promise<Department> => {
    const response = await apiClient.get(`/api/departments/${departmentId}`);
    return response.data;
  },

  createDepartment: async (departmentData: CreateDepartmentRequest): Promise<Department> => {
    const response = await apiClient.post('/api/departments', departmentData);
    return response.data;
  },

  updateDepartment: async (departmentId: string, departmentData: UpdateDepartmentRequest): Promise<Department> => {
    const response = await apiClient.put(`/api/departments/${departmentId}`, departmentData);
    return response.data;
  },

  deleteDepartment: async (departmentId: string): Promise<void> => {
    await apiClient.delete(`/api/departments/${departmentId}`);
  },

  assignUser: async (departmentId: string, userId: string): Promise<void> => {
    await apiClient.post(`/api/departments/${departmentId}/users`, { userId });
  },

  removeUser: async (departmentId: string, userId: string): Promise<void> => {
    await apiClient.delete(`/api/departments/${departmentId}/users/${userId}`);
  },

  getDepartmentUsers: async (departmentId: string) => {
    const response = await apiClient.get(`/api/departments/${departmentId}/users`);
    return response.data;
  }
};
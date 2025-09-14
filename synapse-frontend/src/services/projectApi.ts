import { apiClient } from './apiClient';
import { Project, CreateProjectRequest, UpdateProjectRequest } from '../types/project';

export const projectApi = {
  getProjects: async () => {
    const response = await apiClient.get('/api/projects');
    return response.data;
  },

  getProject: async (projectId: string): Promise<Project> => {
    const response = await apiClient.get(`/api/projects/${projectId}`);
    return response.data;
  },

  createProject: async (projectData: CreateProjectRequest): Promise<Project> => {
    const response = await apiClient.post('/api/projects', projectData);
    return response.data;
  },

  updateProject: async (projectId: string, projectData: UpdateProjectRequest): Promise<Project> => {
    const response = await apiClient.put(`/api/projects/${projectId}`, projectData);
    return response.data;
  },

  deleteProject: async (projectId: string): Promise<void> => {
    await apiClient.delete(`/api/projects/${projectId}`);
  },

  assignUser: async (projectId: string, userId: string): Promise<void> => {
    await apiClient.post(`/api/projects/${projectId}/users`, { userId });
  },

  removeUser: async (userId: string): Promise<void> => {
    await apiClient.delete(`/api/users/${userId}/project`);
  },

  getProjectUsers: async (projectId: string) => {
    const response = await apiClient.get(`/api/projects/${projectId}/users`);
    return response.data;
  }
};
import { apiClient } from './apiClient';
import { AuditLog, AuditSearchCriteria, AuditAnalytics } from '../types/audit';

export const auditApi = {
  getAuditLogs: async (params: { page?: number; size?: number } & AuditSearchCriteria) => {
    const response = await apiClient.get('/api/audit/logs', { params });
    return response.data;
  },

  getAuditLog: async (logId: string): Promise<AuditLog> => {
    const response = await apiClient.get(`/api/audit/logs/${logId}`);
    return response.data;
  },

  getAnalytics: async (timeRange?: string): Promise<AuditAnalytics> => {
    const response = await apiClient.get('/api/audit/analytics', {
      params: { timeRange }
    });
    return response.data;
  },

  exportAuditLogs: async (params: { format: string; criteria?: AuditSearchCriteria }): Promise<Blob> => {
    const response = await apiClient.get('/api/audit/export', {
      params,
      responseType: 'blob'
    });
    return response.data;
  },

  generateComplianceReport: async (params: { startDate: Date; endDate: Date; format: string }): Promise<Blob> => {
    const response = await apiClient.post('/api/audit/compliance-report', params, {
      responseType: 'blob'
    });
    return response.data;
  },

  getSuspiciousActivities: async () => {
    const response = await apiClient.get('/api/audit/suspicious');
    return response.data;
  },

  markActivityReviewed: async (logId: string): Promise<void> => {
    await apiClient.post(`/api/audit/logs/${logId}/reviewed`);
  },

  createAlert: async (alertData: { name: string; criteria: any; threshold: number }): Promise<void> => {
    await apiClient.post('/api/audit/alerts', alertData);
  },

  getAlerts: async () => {
    const response = await apiClient.get('/api/audit/alerts');
    return response.data;
  }
};
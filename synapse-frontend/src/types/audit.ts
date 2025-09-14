export interface AuditLog {
  id: string;
  userId?: string;
  username?: string;
  actionType: string;
  resourceType?: string;
  resourceId?: string;
  details?: any;
  ipAddress: string;
  userAgent?: string;
  timestamp: string;
  success: boolean;
  errorMessage?: string;
  duration?: number;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
}

export interface AuditSearchCriteria {
  query?: string;
  actionType?: string;
  userId?: string;
  resourceType?: string;
  startDate?: Date | null;
  endDate?: Date | null;
  success?: boolean;
  severity?: string;
}

export interface AuditAnalytics {
  totalActivities: number;
  activeUsers24h: number;
  failedLogins24h: number;
  suspiciousActivities: number;
  activityTimeline: Array<{
    date: string;
    activities: number;
  }>;
  actionTypeDistribution: Array<{
    name: string;
    count: number;
  }>;
  topUsers: Array<{
    username: string;
    activityCount: number;
  }>;
  failedActivities: Array<{
    hour: string;
    count: number;
  }>;
}

export interface AuditState {
  auditLogs: AuditLog[];
  analytics: AuditAnalytics | null;
  isLoading: boolean;
  error: string | null;
  totalCount: number;
}
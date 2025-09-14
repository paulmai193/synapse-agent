import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { AuditState, AuditLog, AuditSearchCriteria, AuditAnalytics } from '../../types/audit';
import { auditApi } from '../../services/auditApi';

const initialState: AuditState = {
  auditLogs: [],
  analytics: null,
  isLoading: false,
  error: null,
  totalCount: 0
};

export const fetchAuditLogs = createAsyncThunk(
  'audit/fetchAuditLogs',
  async (params: { page?: number; size?: number } & AuditSearchCriteria, { rejectWithValue }) => {
    try {
      const response = await auditApi.getAuditLogs(params);
      return response;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch audit logs');
    }
  }
);

export const fetchAuditAnalytics = createAsyncThunk(
  'audit/fetchAuditAnalytics',
  async (timeRange?: string, { rejectWithValue }) => {
    try {
      const analytics = await auditApi.getAnalytics(timeRange);
      return analytics;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch analytics');
    }
  }
);

export const exportAuditLogs = createAsyncThunk(
  'audit/exportAuditLogs',
  async (params: { format: string; criteria?: AuditSearchCriteria }, { rejectWithValue }) => {
    try {
      const blob = await auditApi.exportAuditLogs(params);
      return blob;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to export audit logs');
    }
  }
);

export const generateComplianceReport = createAsyncThunk(
  'audit/generateComplianceReport',
  async (params: { startDate: Date; endDate: Date; format: string }, { rejectWithValue }) => {
    try {
      const blob = await auditApi.generateComplianceReport(params);
      return blob;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to generate compliance report');
    }
  }
);

const auditSlice = createSlice({
  name: 'audit',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    clearAuditLogs: (state) => {
      state.auditLogs = [];
      state.totalCount = 0;
    }
  },
  extraReducers: (builder) => {
    builder
      // Fetch Audit Logs
      .addCase(fetchAuditLogs.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchAuditLogs.fulfilled, (state, action) => {
        state.isLoading = false;
        state.auditLogs = action.payload.logs || action.payload;
        state.totalCount = action.payload.totalCount || action.payload.length;
      })
      .addCase(fetchAuditLogs.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })

      // Fetch Analytics
      .addCase(fetchAuditAnalytics.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchAuditAnalytics.fulfilled, (state, action) => {
        state.isLoading = false;
        state.analytics = action.payload;
      })
      .addCase(fetchAuditAnalytics.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })

      // Export Audit Logs
      .addCase(exportAuditLogs.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(exportAuditLogs.fulfilled, (state) => {
        state.isLoading = false;
      })
      .addCase(exportAuditLogs.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })

      // Generate Compliance Report
      .addCase(generateComplianceReport.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(generateComplianceReport.fulfilled, (state) => {
        state.isLoading = false;
      })
      .addCase(generateComplianceReport.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      });
  }
});

export const { clearError, clearAuditLogs } = auditSlice.actions;
export default auditSlice.reducer;
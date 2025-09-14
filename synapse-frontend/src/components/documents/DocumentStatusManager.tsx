import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Typography,
  Alert,
} from '@mui/material';
import { Document } from '../../types';
import { useAuth } from '../../hooks/useAuth';
import { apiClient } from '../../utils/api';
import { API_CONFIG } from '../../config/api';

interface DocumentStatusManagerProps {
  document: Document | null;
  open: boolean;
  onClose: () => void;
  onStatusUpdate: (documentId: string, newStatus: string) => void;
}

const DocumentStatusManager: React.FC<DocumentStatusManagerProps> = ({
  document,
  open,
  onClose,
  onStatusUpdate,
}) => {
  const { hasAnyRole } = useAuth();
  const [newStatus, setNewStatus] = useState<'ACTIVE' | 'INACTIVE'>('ACTIVE');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  React.useEffect(() => {
    if (document) {
      setNewStatus(document.status as 'ACTIVE' | 'INACTIVE');
    }
  }, [document]);

  const handleStatusUpdate = async () => {
    if (!document) return;

    setLoading(true);
    setError('');

    try {
      await apiClient.put(`${API_CONFIG.ENDPOINTS.DOCUMENTS}/${document.id}/status`, {
        status: newStatus,
      });

      onStatusUpdate(document.id, newStatus);
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update status');
    } finally {
      setLoading(false);
    }
  };

  if (!hasAnyRole(['SYSTEM_ADMIN', 'PROJECT_ADMIN', 'DEPARTMENT_ADMIN'])) {
    return null;
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Update Document Status</DialogTitle>
      <DialogContent>
        <Typography variant="body1" gutterBottom>
          Document: <strong>{document?.title}</strong>
        </Typography>
        
        <FormControl fullWidth margin="normal">
          <InputLabel>Status</InputLabel>
          <Select
            value={newStatus}
            onChange={(e) => setNewStatus(e.target.value as 'ACTIVE' | 'INACTIVE')}
            label="Status"
          >
            <MenuItem value="ACTIVE">Active</MenuItem>
            <MenuItem value="INACTIVE">Inactive</MenuItem>
          </Select>
        </FormControl>

        <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
          Inactive documents will not appear in search results or Q&A responses.
        </Typography>

        {error && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {error}
          </Alert>
        )}
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onClose} disabled={loading}>
          Cancel
        </Button>
        <Button
          onClick={handleStatusUpdate}
          variant="contained"
          disabled={loading || newStatus === document?.status}
        >
          {loading ? 'Updating...' : 'Update Status'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default DocumentStatusManager;
import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  Tabs,
  Tab,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  LinearProgress,
  List,
  ListItem,
  ListItemText,
  Divider
} from '@mui/material';
import {
  Upload as UploadIcon,
  Download as DownloadIcon,
  Edit as EditIcon
} from '@mui/icons-material';
import { useAppDispatch } from '../../hooks/redux';
import { bulkUpdateUsers, importUsers, exportUsers } from '../../store/slices/userSlice';

interface BulkUserOperationsProps {
  open: boolean;
  onClose: () => void;
  selectedUsers: string[];
  onOperationComplete: () => void;
}

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => (
  <div hidden={value !== index}>
    {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
  </div>
);

export const BulkUserOperations: React.FC<BulkUserOperationsProps> = ({
  open,
  onClose,
  selectedUsers,
  onOperationComplete
}) => {
  const dispatch = useAppDispatch();
  const [activeTab, setActiveTab] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  
  // Bulk Edit State
  const [bulkEditData, setBulkEditData] = useState({
    status: '',
    addRoles: [] as string[],
    removeRoles: [] as string[],
    projectId: ''
  });
  
  // Import State
  const [importFile, setImportFile] = useState<File | null>(null);
  const [importProgress, setImportProgress] = useState(0);
  const [importResults, setImportResults] = useState<any>(null);

  const handleBulkEdit = async () => {
    if (selectedUsers.length === 0) {
      setError('No users selected');
      return;
    }

    setIsLoading(true);
    setError(null);
    
    try {
      await dispatch(bulkUpdateUsers({
        userIds: selectedUsers,
        updates: bulkEditData
      })).unwrap();
      
      setSuccess(`Successfully updated ${selectedUsers.length} users`);
      onOperationComplete();
    } catch (err: any) {
      setError(err.message || 'Failed to update users');
    } finally {
      setIsLoading(false);
    }
  };

  const handleImport = async () => {
    if (!importFile) {
      setError('Please select a file to import');
      return;
    }

    setIsLoading(true);
    setError(null);
    setImportProgress(0);
    
    try {
      const formData = new FormData();
      formData.append('file', importFile);
      
      const result = await dispatch(importUsers(formData)).unwrap();
      setImportResults(result);
      setSuccess(`Successfully imported ${result.successful} users`);
      onOperationComplete();
    } catch (err: any) {
      setError(err.message || 'Failed to import users');
    } finally {
      setIsLoading(false);
      setImportProgress(100);
    }
  };

  const handleExport = async () => {
    setIsLoading(true);
    setError(null);
    
    try {
      const blob = await dispatch(exportUsers({
        userIds: selectedUsers.length > 0 ? selectedUsers : undefined,
        format: 'csv'
      })).unwrap();
      
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `users_export_${new Date().toISOString().split('T')[0]}.csv`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      
      setSuccess('Users exported successfully');
    } catch (err: any) {
      setError(err.message || 'Failed to export users');
    } finally {
      setIsLoading(false);
    }
  };

  const downloadTemplate = () => {
    const csvContent = 'username,email,password,roles,status,projectId,departmentIds\n' +
                      'john.doe,john@example.com,password123,USER,ACTIVE,,dept1;dept2\n' +
                      'jane.admin,jane@example.com,admin123,USER;PROJECT_ADMIN,ACTIVE,proj1,dept1';
    
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'user_import_template.csv';
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  };

  const resetState = () => {
    setError(null);
    setSuccess(null);
    setImportResults(null);
    setImportFile(null);
    setImportProgress(0);
    setBulkEditData({
      status: '',
      addRoles: [],
      removeRoles: [],
      projectId: ''
    });
  };

  const handleClose = () => {
    resetState();
    onClose();
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <DialogTitle>Bulk User Operations</DialogTitle>
      
      <DialogContent>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
        
        <Tabs value={activeTab} onChange={(_, newValue) => setActiveTab(newValue)}>
          <Tab label="Bulk Edit" icon={<EditIcon />} />
          <Tab label="Import" icon={<UploadIcon />} />
          <Tab label="Export" icon={<DownloadIcon />} />
        </Tabs>

        <TabPanel value={activeTab} index={0}>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Selected users: {selectedUsers.length}
          </Typography>
          
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
            <FormControl>
              <InputLabel>Update Status</InputLabel>
              <Select
                value={bulkEditData.status}
                onChange={(e) => setBulkEditData({ ...bulkEditData, status: e.target.value })}
                label="Update Status"
              >
                <MenuItem value="">No Change</MenuItem>
                <MenuItem value="ACTIVE">Active</MenuItem>
                <MenuItem value="INACTIVE">Inactive</MenuItem>
              </Select>
            </FormControl>
            
            <TextField
              label="Add Roles (comma-separated)"
              value={bulkEditData.addRoles.join(', ')}
              onChange={(e) => setBulkEditData({
                ...bulkEditData,
                addRoles: e.target.value.split(',').map(r => r.trim()).filter(r => r)
              })}
              placeholder="USER, PROJECT_ADMIN"
            />
            
            <TextField
              label="Remove Roles (comma-separated)"
              value={bulkEditData.removeRoles.join(', ')}
              onChange={(e) => setBulkEditData({
                ...bulkEditData,
                removeRoles: e.target.value.split(',').map(r => r.trim()).filter(r => r)
              })}
              placeholder="USER, PROJECT_ADMIN"
            />
          </Box>
        </TabPanel>

        <TabPanel value={activeTab} index={1}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <Button
              variant="outlined"
              onClick={downloadTemplate}
              startIcon={<DownloadIcon />}
            >
              Download CSV Template
            </Button>
            
            <input
              type="file"
              accept=".csv"
              onChange={(e) => setImportFile(e.target.files?.[0] || null)}
              style={{ margin: '16px 0' }}
            />
            
            {isLoading && (
              <Box sx={{ width: '100%' }}>
                <LinearProgress variant="determinate" value={importProgress} />
                <Typography variant="body2" color="text.secondary" align="center">
                  Importing users...
                </Typography>
              </Box>
            )}
            
            {importResults && (
              <Box>
                <Typography variant="h6" gutterBottom>Import Results</Typography>
                <List dense>
                  <ListItem>
                    <ListItemText
                      primary="Successful"
                      secondary={`${importResults.successful} users imported`}
                    />
                  </ListItem>
                  <ListItem>
                    <ListItemText
                      primary="Failed"
                      secondary={`${importResults.failed} users failed`}
                    />
                  </ListItem>
                  {importResults.errors && importResults.errors.length > 0 && (
                    <>
                      <Divider />
                      <ListItem>
                        <ListItemText
                          primary="Errors"
                          secondary={importResults.errors.join(', ')}
                        />
                      </ListItem>
                    </>
                  )}
                </List>
              </Box>
            )}
          </Box>
        </TabPanel>

        <TabPanel value={activeTab} index={2}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <Typography variant="body2" color="text.secondary">
              {selectedUsers.length > 0
                ? `Export ${selectedUsers.length} selected users`
                : 'Export all users'
              }
            </Typography>
            
            <Alert severity="info">
              The export will include user details, roles, and assignments in CSV format.
            </Alert>
          </Box>
        </TabPanel>
      </DialogContent>
      
      <DialogActions>
        <Button onClick={handleClose}>Close</Button>
        {activeTab === 0 && (
          <Button
            onClick={handleBulkEdit}
            variant="contained"
            disabled={isLoading || selectedUsers.length === 0}
          >
            Apply Changes
          </Button>
        )}
        {activeTab === 1 && (
          <Button
            onClick={handleImport}
            variant="contained"
            disabled={isLoading || !importFile}
          >
            Import Users
          </Button>
        )}
        {activeTab === 2 && (
          <Button
            onClick={handleExport}
            variant="contained"
            disabled={isLoading}
          >
            Export Users
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};
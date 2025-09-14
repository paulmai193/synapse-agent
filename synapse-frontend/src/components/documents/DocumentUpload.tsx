import React, { useState, useCallback } from 'react';
import {
  Box,
  Typography,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Alert,
  LinearProgress,
  Paper,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';
import { CloudUpload, AttachFile } from '@mui/icons-material';
import { useDropzone } from 'react-dropzone';
import { useAuth } from '../../hooks/useAuth';
import { apiClient } from '../../utils/api';
import { API_CONFIG } from '../../config/api';
import { Document } from '../../types';

interface DocumentUploadProps {
  onSuccess: (document: Document) => void;
  onCancel: () => void;
}

const DocumentUpload: React.FC<DocumentUploadProps> = ({ onSuccess, onCancel }) => {
  const { user } = useAuth();
  const [files, setFiles] = useState<File[]>([]);
  const [title, setTitle] = useState('');
  const [visibility, setVisibility] = useState<'PROJECT' | 'DEPARTMENT'>('PROJECT');
  const [selectedDepartments, setSelectedDepartments] = useState<number[]>([]);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');
  const [uploadProgress, setUploadProgress] = useState(0);

  const onDrop = useCallback((acceptedFiles: File[]) => {
    setFiles(acceptedFiles);
    if (acceptedFiles.length > 0 && !title) {
      setTitle(acceptedFiles[0].name.replace(/\.[^/.]+$/, ''));
    }
  }, [title]);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'application/pdf': ['.pdf'],
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['.docx'],
      'text/plain': ['.txt'],
      'text/markdown': ['.md'],
    },
    maxSize: 100 * 1024 * 1024, // 100MB
    multiple: false,
  });

  const handleUpload = async () => {
    if (!files.length || !title) return;

    setUploading(true);
    setError('');
    setUploadProgress(0);

    try {
      const formData = new FormData();
      formData.append('file', files[0]);
      formData.append('title', title);
      formData.append('visibility', visibility);
      
      if (visibility === 'DEPARTMENT') {
        formData.append('departmentIds', selectedDepartments.join(','));
      } else if (user?.projectId) {
        formData.append('projectId', user.projectId.toString());
      }

      const response = await apiClient.post<Document>(
        API_CONFIG.ENDPOINTS.DOCUMENTS + '/upload',
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
          onUploadProgress: (progressEvent) => {
            if (progressEvent.total) {
              const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
              setUploadProgress(progress);
            }
          },
        }
      );

      onSuccess(response);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  const removeFile = () => {
    setFiles([]);
    setTitle('');
  };

  return (
    <>
      <DialogTitle>Upload Document</DialogTitle>
      <DialogContent>
        <Box sx={{ mb: 3 }}>
          <Paper
            {...getRootProps()}
            sx={{
              p: 3,
              border: '2px dashed',
              borderColor: isDragActive ? 'primary.main' : 'grey.300',
              bgcolor: isDragActive ? 'action.hover' : 'background.paper',
              cursor: 'pointer',
              textAlign: 'center',
            }}
          >
            <input {...getInputProps()} />
            <CloudUpload sx={{ fontSize: 48, color: 'text.secondary', mb: 2 }} />
            <Typography variant="h6" gutterBottom>
              {isDragActive ? 'Drop files here' : 'Drag & drop files here'}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              or click to select files
            </Typography>
            <Typography variant="caption" display="block" sx={{ mt: 1 }}>
              Supported: PDF, DOCX, TXT, MD (max 100MB)
            </Typography>
          </Paper>
        </Box>

        {files.length > 0 && (
          <Box sx={{ mb: 3 }}>
            <Typography variant="subtitle2" gutterBottom>
              Selected File:
            </Typography>
            <Chip
              icon={<AttachFile />}
              label={`${files[0].name} (${(files[0].size / 1024 / 1024).toFixed(2)} MB)`}
              onDelete={removeFile}
              sx={{ mb: 2 }}
            />
          </Box>
        )}

        <TextField
          fullWidth
          label="Document Title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          margin="normal"
          required
        />

        <FormControl fullWidth margin="normal">
          <InputLabel>Visibility</InputLabel>
          <Select
            value={visibility}
            onChange={(e) => setVisibility(e.target.value as 'PROJECT' | 'DEPARTMENT')}
            label="Visibility"
          >
            <MenuItem value="PROJECT">Project Only</MenuItem>
            <MenuItem value="DEPARTMENT">Department(s)</MenuItem>
          </Select>
        </FormControl>

        {visibility === 'DEPARTMENT' && (
          <FormControl fullWidth margin="normal">
            <InputLabel>Departments</InputLabel>
            <Select
              multiple
              value={selectedDepartments}
              onChange={(e) => setSelectedDepartments(e.target.value as number[])}
              label="Departments"
              renderValue={(selected) => (
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                  {selected.map((value) => (
                    <Chip key={value} label={`Department ${value}`} size="small" />
                  ))}
                </Box>
              )}
            >
              {user?.departmentIds.map((deptId) => (
                <MenuItem key={deptId} value={deptId}>
                  Department {deptId}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        )}

        {error && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {error}
          </Alert>
        )}

        {uploading && (
          <Box sx={{ mt: 2 }}>
            <Typography variant="body2" gutterBottom>
              Uploading... {uploadProgress}%
            </Typography>
            <LinearProgress variant="determinate" value={uploadProgress} />
          </Box>
        )}
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onCancel} disabled={uploading}>
          Cancel
        </Button>
        <Button
          onClick={handleUpload}
          variant="contained"
          disabled={!files.length || !title || uploading}
        >
          {uploading ? 'Uploading...' : 'Upload'}
        </Button>
      </DialogActions>
    </>
  );
};

export default DocumentUpload;
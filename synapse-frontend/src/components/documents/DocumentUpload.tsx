import React, { useState, useCallback } from 'react';
import {
  Paper,
  Typography,
  Box,
  TextField,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  LinearProgress,
  Alert,
} from '@mui/material';
import { useDropzone } from 'react-dropzone';
import { documentApi } from '../../utils/documentApi';
import { DocumentUploadRequest } from '../../types/document';

interface DocumentUploadProps {
  onUploadComplete?: () => void;
}

const DocumentUpload: React.FC<DocumentUploadProps> = ({ onUploadComplete }) => {
  const [file, setFile] = useState<File | null>(null);
  const [metadata, setMetadata] = useState<DocumentUploadRequest>({
    title: '',
    visibility: 'PROJECT',
    departmentIds: [],
    tags: [],
  });
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState('');
  const [tagInput, setTagInput] = useState('');

  const onDrop = useCallback((acceptedFiles: File[]) => {
    if (acceptedFiles.length > 0) {
      const uploadedFile = acceptedFiles[0];
      setFile(uploadedFile);
      if (!metadata.title) {
        setMetadata(prev => ({
          ...prev,
          title: uploadedFile.name.replace(/\.[^/.]+$/, '')
        }));
      }
    }
  }, [metadata.title]);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'text/plain': ['.txt'],
      'application/pdf': ['.pdf'],
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['.docx'],
    },
    maxFiles: 1,
  });

  const handleAddTag = () => {
    if (tagInput.trim() && !metadata.tags?.includes(tagInput.trim())) {
      setMetadata(prev => ({
        ...prev,
        tags: [...(prev.tags || []), tagInput.trim()]
      }));
      setTagInput('');
    }
  };

  const handleRemoveTag = (tagToRemove: string) => {
    setMetadata(prev => ({
      ...prev,
      tags: prev.tags?.filter(tag => tag !== tagToRemove) || []
    }));
  };

  const handleUpload = async () => {
    if (!file || !metadata.title) {
      setError('Please select a file and provide a title');
      return;
    }

    setUploading(true);
    setProgress(0);
    setError('');

    try {
      await documentApi.uploadDocument(file, metadata);
      setProgress(100);
      setFile(null);
      setMetadata({
        title: '',
        visibility: 'PROJECT',
        departmentIds: [],
        tags: [],
      });
      onUploadComplete?.();
    } catch (err) {
      setError('Upload failed. Please try again.');
    } finally {
      setUploading(false);
    }
  };

  return (
    <Paper elevation={3} sx={{ p: 3 }}>
      <Typography variant="h6" gutterBottom>
        Upload Document
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Box
        {...getRootProps()}
        sx={{
          border: '2px dashed #ccc',
          borderRadius: 2,
          p: 3,
          textAlign: 'center',
          cursor: 'pointer',
          mb: 3,
          backgroundColor: isDragActive ? '#f5f5f5' : 'transparent',
        }}
      >
        <input {...getInputProps()} />
        {file ? (
          <Typography>{file.name}</Typography>
        ) : (
          <Typography>
            {isDragActive
              ? 'Drop the file here...'
              : 'Drag & drop a file here, or click to select'}
          </Typography>
        )}
      </Box>

      <TextField
        fullWidth
        label="Document Title"
        value={metadata.title}
        onChange={(e) => setMetadata(prev => ({ ...prev, title: e.target.value }))}
        margin="normal"
        required
      />

      <FormControl fullWidth margin="normal">
        <InputLabel>Visibility</InputLabel>
        <Select
          value={metadata.visibility}
          onChange={(e) => setMetadata(prev => ({ 
            ...prev, 
            visibility: e.target.value as 'PROJECT' | 'DEPARTMENT' 
          }))}
        >
          <MenuItem value="PROJECT">Project</MenuItem>
          <MenuItem value="DEPARTMENT">Department</MenuItem>
        </Select>
      </FormControl>

      <Box sx={{ mt: 2, mb: 2 }}>
        <TextField
          label="Add Tag"
          value={tagInput}
          onChange={(e) => setTagInput(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleAddTag()}
          size="small"
          sx={{ mr: 1 }}
        />
        <Button onClick={handleAddTag} variant="outlined" size="small">
          Add Tag
        </Button>
      </Box>

      <Box sx={{ mb: 2 }}>
        {metadata.tags?.map((tag) => (
          <Chip
            key={tag}
            label={tag}
            onDelete={() => handleRemoveTag(tag)}
            sx={{ mr: 1, mb: 1 }}
          />
        ))}
      </Box>

      {uploading && (
        <Box sx={{ mb: 2 }}>
          <LinearProgress variant="determinate" value={progress} />
        </Box>
      )}

      <Button
        variant="contained"
        onClick={handleUpload}
        disabled={!file || !metadata.title || uploading}
        fullWidth
      >
        {uploading ? 'Uploading...' : 'Upload Document'}
      </Button>
    </Paper>
  );
};

export default DocumentUpload;
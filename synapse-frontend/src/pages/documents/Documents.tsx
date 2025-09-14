import React, { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Grid,
  Card,
  CardContent,
  Chip,
  IconButton,
  Menu,
  MenuItem,
  Dialog,
} from '@mui/material';
import {
  Upload,
  MoreVert,
  Delete,
  Edit,
  Visibility,
  CloudUpload,
} from '@mui/icons-material';
import { Document } from '../../types';
import DocumentUpload from '../../components/documents/DocumentUpload';
import DocumentList from '../../components/documents/DocumentList';

const Documents: React.FC = () => {
  const [uploadDialogOpen, setUploadDialogOpen] = useState(false);
  const [documents, setDocuments] = useState<Document[]>([]);

  const handleUploadSuccess = (document: Document) => {
    setDocuments(prev => [document, ...prev]);
    setUploadDialogOpen(false);
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          Documents
        </Typography>
        <Button
          variant="contained"
          startIcon={<CloudUpload />}
          onClick={() => setUploadDialogOpen(true)}
        >
          Upload Document
        </Button>
      </Box>

      <DocumentList documents={documents} onDocumentsChange={setDocuments} />

      <Dialog
        open={uploadDialogOpen}
        onClose={() => setUploadDialogOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DocumentUpload
          onSuccess={handleUploadSuccess}
          onCancel={() => setUploadDialogOpen(false)}
        />
      </Dialog>
    </Box>
  );
};

export default Documents;
import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Box,
  Chip,
  Divider,
  IconButton,
  Tooltip,
  CircularProgress,
  Alert
} from '@mui/material';
import {
  Close as CloseIcon,
  Download as DownloadIcon,
  Share as ShareIcon,
  Bookmark as BookmarkIcon,
  BookmarkBorder as BookmarkBorderIcon
} from '@mui/icons-material';
import { useAppDispatch, useAppSelector } from '../../hooks/redux';
import { fetchDocumentDetails, bookmarkDocument } from '../../store/slices/documentSlice';
import { Document } from '../../types/document';

interface DocumentPreviewProps {
  documentId: string | null;
  open: boolean;
  onClose: () => void;
}

export const DocumentPreview: React.FC<DocumentPreviewProps> = ({
  documentId,
  open,
  onClose
}) => {
  const dispatch = useAppDispatch();
  const { selectedDocument, isLoading, error } = useAppSelector(state => state.documents);
  
  const [isBookmarked, setIsBookmarked] = useState(false);

  useEffect(() => {
    if (documentId && open) {
      dispatch(fetchDocumentDetails(documentId));
    }
  }, [documentId, open, dispatch]);

  useEffect(() => {
    if (selectedDocument) {
      setIsBookmarked(selectedDocument.isBookmarked || false);
    }
  }, [selectedDocument]);

  const handleBookmark = async () => {
    if (documentId) {
      try {
        await dispatch(bookmarkDocument(documentId)).unwrap();
        setIsBookmarked(!isBookmarked);
      } catch (error) {
        console.error('Failed to bookmark document:', error);
      }
    }
  };

  const handleDownload = () => {
    if (selectedDocument?.downloadUrl) {
      window.open(selectedDocument.downloadUrl, '_blank');
    }
  };

  const handleShare = () => {
    if (selectedDocument) {
      const shareData = {
        title: selectedDocument.title,
        text: `Check out this document: ${selectedDocument.title}`,
        url: window.location.href
      };
      
      if (navigator.share) {
        navigator.share(shareData);
      } else {
        navigator.clipboard.writeText(shareData.url);
      }
    }
  };

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString([], {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
      PaperProps={{
        sx: { height: '80vh' }
      }}
    >
      <DialogTitle>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Typography variant="h6" noWrap>
            Document Preview
          </Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            {selectedDocument && (
              <>
                <Tooltip title={isBookmarked ? 'Remove bookmark' : 'Bookmark'}>
                  <IconButton onClick={handleBookmark} size="small">
                    {isBookmarked ? <BookmarkIcon /> : <BookmarkBorderIcon />}
                  </IconButton>
                </Tooltip>
                
                <Tooltip title="Download">
                  <IconButton onClick={handleDownload} size="small">
                    <DownloadIcon />
                  </IconButton>
                </Tooltip>
                
                <Tooltip title="Share">
                  <IconButton onClick={handleShare} size="small">
                    <ShareIcon />
                  </IconButton>
                </Tooltip>
              </>
            )}
            
            <IconButton onClick={onClose} size="small">
              <CloseIcon />
            </IconButton>
          </Box>
        </Box>
      </DialogTitle>

      <DialogContent dividers>
        {isLoading && (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <CircularProgress />
          </Box>
        )}

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {selectedDocument && (
          <Box>
            {/* Document Metadata */}
            <Box sx={{ mb: 3 }}>
              <Typography variant="h5" gutterBottom>
                {selectedDocument.title}
              </Typography>
              
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mb: 2 }}>
                <Chip
                  label={selectedDocument.originalFormat.toUpperCase()}
                  size="small"
                  color="primary"
                />
                <Chip
                  label={selectedDocument.language}
                  size="small"
                  variant="outlined"
                />
                <Chip
                  label={selectedDocument.status}
                  size="small"
                  color={selectedDocument.status === 'ACTIVE' ? 'success' : 'default'}
                />
                {selectedDocument.metadata?.fileSize && (
                  <Chip
                    label={formatFileSize(selectedDocument.metadata.fileSize)}
                    size="small"
                    variant="outlined"
                  />
                )}
              </Box>

              <Typography variant="body2" color="text.secondary" paragraph>
                <strong>Author:</strong> {selectedDocument.metadata?.author || 'Unknown'}
              </Typography>
              
              <Typography variant="body2" color="text.secondary" paragraph>
                <strong>Created:</strong> {formatDate(selectedDocument.createdAt)}
              </Typography>
              
              <Typography variant="body2" color="text.secondary" paragraph>
                <strong>Source:</strong> {selectedDocument.metadata?.source || 'Upload'}
              </Typography>

              {selectedDocument.metadata?.tags && selectedDocument.metadata.tags.length > 0 && (
                <Box sx={{ mt: 2 }}>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    <strong>Tags:</strong>
                  </Typography>
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                    {selectedDocument.metadata.tags.map((tag, index) => (
                      <Chip key={index} label={tag} size="small" variant="outlined" />
                    ))}
                  </Box>
                </Box>
              )}
            </Box>

            <Divider sx={{ my: 2 }} />

            {/* Document Content */}
            <Box>
              <Typography variant="h6" gutterBottom>
                Content Preview
              </Typography>
              
              <Box
                sx={{
                  maxHeight: 400,
                  overflow: 'auto',
                  p: 2,
                  bgcolor: 'grey.50',
                  borderRadius: 1,
                  border: 1,
                  borderColor: 'grey.200'
                }}
              >
                <Typography
                  variant="body2"
                  component="pre"
                  sx={{
                    whiteSpace: 'pre-wrap',
                    fontFamily: 'monospace',
                    fontSize: '0.875rem',
                    lineHeight: 1.5
                  }}
                >
                  {selectedDocument.content.length > 5000
                    ? `${selectedDocument.content.substring(0, 5000)}...\n\n[Content truncated - download full document to view all content]`
                    : selectedDocument.content
                  }
                </Typography>
              </Box>
            </Box>

            {/* Access Control Information */}
            {selectedDocument.accessControl && (
              <Box sx={{ mt: 3 }}>
                <Divider sx={{ mb: 2 }} />
                <Typography variant="h6" gutterBottom>
                  Access Control
                </Typography>
                
                <Typography variant="body2" color="text.secondary">
                  <strong>Visibility:</strong> {selectedDocument.accessControl.visibility}
                </Typography>
                
                {selectedDocument.accessControl.projectId && (
                  <Typography variant="body2" color="text.secondary">
                    <strong>Project:</strong> {selectedDocument.accessControl.projectId}
                  </Typography>
                )}
                
                {selectedDocument.accessControl.departmentIds && selectedDocument.accessControl.departmentIds.length > 0 && (
                  <Typography variant="body2" color="text.secondary">
                    <strong>Departments:</strong> {selectedDocument.accessControl.departmentIds.join(', ')}
                  </Typography>
                )}
              </Box>
            )}
          </Box>
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Close</Button>
      </DialogActions>
    </Dialog>
  );
};
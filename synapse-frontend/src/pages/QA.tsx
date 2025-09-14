import React, { useState } from 'react';
import { Box, Container, Paper, Typography, IconButton, Tooltip } from '@mui/material';
import { History as HistoryIcon } from '@mui/icons-material';
import { QAInterface } from '../components/qa/QAInterface';
import { DocumentPreview } from '../components/qa/DocumentPreview';

export const QA: React.FC = () => {
  const [showHistory, setShowHistory] = useState(false);
  const [previewDocumentId, setPreviewDocumentId] = useState<string | null>(null);

  const handleDocumentPreview = (documentId: string) => {
    setPreviewDocumentId(documentId);
  };

  const handleClosePreview = () => {
    setPreviewDocumentId(null);
  };

  return (
    <Container maxWidth="xl" sx={{ py: 3, height: 'calc(100vh - 100px)' }}>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 3 }}>
        <Box>
          <Typography variant="h4" component="h1" gutterBottom>
            Q&A Assistant
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Ask questions about your documents and get intelligent answers with source citations
          </Typography>
        </Box>
        
        <Tooltip title="Conversation History">
          <IconButton
            onClick={() => setShowHistory(true)}
            color="primary"
            size="large"
          >
            <HistoryIcon />
          </IconButton>
        </Tooltip>
      </Box>

      <Paper
        elevation={2}
        sx={{
          height: 'calc(100% - 120px)',
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden'
        }}
      >
        <QAInterface
          onDocumentPreview={handleDocumentPreview}
        />
      </Paper>

      <DocumentPreview
        documentId={previewDocumentId}
        open={Boolean(previewDocumentId)}
        onClose={handleClosePreview}
      />
    </Container>
  );
};
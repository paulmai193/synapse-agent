import React, { useState } from 'react';
import { Box, Grid, Tabs, Tab } from '@mui/material';
import DocumentUpload from '../components/documents/DocumentUpload';
import DocumentList from '../components/documents/DocumentList';

const Documents: React.FC = () => {
  const [activeTab, setActiveTab] = useState(0);

  const handleTabChange = (_: React.SyntheticEvent, newValue: number) => {
    setActiveTab(newValue);
  };

  const handleUploadComplete = () => {
    setActiveTab(1); // Switch to documents list after upload
  };

  return (
    <Box>
      <Tabs value={activeTab} onChange={handleTabChange} sx={{ mb: 3 }}>
        <Tab label="Upload Document" />
        <Tab label="My Documents" />
      </Tabs>

      {activeTab === 0 && (
        <Grid container spacing={3}>
          <Grid item xs={12} md={8}>
            <DocumentUpload onUploadComplete={handleUploadComplete} />
          </Grid>
        </Grid>
      )}

      {activeTab === 1 && (
        <DocumentList />
      )}
    </Box>
  );
};

export default Documents;
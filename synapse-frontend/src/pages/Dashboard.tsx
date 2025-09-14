import React from 'react';
import { Typography, Grid, Card, CardContent, Box } from '@mui/material';
import { useSelector } from 'react-redux';
import { RootState } from '../store';

const Dashboard: React.FC = () => {
  const { user } = useSelector((state: RootState) => state.auth);

  return (
    <Box>
      <Typography variant="h4" component="h1" gutterBottom>
        Welcome, {user?.username}
      </Typography>
      
      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Search Documents
              </Typography>
              <Typography variant="body2">
                Find information across all your documents using AI-powered search
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Ask Questions
              </Typography>
              <Typography variant="body2">
                Get answers from your knowledge base in multiple languages
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Upload Documents
              </Typography>
              <Typography variant="body2">
                Add new documents to expand your knowledge base
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;
import React from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Paper,
} from '@mui/material';
import {
  Search,
  QuestionAnswer,
  Upload,
  Analytics,
} from '@mui/icons-material';
import { useAuth } from '../../hooks/useAuth';

const Dashboard: React.FC = () => {
  const { user } = useAuth();

  const quickActions = [
    {
      title: 'Search Documents',
      description: 'Find information across all your documents',
      icon: <Search sx={{ fontSize: 40 }} />,
      path: '/search',
    },
    {
      title: 'Ask Questions',
      description: 'Get AI-powered answers from your knowledge base',
      icon: <QuestionAnswer sx={{ fontSize: 40 }} />,
      path: '/qa',
    },
    {
      title: 'Upload Documents',
      description: 'Add new documents to your knowledge base',
      icon: <Upload sx={{ fontSize: 40 }} />,
      path: '/documents',
    },
    {
      title: 'View Analytics',
      description: 'Monitor usage and system performance',
      icon: <Analytics sx={{ fontSize: 40 }} />,
      path: '/admin/analytics',
    },
  ];

  return (
    <Box>
      <Typography variant="h4" component="h1" gutterBottom>
        Welcome back, {user?.username}!
      </Typography>
      
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        Your AI-powered knowledge management dashboard
      </Typography>

      <Grid container spacing={3}>
        {quickActions.map((action, index) => (
          <Grid item xs={12} sm={6} md={3} key={index}>
            <Card 
              sx={{ 
                height: '100%', 
                cursor: 'pointer',
                '&:hover': {
                  boxShadow: 4,
                  transform: 'translateY(-2px)',
                  transition: 'all 0.2s',
                },
              }}
            >
              <CardContent sx={{ textAlign: 'center', p: 3 }}>
                <Box sx={{ color: 'primary.main', mb: 2 }}>
                  {action.icon}
                </Box>
                <Typography variant="h6" component="h2" gutterBottom>
                  {action.title}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {action.description}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Grid container spacing={3} sx={{ mt: 2 }}>
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Recent Activity
            </Typography>
            <Typography variant="body2" color="text.secondary">
              No recent activity to display.
            </Typography>
          </Paper>
        </Grid>
        
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Quick Stats
            </Typography>
            <Box sx={{ mt: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Documents: 0
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Searches: 0
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Q&A Sessions: 0
              </Typography>
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;
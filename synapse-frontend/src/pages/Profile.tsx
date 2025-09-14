import React from 'react';
import { 
  Paper, 
  Typography, 
  Box, 
  Chip, 
  Divider,
  List,
  ListItem,
  ListItemText
} from '@mui/material';
import { useSelector } from 'react-redux';
import { RootState } from '../store';

const Profile: React.FC = () => {
  const { user } = useSelector((state: RootState) => state.auth);

  if (!user) return null;

  return (
    <Box>
      <Typography variant="h4" component="h1" gutterBottom>
        User Profile
      </Typography>
      
      <Paper elevation={3} sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom>
          Basic Information
        </Typography>
        <List>
          <ListItem>
            <ListItemText primary="Username" secondary={user.username} />
          </ListItem>
          <ListItem>
            <ListItemText primary="Email" secondary={user.email} />
          </ListItem>
          <ListItem>
            <ListItemText primary="Status" secondary={user.status} />
          </ListItem>
        </List>
      </Paper>

      <Paper elevation={3} sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom>
          Roles
        </Typography>
        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
          {user.roles.map((role) => (
            <Chip 
              key={role.id} 
              label={role.name} 
              color="primary" 
              variant="outlined" 
            />
          ))}
        </Box>
      </Paper>

      {user.projectId && (
        <Paper elevation={3} sx={{ p: 3, mb: 3 }}>
          <Typography variant="h6" gutterBottom>
            Project Assignment
          </Typography>
          <Typography variant="body1">
            Project ID: {user.projectId}
          </Typography>
        </Paper>
      )}

      {user.departmentIds.length > 0 && (
        <Paper elevation={3} sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            Department Assignments
          </Typography>
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            {user.departmentIds.map((deptId) => (
              <Chip 
                key={deptId} 
                label={deptId} 
                color="secondary" 
                variant="outlined" 
              />
            ))}
          </Box>
        </Paper>
      )}
    </Box>
  );
};

export default Profile;
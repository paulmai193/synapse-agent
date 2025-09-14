import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  TextField,
  InputAdornment,
  Chip,
  Alert,
  Tabs,
  Tab
} from '@mui/material';
import {
  Search as SearchIcon,
  Add as AddIcon,
  Remove as RemoveIcon
} from '@mui/icons-material';
import { useAppDispatch, useAppSelector } from '../../hooks/redux';
import { fetchUsers } from '../../store/slices/userSlice';
import { assignUserToProject, removeUserFromProject } from '../../store/slices/projectSlice';
import { assignUserToDepartment, removeUserFromDepartment } from '../../store/slices/departmentSlice';
import { Project } from '../../types/project';
import { Department } from '../../types/department';
import { User } from '../../types/user';

interface UserAssignmentDialogProps {
  open: boolean;
  onClose: () => void;
  project?: Project | null;
  department?: Department | null;
  type: 'project' | 'department';
}

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => (
  <div hidden={value !== index}>
    {value === index && <Box sx={{ p: 2 }}>{children}</Box>}
  </div>
);

export const UserAssignmentDialog: React.FC<UserAssignmentDialogProps> = ({
  open,
  onClose,
  project,
  department,
  type
}) => {
  const dispatch = useAppDispatch();
  const { users } = useAppSelector(state => state.users);
  
  const [activeTab, setActiveTab] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const entity = project || department;
  const entityName = entity?.name || '';

  useEffect(() => {
    if (open) {
      dispatch(fetchUsers());
    }
  }, [open, dispatch]);

  const assignedUsers = users.filter(user => {
    if (type === 'project') {
      return user.project?.id === entity?.id;
    } else {
      return user.departments.some(dept => dept.id === entity?.id);
    }
  });

  const availableUsers = users.filter(user => {
    const matchesSearch = user.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         user.email.toLowerCase().includes(searchTerm.toLowerCase());
    
    if (type === 'project') {
      return matchesSearch && (!user.project || user.project.id !== entity?.id);
    } else {
      return matchesSearch && !user.departments.some(dept => dept.id === entity?.id);
    }
  });

  const handleAssignUser = async (user: User) => {
    if (!entity) return;
    
    setIsLoading(true);
    setError(null);
    
    try {
      if (type === 'project') {
        await dispatch(assignUserToProject({ userId: user.id, projectId: entity.id })).unwrap();
      } else {
        await dispatch(assignUserToDepartment({ userId: user.id, departmentId: entity.id })).unwrap();
      }
      
      setSuccess(`User ${user.username} assigned successfully`);
      dispatch(fetchUsers());
    } catch (err: any) {
      setError(err.message || `Failed to assign user to ${type}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleRemoveUser = async (user: User) => {
    if (!entity) return;
    
    setIsLoading(true);
    setError(null);
    
    try {
      if (type === 'project') {
        await dispatch(removeUserFromProject({ userId: user.id })).unwrap();
      } else {
        await dispatch(removeUserFromDepartment({ userId: user.id, departmentId: entity.id })).unwrap();
      }
      
      setSuccess(`User ${user.username} removed successfully`);
      dispatch(fetchUsers());
    } catch (err: any) {
      setError(err.message || `Failed to remove user from ${type}`);
    } finally {
      setIsLoading(false);
    }
  };

  const resetMessages = () => {
    setError(null);
    setSuccess(null);
  };

  const handleClose = () => {
    resetMessages();
    setSearchTerm('');
    setActiveTab(0);
    onClose();
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <DialogTitle>
        Manage Users - {entityName}
      </DialogTitle>
      
      <DialogContent>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
        
        <Tabs value={activeTab} onChange={(_, newValue) => setActiveTab(newValue)}>
          <Tab label={`Assigned Users (${assignedUsers.length})`} />
          <Tab label="Available Users" />
        </Tabs>

        <TabPanel value={activeTab} index={0}>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Users currently assigned to this {type}
          </Typography>
          
          {assignedUsers.length === 0 ? (
            <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 4 }}>
              No users assigned to this {type}
            </Typography>
          ) : (
            <List>
              {assignedUsers.map((user) => (
                <ListItem key={user.id}>
                  <ListItemText
                    primary={user.username}
                    secondary={
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 1 }}>
                        <Typography variant="caption">{user.email}</Typography>
                        <Box sx={{ display: 'flex', gap: 0.5 }}>
                          {user.roles.map((role) => (
                            <Chip key={role.id} label={role.name} size="small" />
                          ))}
                        </Box>
                      </Box>
                    }
                  />
                  <ListItemSecondaryAction>
                    <IconButton
                      edge="end"
                      onClick={() => handleRemoveUser(user)}
                      disabled={isLoading}
                      color="error"
                    >
                      <RemoveIcon />
                    </IconButton>
                  </ListItemSecondaryAction>
                </ListItem>
              ))}
            </List>
          )}
        </TabPanel>

        <TabPanel value={activeTab} index={1}>
          <Box sx={{ mb: 2 }}>
            <TextField
              placeholder="Search users..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                )
              }}
              fullWidth
              size="small"
            />
          </Box>
          
          {availableUsers.length === 0 ? (
            <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 4 }}>
              {searchTerm ? 'No users found matching your search' : 'No available users'}
            </Typography>
          ) : (
            <List>
              {availableUsers.slice(0, 20).map((user) => (
                <ListItem key={user.id}>
                  <ListItemText
                    primary={user.username}
                    secondary={
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 1 }}>
                        <Typography variant="caption">{user.email}</Typography>
                        <Box sx={{ display: 'flex', gap: 0.5 }}>
                          {user.roles.map((role) => (
                            <Chip key={role.id} label={role.name} size="small" />
                          ))}
                        </Box>
                      </Box>
                    }
                  />
                  <ListItemSecondaryAction>
                    <IconButton
                      edge="end"
                      onClick={() => handleAssignUser(user)}
                      disabled={isLoading}
                      color="primary"
                    >
                      <AddIcon />
                    </IconButton>
                  </ListItemSecondaryAction>
                </ListItem>
              ))}
            </List>
          )}
        </TabPanel>
      </DialogContent>
      
      <DialogActions>
        <Button onClick={handleClose}>Close</Button>
      </DialogActions>
    </Dialog>
  );
};
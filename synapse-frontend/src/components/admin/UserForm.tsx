import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Box,
  Typography,
  Autocomplete,
  FormControlLabel,
  Switch
} from '@mui/material';
import { User, CreateUserRequest, UpdateUserRequest } from '../../types/user';
import { Project } from '../../types/project';
import { Department } from '../../types/department';

interface UserFormProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (data: CreateUserRequest | UpdateUserRequest) => void;
  user?: User | null;
  projects: Project[];
  departments: Department[];
}

export const UserForm: React.FC<UserFormProps> = ({
  open,
  onClose,
  onSubmit,
  user,
  projects,
  departments
}) => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    status: 'ACTIVE',
    roles: ['USER'],
    projectId: '',
    departmentIds: [] as string[]
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (user) {
      setFormData({
        username: user.username,
        email: user.email,
        password: '',
        status: user.status,
        roles: user.roles.map(role => role.name),
        projectId: user.project?.id || '',
        departmentIds: user.departments.map(dept => dept.id)
      });
    } else {
      setFormData({
        username: '',
        email: '',
        password: '',
        status: 'ACTIVE',
        roles: ['USER'],
        projectId: '',
        departmentIds: []
      });
    }
    setErrors({});
  }, [user, open]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.username.trim()) {
      newErrors.username = 'Username is required';
    }

    if (!formData.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email is invalid';
    }

    if (!user && !formData.password.trim()) {
      newErrors.password = 'Password is required';
    }

    if (formData.password && formData.password.length < 8) {
      newErrors.password = 'Password must be at least 8 characters';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = () => {
    if (!validateForm()) return;

    const submitData = {
      username: formData.username,
      email: formData.email,
      status: formData.status,
      roles: formData.roles,
      projectId: formData.projectId || null,
      departmentIds: formData.departmentIds,
      ...(formData.password && { password: formData.password })
    };

    onSubmit(submitData);
  };

  const handleRoleChange = (role: string) => {
    const newRoles = formData.roles.includes(role)
      ? formData.roles.filter(r => r !== role)
      : [...formData.roles, role];
    
    setFormData({ ...formData, roles: newRoles });
  };

  const availableRoles = ['USER', 'PROJECT_ADMIN', 'DEPARTMENT_ADMIN', 'SYSTEM_ADMIN'];

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        {user ? 'Edit User' : 'Create User'}
      </DialogTitle>
      
      <DialogContent>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
          <TextField
            label="Username"
            value={formData.username}
            onChange={(e) => setFormData({ ...formData, username: e.target.value })}
            error={!!errors.username}
            helperText={errors.username}
            fullWidth
            required
          />
          
          <TextField
            label="Email"
            type="email"
            value={formData.email}
            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            error={!!errors.email}
            helperText={errors.email}
            fullWidth
            required
          />
          
          <TextField
            label={user ? 'New Password (leave blank to keep current)' : 'Password'}
            type="password"
            value={formData.password}
            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
            error={!!errors.password}
            helperText={errors.password}
            fullWidth
            required={!user}
          />
          
          <FormControl fullWidth>
            <InputLabel>Status</InputLabel>
            <Select
              value={formData.status}
              onChange={(e) => setFormData({ ...formData, status: e.target.value })}
              label="Status"
            >
              <MenuItem value="ACTIVE">Active</MenuItem>
              <MenuItem value="INACTIVE">Inactive</MenuItem>
            </Select>
          </FormControl>
          
          <Box>
            <Typography variant="subtitle2" gutterBottom>
              Roles
            </Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {availableRoles.map((role) => (
                <FormControlLabel
                  key={role}
                  control={
                    <Switch
                      checked={formData.roles.includes(role)}
                      onChange={() => handleRoleChange(role)}
                      size="small"
                    />
                  }
                  label={role.replace('_', ' ')}
                />
              ))}
            </Box>
          </Box>
          
          <FormControl fullWidth>
            <InputLabel>Project</InputLabel>
            <Select
              value={formData.projectId}
              onChange={(e) => setFormData({ ...formData, projectId: e.target.value })}
              label="Project"
            >
              <MenuItem value="">None</MenuItem>
              {projects.filter(p => p.status === 'ACTIVE').map((project) => (
                <MenuItem key={project.id} value={project.id}>
                  {project.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          
          <Autocomplete
            multiple
            options={departments.filter(d => d.status === 'ACTIVE')}
            getOptionLabel={(option) => option.name}
            value={departments.filter(d => formData.departmentIds.includes(d.id))}
            onChange={(_, newValue) => {
              setFormData({
                ...formData,
                departmentIds: newValue.map(dept => dept.id)
              });
            }}
            renderTags={(value, getTagProps) =>
              value.map((option, index) => (
                <Chip
                  variant="outlined"
                  label={option.name}
                  {...getTagProps({ index })}
                  key={option.id}
                />
              ))
            }
            renderInput={(params) => (
              <TextField
                {...params}
                label="Departments"
                placeholder="Select departments"
              />
            )}
          />
        </Box>
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button onClick={handleSubmit} variant="contained">
          {user ? 'Update' : 'Create'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
import React from 'react';
import { Button, Box } from '@mui/material';
import { Link, useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { logout } from '../../store/authSlice';
import { useAuth } from '../../hooks/useAuth';
import RoleBasedComponent from '../common/RoleBasedComponent';

const Navigation: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const handleLogout = () => {
    dispatch(logout());
    navigate('/login');
  };

  if (!isAuthenticated) {
    return (
      <Box>
        <Button color="inherit" component={Link} to="/login">
          Login
        </Button>
        <Button color="inherit" component={Link} to="/register">
          Register
        </Button>
      </Box>
    );
  }

  return (
    <Box>
      <Button color="inherit" component={Link} to="/search">
        Search
      </Button>
      <Button color="inherit" component={Link} to="/documents">
        Documents
      </Button>
      <Button color="inherit" component={Link} to="/profile">
        Profile
      </Button>
      <RoleBasedComponent requiredRoles={['SYSTEM_ADMIN', 'PROJECT_ADMIN', 'DEPARTMENT_ADMIN']}>
        <Button color="inherit" component={Link} to="/admin">
          Admin
        </Button>
      </RoleBasedComponent>
      <Button color="inherit" onClick={handleLogout}>
        Logout
      </Button>
    </Box>
  );
};

export default Navigation;
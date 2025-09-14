import React from 'react';
import { Button, Box } from '@mui/material';
import { Link, useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { RootState } from '../../store';
import { logout } from '../../store/authSlice';

const Navigation: React.FC = () => {
  const { isAuthenticated, user } = useSelector((state: RootState) => state.auth);
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

  const isAdmin = user?.roles.some(role => 
    ['SYSTEM_ADMIN', 'PROJECT_ADMIN', 'DEPARTMENT_ADMIN'].includes(role.name)
  );

  return (
    <Box>
      <Button color="inherit" component={Link} to="/search">
        Search
      </Button>
      <Button color="inherit" component={Link} to="/documents">
        Documents
      </Button>
      {isAdmin && (
        <Button color="inherit" component={Link} to="/admin">
          Admin
        </Button>
      )}
      <Button color="inherit" onClick={handleLogout}>
        Logout
      </Button>
    </Box>
  );
};

export default Navigation;
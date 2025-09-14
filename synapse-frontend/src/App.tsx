import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';
import { Provider } from 'react-redux';
import { store } from './store';
import Layout from './components/layout/Layout';
import ProtectedRoute from './components/auth/ProtectedRoute';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import { useDispatch, useSelector } from 'react-redux';
import { setUser } from './store/authSlice';
import { authApi } from './utils/api';
import { RootState } from './store';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

const AppContent: React.FC = () => {
  const dispatch = useDispatch();
  const { token, isAuthenticated } = useSelector((state: RootState) => state.auth);

  useEffect(() => {
    const initAuth = async () => {
      if (token && !isAuthenticated) {
        try {
          const user = await authApi.getCurrentUser();
          dispatch(setUser(user));
        } catch (error) {
          localStorage.removeItem('token');
        }
      }
    };

    initAuth();
  }, [token, isAuthenticated, dispatch]);

  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<Layout />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route 
            path="dashboard" 
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="search" 
            element={
              <ProtectedRoute>
                <div>Search Page (Coming Soon)</div>
              </ProtectedRoute>
            } 
          />
          <Route 
            path="documents" 
            element={
              <ProtectedRoute>
                <div>Documents Page (Coming Soon)</div>
              </ProtectedRoute>
            } 
          />
          <Route 
            path="admin" 
            element={
              <ProtectedRoute requiredRoles={['SYSTEM_ADMIN', 'PROJECT_ADMIN', 'DEPARTMENT_ADMIN']}>
                <div>Admin Page (Coming Soon)</div>
              </ProtectedRoute>
            } 
          />
        </Route>
      </Routes>
    </Router>
  );
};

const App: React.FC = () => {
  return (
    <Provider store={store}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <AppContent />
      </ThemeProvider>
    </Provider>
  );
};

export default App;
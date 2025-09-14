import { useSelector, useDispatch } from 'react-redux';
import { RootState } from '../store';
import { loginStart, loginSuccess, loginFailure, logout } from '../store/slices/authSlice';
import { apiClient } from '../utils/api';
import { API_CONFIG } from '../config/api';
import { User } from '../types';

export const useAuth = () => {
  const dispatch = useDispatch();
  const { user, token, isAuthenticated, loading } = useSelector((state: RootState) => state.auth);

  const login = async (email: string, password: string) => {
    try {
      dispatch(loginStart());
      
      const response = await apiClient.post<{ user: User; token: string }>(
        API_CONFIG.ENDPOINTS.AUTH.LOGIN,
        { email, password }
      );
      
      dispatch(loginSuccess(response));
      return { success: true };
    } catch (error: any) {
      dispatch(loginFailure());
      return { 
        success: false, 
        error: error.response?.data?.message || 'Login failed' 
      };
    }
  };

  const register = async (userData: { username: string; email: string; password: string }) => {
    try {
      dispatch(loginStart());
      
      const response = await apiClient.post<{ user: User; token: string }>(
        API_CONFIG.ENDPOINTS.AUTH.REGISTER,
        userData
      );
      
      dispatch(loginSuccess(response));
      return { success: true };
    } catch (error: any) {
      dispatch(loginFailure());
      return { 
        success: false, 
        error: error.response?.data?.message || 'Registration failed' 
      };
    }
  };

  const logoutUser = () => {
    dispatch(logout());
  };

  const hasRole = (roleName: string): boolean => {
    return user?.roles.some(role => role.name === roleName) || false;
  };

  const hasAnyRole = (roleNames: string[]): boolean => {
    return roleNames.some(roleName => hasRole(roleName));
  };

  return {
    user,
    token,
    isAuthenticated,
    loading,
    login,
    register,
    logout: logoutUser,
    hasRole,
    hasAnyRole,
  };
};
import { useEffect } from 'react';
import { useDispatch } from 'react-redux';
import { logout } from '../store/slices/authSlice';
import { apiClient } from '../utils/api';
import { API_CONFIG, STORAGE_KEYS } from '../config/api';

export const useTokenRefresh = () => {
  const dispatch = useDispatch();

  useEffect(() => {
    const refreshToken = async () => {
      try {
        const refreshToken = localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN);
        if (!refreshToken) return;

        const response = await apiClient.post<{ token: string }>(
          API_CONFIG.ENDPOINTS.AUTH.REFRESH,
          { refreshToken }
        );

        localStorage.setItem(STORAGE_KEYS.TOKEN, response.token);
      } catch (error) {
        dispatch(logout());
      }
    };

    // Refresh token every 30 minutes
    const interval = setInterval(refreshToken, 30 * 60 * 1000);
    
    return () => clearInterval(interval);
  }, [dispatch]);
};
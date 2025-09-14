import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { RootState } from '../../store';
import { clearAuth, setUser } from '../../store/authSlice';
import { authApi } from '../../utils/api';

const TokenManager: React.FC = () => {
  const dispatch = useDispatch();
  const { token, isAuthenticated } = useSelector((state: RootState) => state.auth);

  useEffect(() => {
    const checkTokenValidity = async () => {
      if (token && !isAuthenticated) {
        try {
          const user = await authApi.getCurrentUser();
          dispatch(setUser(user));
        } catch (error) {
          dispatch(clearAuth());
          localStorage.removeItem('token');
        }
      }
    };

    checkTokenValidity();
  }, [token, isAuthenticated, dispatch]);

  useEffect(() => {
    const refreshInterval = setInterval(async () => {
      if (token && isAuthenticated) {
        try {
          await authApi.getCurrentUser();
        } catch (error) {
          dispatch(clearAuth());
          localStorage.removeItem('token');
        }
      }
    }, 15 * 60 * 1000); // Check every 15 minutes

    return () => clearInterval(refreshInterval);
  }, [token, isAuthenticated, dispatch]);

  return null;
};

export default TokenManager;
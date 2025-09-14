import { renderHook, act } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { useAuth } from '../../hooks/useAuth';
import authSlice from '../../store/slices/authSlice';
import uiSlice from '../../store/slices/uiSlice';

const createTestStore = () => {
  return configureStore({
    reducer: {
      auth: authSlice,
      ui: uiSlice,
    },
  });
};

const wrapper = ({ children }: { children: React.ReactNode }) => {
  const store = createTestStore();
  return <Provider store={store}>{children}</Provider>;
};

describe('useAuth Hook', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  test('returns initial unauthenticated state', () => {
    const { result } = renderHook(() => useAuth(), { wrapper });
    
    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.user).toBe(null);
    expect(result.current.token).toBe(null);
    expect(result.current.loading).toBe(false);
  });

  test('hasRole returns false when user has no roles', () => {
    const { result } = renderHook(() => useAuth(), { wrapper });
    
    expect(result.current.hasRole('ADMIN')).toBe(false);
  });

  test('hasAnyRole returns false when user has no roles', () => {
    const { result } = renderHook(() => useAuth(), { wrapper });
    
    expect(result.current.hasAnyRole(['ADMIN', 'USER'])).toBe(false);
  });

  test('logout clears user data', () => {
    const { result } = renderHook(() => useAuth(), { wrapper });
    
    act(() => {
      result.current.logout();
    });
    
    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.user).toBe(null);
    expect(result.current.token).toBe(null);
  });
});
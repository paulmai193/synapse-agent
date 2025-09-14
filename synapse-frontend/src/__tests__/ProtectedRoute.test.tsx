import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { BrowserRouter } from 'react-router-dom';
import { configureStore } from '@reduxjs/toolkit';
import ProtectedRoute from '../components/auth/ProtectedRoute';
import authReducer from '../store/authSlice';

const createTestStore = (initialState: any) => {
  return configureStore({
    reducer: {
      auth: authReducer,
    },
    preloadedState: {
      auth: initialState,
    },
  });
};

const renderWithProviders = (component: React.ReactElement, authState: any) => {
  const store = createTestStore(authState);
  return render(
    <Provider store={store}>
      <BrowserRouter>
        {component}
      </BrowserRouter>
    </Provider>
  );
};

describe('ProtectedRoute Component', () => {
  test('renders children when authenticated', () => {
    const authState = {
      isAuthenticated: true,
      user: { roles: [{ name: 'USER' }] },
      token: 'test-token',
      loading: false,
    };

    renderWithProviders(
      <ProtectedRoute>
        <div>Protected Content</div>
      </ProtectedRoute>,
      authState
    );

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  test('redirects to login when not authenticated', () => {
    const authState = {
      isAuthenticated: false,
      user: null,
      token: null,
      loading: false,
    };

    renderWithProviders(
      <ProtectedRoute>
        <div>Protected Content</div>
      </ProtectedRoute>,
      authState
    );

    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
  });

  test('renders children when user has required role', () => {
    const authState = {
      isAuthenticated: true,
      user: { roles: [{ name: 'SYSTEM_ADMIN' }] },
      token: 'test-token',
      loading: false,
    };

    renderWithProviders(
      <ProtectedRoute requiredRoles={['SYSTEM_ADMIN']}>
        <div>Admin Content</div>
      </ProtectedRoute>,
      authState
    );

    expect(screen.getByText('Admin Content')).toBeInTheDocument();
  });
});
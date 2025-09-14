import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { Provider } from 'react-redux';
import { BrowserRouter } from 'react-router-dom';
import { configureStore } from '@reduxjs/toolkit';
import DocumentUpload from '../../components/documents/DocumentUpload';
import authSlice from '../../store/slices/authSlice';
import uiSlice from '../../store/slices/uiSlice';

const createTestStore = () => {
  return configureStore({
    reducer: {
      auth: authSlice,
      ui: uiSlice,
    },
    preloadedState: {
      auth: {
        user: {
          id: 1,
          username: 'testuser',
          email: 'test@example.com',
          roles: [{ id: 1, name: 'USER' }],
          status: 'ACTIVE',
          projectId: 1,
          departmentIds: [1, 2],
        },
        token: 'test-token',
        isAuthenticated: true,
        loading: false,
      },
      ui: {
        theme: 'light',
        sidebarOpen: true,
        loading: false,
        notifications: [],
      },
    },
  });
};

const renderWithProviders = (component: React.ReactElement) => {
  const store = createTestStore();
  return render(
    <Provider store={store}>
      <BrowserRouter>
        {component}
      </BrowserRouter>
    </Provider>
  );
};

describe('DocumentUpload Component', () => {
  const mockOnSuccess = jest.fn();
  const mockOnCancel = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders upload form', () => {
    renderWithProviders(
      <DocumentUpload onSuccess={mockOnSuccess} onCancel={mockOnCancel} />
    );
    
    expect(screen.getByText('Upload Document')).toBeInTheDocument();
    expect(screen.getByText('Drag & drop files here')).toBeInTheDocument();
    expect(screen.getByLabelText(/document title/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/visibility/i)).toBeInTheDocument();
  });

  test('updates title when file is selected', () => {
    renderWithProviders(
      <DocumentUpload onSuccess={mockOnSuccess} onCancel={mockOnCancel} />
    );
    
    const titleInput = screen.getByLabelText(/document title/i);
    expect(titleInput).toHaveValue('');
  });

  test('shows department selection when visibility is set to department', () => {
    renderWithProviders(
      <DocumentUpload onSuccess={mockOnSuccess} onCancel={mockOnCancel} />
    );
    
    const visibilitySelect = screen.getByLabelText(/visibility/i);
    fireEvent.mouseDown(visibilitySelect);
    
    const departmentOption = screen.getByText('Department(s)');
    fireEvent.click(departmentOption);
    
    expect(screen.getByLabelText(/departments/i)).toBeInTheDocument();
  });

  test('calls onCancel when cancel button is clicked', () => {
    renderWithProviders(
      <DocumentUpload onSuccess={mockOnSuccess} onCancel={mockOnCancel} />
    );
    
    const cancelButton = screen.getByText('Cancel');
    fireEvent.click(cancelButton);
    
    expect(mockOnCancel).toHaveBeenCalledTimes(1);
  });

  test('upload button is disabled when no file is selected', () => {
    renderWithProviders(
      <DocumentUpload onSuccess={mockOnSuccess} onCancel={mockOnCancel} />
    );
    
    const uploadButton = screen.getByText('Upload');
    expect(uploadButton).toBeDisabled();
  });
});
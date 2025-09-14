import { render, screen, fireEvent } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import DocumentList from '../components/documents/DocumentList';
import authReducer from '../store/authSlice';

// Mock document API
jest.mock('../utils/documentApi', () => ({
  documentApi: {
    getDocuments: jest.fn().mockResolvedValue({
      content: [
        {
          id: '1',
          title: 'Test Document',
          status: 'ACTIVE',
          processingStatus: 'COMPLETED',
          accessControl: { visibility: 'PROJECT' },
          createdAt: '2024-01-01T00:00:00Z',
          metadata: { tags: ['test'] }
        }
      ],
      totalElements: 1
    }),
    updateDocumentStatus: jest.fn(),
    deleteDocument: jest.fn(),
  },
}));

const createTestStore = () => {
  return configureStore({
    reducer: {
      auth: authReducer,
    },
    preloadedState: {
      auth: {
        isAuthenticated: true,
        user: { roles: [{ name: 'USER' }] },
        token: 'test-token',
        loading: false,
      },
    },
  });
};

const renderWithStore = (component: React.ReactElement) => {
  const store = createTestStore();
  return render(
    <Provider store={store}>
      {component}
    </Provider>
  );
};

describe('DocumentList Component', () => {
  test('renders document list', async () => {
    renderWithStore(<DocumentList />);
    
    expect(screen.getByText('Documents')).toBeInTheDocument();
    expect(screen.getByLabelText('Search documents...')).toBeInTheDocument();
  });

  test('filters documents by search term', async () => {
    renderWithStore(<DocumentList />);
    
    const searchInput = screen.getByLabelText('Search documents...');
    fireEvent.change(searchInput, { target: { value: 'test' } });
    
    expect(searchInput).toHaveValue('test');
  });

  test('displays table headers', () => {
    renderWithStore(<DocumentList />);
    
    expect(screen.getByText('Title')).toBeInTheDocument();
    expect(screen.getByText('Status')).toBeInTheDocument();
    expect(screen.getByText('Processing')).toBeInTheDocument();
    expect(screen.getByText('Visibility')).toBeInTheDocument();
    expect(screen.getByText('Created')).toBeInTheDocument();
    expect(screen.getByText('Actions')).toBeInTheDocument();
  });
});
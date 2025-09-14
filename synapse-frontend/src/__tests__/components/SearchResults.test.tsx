import { render, screen, fireEvent } from '@testing-library/react';
import { Provider } from 'react-redux';
import { BrowserRouter } from 'react-router-dom';
import { configureStore } from '@reduxjs/toolkit';
import SearchResults from '../../components/search/SearchResults';
import authSlice from '../../store/slices/authSlice';
import uiSlice from '../../store/slices/uiSlice';
import { SearchResult } from '../../types';

const createTestStore = () => {
  return configureStore({
    reducer: {
      auth: authSlice,
      ui: uiSlice,
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

const mockResults: SearchResult[] = [
  {
    documentId: '1',
    title: 'Test Document',
    content: 'This is a test document content with search terms.',
    relevanceScore: 0.85,
    source: 'upload',
  },
  {
    documentId: '2',
    title: 'Another Document',
    content: 'Another document with different content.',
    relevanceScore: 0.72,
    source: 'confluence',
  },
];

describe('SearchResults Component', () => {
  const mockOnSearch = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders loading state', () => {
    renderWithProviders(
      <SearchResults
        results={[]}
        loading={true}
        query="test"
        onSearch={mockOnSearch}
      />
    );
    
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  test('renders empty state when no query', () => {
    renderWithProviders(
      <SearchResults
        results={[]}
        loading={false}
        query=""
        onSearch={mockOnSearch}
      />
    );
    
    expect(screen.getByText('Enter a search query to find documents')).toBeInTheDocument();
  });

  test('renders no results state', () => {
    renderWithProviders(
      <SearchResults
        results={[]}
        loading={false}
        query="nonexistent"
        onSearch={mockOnSearch}
      />
    );
    
    expect(screen.getByText('No results found for "nonexistent"')).toBeInTheDocument();
  });

  test('renders search results', () => {
    renderWithProviders(
      <SearchResults
        results={mockResults}
        loading={false}
        query="test"
        onSearch={mockOnSearch}
      />
    );
    
    expect(screen.getByText('Found 2 results for "test"')).toBeInTheDocument();
    expect(screen.getByText('Test Document')).toBeInTheDocument();
    expect(screen.getByText('Another Document')).toBeInTheDocument();
  });

  test('highlights search terms in results', () => {
    renderWithProviders(
      <SearchResults
        results={mockResults}
        loading={false}
        query="test"
        onSearch={mockOnSearch}
      />
    );
    
    const highlightedElements = screen.getAllByText('test');
    expect(highlightedElements.length).toBeGreaterThan(0);
  });

  test('shows relevance scores', () => {
    renderWithProviders(
      <SearchResults
        results={mockResults}
        loading={false}
        query="test"
        onSearch={mockOnSearch}
      />
    );
    
    expect(screen.getByText('85% match')).toBeInTheDocument();
    expect(screen.getByText('72% match')).toBeInTheDocument();
  });

  test('renders feedback buttons', () => {
    renderWithProviders(
      <SearchResults
        results={mockResults}
        loading={false}
        query="test"
        onSearch={mockOnSearch}
      />
    );
    
    const helpfulButtons = screen.getAllByText('Helpful');
    const notHelpfulButtons = screen.getAllByText('Not Helpful');
    
    expect(helpfulButtons).toHaveLength(2);
    expect(notHelpfulButtons).toHaveLength(2);
  });
});
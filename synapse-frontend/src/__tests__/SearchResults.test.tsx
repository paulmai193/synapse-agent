import { render, screen } from '@testing-library/react';
import SearchResults from '../components/search/SearchResults';
import { SearchResponse } from '../types/search';

// Mock search API
jest.mock('../utils/searchApi', () => ({
  searchApi: {
    provideFeedback: jest.fn(),
  },
}));

describe('SearchResults Component', () => {
  const mockSearchResponse: SearchResponse = {
    results: [
      {
        id: '1',
        title: 'Test Document',
        content: 'This is test content for the document',
        score: 0.95,
        documentId: 'doc1',
        language: 'en',
        source: 'Upload',
        createdAt: '2024-01-01T00:00:00Z',
        highlights: ['test']
      }
    ],
    totalCount: 1,
    query: 'test',
    language: 'en',
    processingTime: 150
  };

  test('renders search results', () => {
    render(<SearchResults searchResponse={mockSearchResponse} loading={false} />);
    
    expect(screen.getByText('Test Document')).toBeInTheDocument();
    expect(screen.getByText(/This is test content/)).toBeInTheDocument();
    expect(screen.getByText('Score: 0.95')).toBeInTheDocument();
    expect(screen.getByText('EN')).toBeInTheDocument();
  });

  test('shows loading state', () => {
    render(<SearchResults searchResponse={null} loading={true} />);
    
    expect(screen.getByText('Searching...')).toBeInTheDocument();
  });

  test('shows empty state when no results', () => {
    const emptyResponse: SearchResponse = {
      ...mockSearchResponse,
      results: [],
      totalCount: 0
    };
    
    render(<SearchResults searchResponse={emptyResponse} loading={false} />);
    
    expect(screen.getByText(/No results found for "test"/)).toBeInTheDocument();
  });

  test('shows initial state when no search performed', () => {
    render(<SearchResults searchResponse={null} loading={false} />);
    
    expect(screen.getByText('Enter a search query to find relevant documents')).toBeInTheDocument();
  });
});
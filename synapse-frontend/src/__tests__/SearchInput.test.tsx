import { render, screen, fireEvent } from '@testing-library/react';
import SearchInput from '../components/search/SearchInput';

// Mock search API
jest.mock('../utils/searchApi', () => ({
  searchApi: {
    getSearchHistory: jest.fn().mockResolvedValue([
      { id: '1', query: 'test query', language: 'en', timestamp: '2024-01-01', resultCount: 5 }
    ]),
  },
}));

describe('SearchInput Component', () => {
  const mockOnSearch = jest.fn();

  beforeEach(() => {
    mockOnSearch.mockClear();
  });

  test('renders search input', () => {
    render(<SearchInput onSearch={mockOnSearch} />);
    
    expect(screen.getByLabelText('Search documents...')).toBeInTheDocument();
    expect(screen.getByLabelText('Language')).toBeInTheDocument();
  });

  test('calls onSearch when search button clicked', () => {
    render(<SearchInput onSearch={mockOnSearch} />);
    
    const searchInput = screen.getByLabelText('Search documents...');
    const searchButton = screen.getByRole('button', { name: '' }); // Search icon button
    
    fireEvent.change(searchInput, { target: { value: 'test query' } });
    fireEvent.click(searchButton);
    
    expect(mockOnSearch).toHaveBeenCalledWith({
      query: 'test query',
      language: undefined,
      limit: 20,
      offset: 0,
    });
  });

  test('calls onSearch when Enter key pressed', () => {
    render(<SearchInput onSearch={mockOnSearch} />);
    
    const searchInput = screen.getByLabelText('Search documents...');
    
    fireEvent.change(searchInput, { target: { value: 'test query' } });
    fireEvent.keyPress(searchInput, { key: 'Enter', code: 'Enter' });
    
    expect(mockOnSearch).toHaveBeenCalledWith({
      query: 'test query',
      language: undefined,
      limit: 20,
      offset: 0,
    });
  });

  test('disables search when query is empty', () => {
    render(<SearchInput onSearch={mockOnSearch} />);
    
    const searchButton = screen.getByRole('button', { name: '' });
    expect(searchButton).toBeDisabled();
  });
});
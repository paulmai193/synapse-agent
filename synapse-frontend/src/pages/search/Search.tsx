import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  TextField,
  InputAdornment,
  Paper,
  Chip,
  Grid,
  Card,
  CardContent,
  Pagination,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Autocomplete,
} from '@mui/material';
import { Search as SearchIcon, History, Bookmark } from '@mui/icons-material';
import { SearchResult } from '../../types';
import { apiClient } from '../../utils/api';
import { API_CONFIG } from '../../config/api';
import SearchResults from '../../components/search/SearchResults';
import SearchFilters from '../../components/search/SearchFilters';
import SearchHistory from '../../components/search/SearchHistory';

const Search: React.FC = () => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<SearchResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const [searchHistory, setSearchHistory] = useState<string[]>([]);
  const [savedSearches, setSavedSearches] = useState<string[]>([]);
  const [filters, setFilters] = useState({
    language: 'all',
    source: 'all',
    dateRange: 'all',
  });
  const [page, setPage] = useState(1);
  const [totalResults, setTotalResults] = useState(0);

  const resultsPerPage = 10;

  useEffect(() => {
    loadSearchHistory();
    loadSavedSearches();
  }, []);

  useEffect(() => {
    if (query.length > 2) {
      fetchSuggestions();
    } else {
      setSuggestions([]);
    }
  }, [query]);

  const loadSearchHistory = () => {
    const history = JSON.parse(localStorage.getItem('search_history') || '[]');
    setSearchHistory(history.slice(0, 10)); // Keep last 10 searches
  };

  const loadSavedSearches = () => {
    const saved = JSON.parse(localStorage.getItem('saved_searches') || '[]');
    setSavedSearches(saved);
  };

  const saveToHistory = (searchQuery: string) => {
    const history = JSON.parse(localStorage.getItem('search_history') || '[]');
    const updatedHistory = [searchQuery, ...history.filter(h => h !== searchQuery)].slice(0, 10);
    localStorage.setItem('search_history', JSON.stringify(updatedHistory));
    setSearchHistory(updatedHistory);
  };

  const fetchSuggestions = async () => {
    try {
      const response = await apiClient.get<string[]>(
        `${API_CONFIG.ENDPOINTS.SEARCH}/suggestions?query=${encodeURIComponent(query)}`
      );
      setSuggestions(response);
    } catch (error) {
      console.error('Failed to fetch suggestions:', error);
    }
  };

  const handleSearch = async (searchQuery: string = query, pageNum: number = 1) => {
    if (!searchQuery.trim()) return;

    setLoading(true);
    try {
      const searchParams = {
        query: searchQuery,
        limit: resultsPerPage,
        offset: (pageNum - 1) * resultsPerPage,
        language: filters.language !== 'all' ? filters.language : undefined,
        source: filters.source !== 'all' ? filters.source : undefined,
      };

      const response = await apiClient.post<{
        results: SearchResult[];
        totalCount: number;
        responseTimeMs: number;
      }>(API_CONFIG.ENDPOINTS.SEARCH, searchParams);

      setResults(response.results);
      setTotalResults(response.totalCount);
      setPage(pageNum);
      
      if (pageNum === 1) {
        saveToHistory(searchQuery);
      }
    } catch (error) {
      console.error('Search failed:', error);
      setResults([]);
      setTotalResults(0);
    } finally {
      setLoading(false);
    }
  };

  const handleSuggestionClick = (suggestion: string) => {
    setQuery(suggestion);
    handleSearch(suggestion);
  };

  const handleHistoryClick = (historyQuery: string) => {
    setQuery(historyQuery);
    handleSearch(historyQuery);
  };

  const handleSaveSearch = () => {
    if (!query.trim() || savedSearches.includes(query)) return;
    
    const updated = [...savedSearches, query];
    localStorage.setItem('saved_searches', JSON.stringify(updated));
    setSavedSearches(updated);
  };

  const handleRemoveSavedSearch = (searchQuery: string) => {
    const updated = savedSearches.filter(s => s !== searchQuery);
    localStorage.setItem('saved_searches', JSON.stringify(updated));
    setSavedSearches(updated);
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  return (
    <Box>
      <Typography variant="h4" component="h1" gutterBottom>
        Search
      </Typography>

      <Paper sx={{ p: 3, mb: 3 }}>
        <Autocomplete
          freeSolo
          options={suggestions}
          value={query}
          onInputChange={(_, newValue) => setQuery(newValue)}
          renderInput={(params) => (
            <TextField
              {...params}
              fullWidth
              placeholder="Search documents in any language..."
              InputProps={{
                ...params.InputProps,
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                ),
              }}
              onKeyPress={handleKeyPress}
            />
          )}
          renderOption={(props, option) => (
            <li {...props} onClick={() => handleSuggestionClick(option)}>
              {option}
            </li>
          )}
        />

        <Box sx={{ mt: 2, display: 'flex', gap: 2, alignItems: 'center' }}>
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>Language</InputLabel>
            <Select
              value={filters.language}
              onChange={(e) => setFilters({ ...filters, language: e.target.value })}
              label="Language"
            >
              <MenuItem value="all">All Languages</MenuItem>
              <MenuItem value="en">English</MenuItem>
              <MenuItem value="vi">Vietnamese</MenuItem>
              <MenuItem value="ja">Japanese</MenuItem>
            </Select>
          </FormControl>

          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>Source</InputLabel>
            <Select
              value={filters.source}
              onChange={(e) => setFilters({ ...filters, source: e.target.value })}
              label="Source"
            >
              <MenuItem value="all">All Sources</MenuItem>
              <MenuItem value="upload">Uploaded</MenuItem>
              <MenuItem value="confluence">Confluence</MenuItem>
              <MenuItem value="repository">Repository</MenuItem>
            </Select>
          </FormControl>

          {query && !savedSearches.includes(query) && (
            <Chip
              icon={<Bookmark />}
              label="Save Search"
              onClick={handleSaveSearch}
              clickable
              variant="outlined"
            />
          )}
        </Box>
      </Paper>

      <Grid container spacing={3}>
        <Grid item xs={12} md={9}>
          <SearchResults
            results={results}
            loading={loading}
            query={query}
            onSearch={handleSearch}
          />
          
          {totalResults > resultsPerPage && (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
              <Pagination
                count={Math.ceil(totalResults / resultsPerPage)}
                page={page}
                onChange={(_, newPage) => handleSearch(query, newPage)}
              />
            </Box>
          )}
        </Grid>

        <Grid item xs={12} md={3}>
          <SearchHistory
            history={searchHistory}
            savedSearches={savedSearches}
            onHistoryClick={handleHistoryClick}
            onRemoveSaved={handleRemoveSavedSearch}
          />
        </Grid>
      </Grid>
    </Box>
  );
};

export default Search;
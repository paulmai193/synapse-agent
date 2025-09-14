import React, { useState, useEffect } from 'react';
import {
  TextField,
  Autocomplete,
  Box,
  IconButton,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
} from '@mui/material';
import { Search, FilterList, History } from '@mui/icons-material';
import { SearchRequest, SearchHistory } from '../../types/search';
import { searchApi } from '../../utils/searchApi';

interface SearchInputProps {
  onSearch: (request: SearchRequest) => void;
  loading?: boolean;
}

const SearchInput: React.FC<SearchInputProps> = ({ onSearch, loading = false }) => {
  const [query, setQuery] = useState('');
  const [language, setLanguage] = useState('auto');
  const [searchHistory, setSearchHistory] = useState<SearchHistory[]>([]);
  const [showFilters, setShowFilters] = useState(false);

  useEffect(() => {
    loadSearchHistory();
  }, []);

  const loadSearchHistory = async () => {
    try {
      const history = await searchApi.getSearchHistory();
      setSearchHistory(history.slice(0, 10)); // Show last 10 searches
    } catch (error) {
      console.error('Failed to load search history:', error);
    }
  };

  const handleSearch = () => {
    if (query.trim()) {
      const request: SearchRequest = {
        query: query.trim(),
        language: language === 'auto' ? undefined : language,
        limit: 20,
        offset: 0,
      };
      onSearch(request);
    }
  };

  const handleKeyPress = (event: React.KeyboardEvent) => {
    if (event.key === 'Enter') {
      handleSearch();
    }
  };

  const handleHistorySelect = (historyItem: SearchHistory) => {
    setQuery(historyItem.query);
    setLanguage(historyItem.language || 'auto');
  };

  const languages = [
    { code: 'auto', name: 'Auto-detect' },
    { code: 'en', name: 'English' },
    { code: 'vi', name: 'Vietnamese' },
    { code: 'ja', name: 'Japanese' },
    { code: 'zh', name: 'Chinese' },
    { code: 'ko', name: 'Korean' },
  ];

  return (
    <Box sx={{ mb: 3 }}>
      <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
        <Autocomplete
          freeSolo
          options={searchHistory.map(h => h.query)}
          value={query}
          onInputChange={(_, newValue) => setQuery(newValue || '')}
          renderInput={(params) => (
            <TextField
              {...params}
              label="Search documents..."
              variant="outlined"
              onKeyPress={handleKeyPress}
              disabled={loading}
            />
          )}
          sx={{ flexGrow: 1 }}
        />
        
        <FormControl sx={{ minWidth: 120 }}>
          <InputLabel>Language</InputLabel>
          <Select
            value={language}
            onChange={(e) => setLanguage(e.target.value)}
            disabled={loading}
          >
            {languages.map((lang) => (
              <MenuItem key={lang.code} value={lang.code}>
                {lang.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <IconButton 
          onClick={handleSearch} 
          disabled={!query.trim() || loading}
          color="primary"
        >
          <Search />
        </IconButton>

        <IconButton onClick={() => setShowFilters(!showFilters)}>
          <FilterList />
        </IconButton>
      </Box>

      {searchHistory.length > 0 && (
        <Box sx={{ mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
            <History fontSize="small" />
            <span>Recent searches:</span>
          </Box>
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            {searchHistory.slice(0, 5).map((item) => (
              <Chip
                key={item.id}
                label={item.query}
                size="small"
                onClick={() => handleHistorySelect(item)}
                clickable
              />
            ))}
          </Box>
        </Box>
      )}
    </Box>
  );
};

export default SearchInput;
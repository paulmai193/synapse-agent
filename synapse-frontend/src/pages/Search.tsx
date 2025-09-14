import React, { useState, useEffect } from 'react';
import { Box, Typography } from '@mui/material';
import SearchInput from '../components/search/SearchInput';
import SearchResults from '../components/search/SearchResults';
import SearchFilters from '../components/search/SearchFilters';
import { SearchRequest, SearchResponse, SearchFilters as SearchFiltersType } from '../types/search';
import { searchApi } from '../utils/searchApi';

const Search: React.FC = () => {
  const [searchResponse, setSearchResponse] = useState<SearchResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [showFilters, setShowFilters] = useState(false);
  const [filters, setFilters] = useState<SearchFiltersType>({});

  // Load search analytics on component mount
  useEffect(() => {
    // Track page visit for analytics
    console.log('Search page visited');
  }, []);

  const handleSearch = async (request: SearchRequest) => {
    setLoading(true);
    try {
      const requestWithFilters = {
        ...request,
        filters: Object.keys(filters).length > 0 ? filters : undefined
      };
      
      const response = await searchApi.search(requestWithFilters);
      setSearchResponse(response);
      
      // Track search analytics
      console.log('Search performed:', {
        query: request.query,
        language: request.language,
        resultCount: response.totalCount,
        processingTime: response.processingTime
      });
    } catch (error) {
      console.error('Search failed:', error);
      setSearchResponse({
        results: [],
        totalCount: 0,
        query: request.query,
        language: request.language || 'auto',
        processingTime: 0
      });
    } finally {
      setLoading(false);
    }
  };

  const handleFiltersChange = (newFilters: SearchFiltersType) => {
    setFilters(newFilters);
    // Re-run search if there's an active query
    if (searchResponse) {
      const request: SearchRequest = {
        query: searchResponse.query,
        language: searchResponse.language === 'auto' ? undefined : searchResponse.language,
        filters: Object.keys(newFilters).length > 0 ? newFilters : undefined
      };
      handleSearch(request);
    }
  };

  return (
    <Box>
      <Typography variant="h4" component="h1" gutterBottom>
        Search Documents
      </Typography>

      <SearchInput 
        onSearch={handleSearch} 
        loading={loading}
      />

      <SearchFilters
        filters={filters}
        onFiltersChange={handleFiltersChange}
        open={showFilters}
      />

      <SearchResults 
        searchResponse={searchResponse}
        loading={loading}
      />
    </Box>
  );
};

export default Search;
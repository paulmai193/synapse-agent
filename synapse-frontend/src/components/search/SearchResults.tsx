import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Chip,
  CircularProgress,
  Button,
  Divider,
} from '@mui/material';
import { Visibility, ThumbUp, ThumbDown } from '@mui/icons-material';
import { SearchResult } from '../../types';
import { apiClient } from '../../utils/api';
import { API_CONFIG } from '../../config/api';

interface SearchResultsProps {
  results: SearchResult[];
  loading: boolean;
  query: string;
  onSearch: (query: string) => void;
}

const SearchResults: React.FC<SearchResultsProps> = ({
  results,
  loading,
  query,
  onSearch,
}) => {
  const handleFeedback = async (documentId: string, helpful: boolean) => {
    try {
      await apiClient.post(`${API_CONFIG.ENDPOINTS.SEARCH}/feedback`, {
        query,
        documentId,
        helpful,
        rating: helpful ? 5 : 1,
      });
    } catch (error) {
      console.error('Failed to submit feedback:', error);
    }
  };

  const highlightText = (text: string, searchQuery: string) => {
    if (!searchQuery) return text;
    
    const regex = new RegExp(`(${searchQuery})`, 'gi');
    const parts = text.split(regex);
    
    return parts.map((part, index) =>
      regex.test(part) ? (
        <mark key={index} style={{ backgroundColor: '#ffeb3b', padding: '0 2px' }}>
          {part}
        </mark>
      ) : (
        part
      )
    );
  };

  const formatRelevanceScore = (score: number) => {
    return Math.round(score * 100);
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!query) {
    return (
      <Box sx={{ textAlign: 'center', py: 4 }}>
        <Typography variant="h6" color="text.secondary">
          Enter a search query to find documents
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
          Search across all your documents in multiple languages
        </Typography>
      </Box>
    );
  }

  if (results.length === 0) {
    return (
      <Box sx={{ textAlign: 'center', py: 4 }}>
        <Typography variant="h6" color="text.secondary">
          No results found for "{query}"
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
          Try different keywords or check your spelling
        </Typography>
        <Box sx={{ mt: 2 }}>
          <Button onClick={() => onSearch('*')} variant="outlined" size="small">
            Browse All Documents
          </Button>
        </Box>
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Found {results.length} results for "{query}"
      </Typography>

      {results.map((result, index) => (
        <Card key={`${result.documentId}-${index}`} sx={{ mb: 2 }}>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
              <Typography variant="h6" component="h3" sx={{ flexGrow: 1 }}>
                {highlightText(result.title, query)}
              </Typography>
              <Chip
                label={`${formatRelevanceScore(result.relevanceScore)}% match`}
                size="small"
                color="primary"
                variant="outlined"
              />
            </Box>

            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              {highlightText(result.content, query)}
            </Typography>

            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
              <Chip label={result.source} size="small" variant="outlined" />
              {result.documentId && (
                <Typography variant="caption" color="text.secondary">
                  Document ID: {result.documentId}
                </Typography>
              )}
            </Box>

            <Divider sx={{ my: 1 }} />

            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Button
                startIcon={<Visibility />}
                size="small"
                variant="outlined"
              >
                View Document
              </Button>

              <Box sx={{ display: 'flex', gap: 1 }}>
                <Button
                  startIcon={<ThumbUp />}
                  size="small"
                  onClick={() => handleFeedback(result.documentId, true)}
                >
                  Helpful
                </Button>
                <Button
                  startIcon={<ThumbDown />}
                  size="small"
                  onClick={() => handleFeedback(result.documentId, false)}
                >
                  Not Helpful
                </Button>
              </Box>
            </Box>
          </CardContent>
        </Card>
      ))}
    </Box>
  );
};

export default SearchResults;
import React from 'react';
import {
  Paper,
  Typography,
  Box,
  Card,
  CardContent,
  Chip,
  IconButton,
  Divider,
} from '@mui/material';
import { ThumbUp, ThumbDown, OpenInNew } from '@mui/icons-material';
import { SearchResult, SearchResponse } from '../../types/search';
import { searchApi } from '../../utils/searchApi';

interface SearchResultsProps {
  searchResponse: SearchResponse | null;
  loading: boolean;
}

const SearchResults: React.FC<SearchResultsProps> = ({ searchResponse, loading }) => {
  const handleFeedback = async (result: SearchResult, feedback: 'helpful' | 'not_helpful') => {
    try {
      await searchApi.provideFeedback('current-search', result.id, feedback);
    } catch (error) {
      console.error('Failed to provide feedback:', error);
    }
  };

  const highlightText = (text: string, highlights?: string[]) => {
    if (!highlights || highlights.length === 0) {
      return text.substring(0, 200) + (text.length > 200 ? '...' : '');
    }
    
    let highlightedText = text;
    highlights.forEach(highlight => {
      const regex = new RegExp(`(${highlight})`, 'gi');
      highlightedText = highlightedText.replace(regex, '<mark>$1</mark>');
    });
    
    return highlightedText.substring(0, 300) + (highlightedText.length > 300 ? '...' : '');
  };

  if (loading) {
    return (
      <Paper sx={{ p: 3, textAlign: 'center' }}>
        <Typography>Searching...</Typography>
      </Paper>
    );
  }

  if (!searchResponse) {
    return (
      <Paper sx={{ p: 3, textAlign: 'center' }}>
        <Typography color="text.secondary">
          Enter a search query to find relevant documents
        </Typography>
      </Paper>
    );
  }

  if (searchResponse.results.length === 0) {
    return (
      <Paper sx={{ p: 3, textAlign: 'center' }}>
        <Typography>No results found for "{searchResponse.query}"</Typography>
        <Typography color="text.secondary" sx={{ mt: 1 }}>
          Try different keywords or check your spelling
        </Typography>
      </Paper>
    );
  }

  return (
    <Box>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Typography variant="body2" color="text.secondary">
          Found {searchResponse.totalCount} results for "{searchResponse.query}" 
          in {searchResponse.processingTime}ms
        </Typography>
      </Paper>

      {searchResponse.results.map((result, index) => (
        <Card key={result.id} sx={{ mb: 2 }}>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
              <Typography variant="h6" component="h3">
                {result.title}
              </Typography>
              <Box sx={{ display: 'flex', gap: 1 }}>
                <Chip 
                  label={`Score: ${result.score.toFixed(2)}`} 
                  size="small" 
                  color="primary" 
                />
                <Chip 
                  label={result.language.toUpperCase()} 
                  size="small" 
                  variant="outlined" 
                />
              </Box>
            </Box>

            <Typography 
              variant="body2" 
              color="text.secondary" 
              sx={{ mb: 2 }}
              dangerouslySetInnerHTML={{ 
                __html: highlightText(result.content, result.highlights) 
              }}
            />

            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Box sx={{ display: 'flex', gap: 1 }}>
                <Chip label={result.source} size="small" variant="outlined" />
                <Typography variant="caption" color="text.secondary">
                  {new Date(result.createdAt).toLocaleDateString()}
                </Typography>
              </Box>

              <Box sx={{ display: 'flex', gap: 1 }}>
                <IconButton 
                  size="small" 
                  onClick={() => handleFeedback(result, 'helpful')}
                  title="Helpful"
                >
                  <ThumbUp fontSize="small" />
                </IconButton>
                <IconButton 
                  size="small" 
                  onClick={() => handleFeedback(result, 'not_helpful')}
                  title="Not helpful"
                >
                  <ThumbDown fontSize="small" />
                </IconButton>
                <IconButton 
                  size="small" 
                  onClick={() => window.open(`/documents/${result.documentId}`, '_blank')}
                  title="Open document"
                >
                  <OpenInNew fontSize="small" />
                </IconButton>
              </Box>
            </Box>
          </CardContent>
        </Card>
      ))}
    </Box>
  );
};

export default SearchResults;
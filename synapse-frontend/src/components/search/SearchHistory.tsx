import React from 'react';
import {
  Box,
  Paper,
  Typography,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Divider,
  Chip,
} from '@mui/material';
import { History, Bookmark, Delete, Search } from '@mui/icons-material';

interface SearchHistoryProps {
  history: string[];
  savedSearches: string[];
  onHistoryClick: (query: string) => void;
  onRemoveSaved: (query: string) => void;
}

const SearchHistory: React.FC<SearchHistoryProps> = ({
  history,
  savedSearches,
  onHistoryClick,
  onRemoveSaved,
}) => {
  return (
    <Box>
      {savedSearches.length > 0 && (
        <Paper sx={{ p: 2, mb: 2 }}>
          <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Bookmark />
            Saved Searches
          </Typography>
          <List dense>
            {savedSearches.map((search, index) => (
              <ListItem
                key={index}
                button
                onClick={() => onHistoryClick(search)}
                sx={{ px: 0 }}
              >
                <ListItemText
                  primary={search}
                  primaryTypographyProps={{ variant: 'body2' }}
                />
                <ListItemSecondaryAction>
                  <IconButton
                    edge="end"
                    size="small"
                    onClick={(e) => {
                      e.stopPropagation();
                      onRemoveSaved(search);
                    }}
                  >
                    <Delete fontSize="small" />
                  </IconButton>
                </ListItemSecondaryAction>
              </ListItem>
            ))}
          </List>
        </Paper>
      )}

      {history.length > 0 && (
        <Paper sx={{ p: 2 }}>
          <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <History />
            Recent Searches
          </Typography>
          <List dense>
            {history.map((search, index) => (
              <ListItem
                key={index}
                button
                onClick={() => onHistoryClick(search)}
                sx={{ px: 0 }}
              >
                <ListItemText
                  primary={search}
                  primaryTypographyProps={{ variant: 'body2' }}
                />
                <ListItemSecondaryAction>
                  <IconButton
                    edge="end"
                    size="small"
                    onClick={(e) => {
                      e.stopPropagation();
                      onHistoryClick(search);
                    }}
                  >
                    <Search fontSize="small" />
                  </IconButton>
                </ListItemSecondaryAction>
              </ListItem>
            ))}
          </List>
        </Paper>
      )}

      {history.length === 0 && savedSearches.length === 0 && (
        <Paper sx={{ p: 2, textAlign: 'center' }}>
          <Typography variant="body2" color="text.secondary">
            Your search history will appear here
          </Typography>
        </Paper>
      )}
    </Box>
  );
};

export default SearchHistory;
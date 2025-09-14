import React from 'react';
import {
  Box,
  Paper,
  Typography,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  TextField,
  Button,
} from '@mui/material';
import { FilterList, Clear } from '@mui/icons-material';

interface SearchFiltersProps {
  filters: {
    language: string;
    source: string;
    dateRange: string;
    documentType?: string;
  };
  onFiltersChange: (filters: any) => void;
  onClearFilters: () => void;
}

const SearchFilters: React.FC<SearchFiltersProps> = ({
  filters,
  onFiltersChange,
  onClearFilters,
}) => {
  const handleFilterChange = (key: string, value: string) => {
    onFiltersChange({
      ...filters,
      [key]: value,
    });
  };

  const getActiveFiltersCount = () => {
    return Object.values(filters).filter(value => value !== 'all' && value !== '').length;
  };

  return (
    <Paper sx={{ p: 2 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <FilterList />
          Filters
          {getActiveFiltersCount() > 0 && (
            <Chip label={getActiveFiltersCount()} size="small" color="primary" />
          )}
        </Typography>
        {getActiveFiltersCount() > 0 && (
          <Button
            startIcon={<Clear />}
            size="small"
            onClick={onClearFilters}
          >
            Clear
          </Button>
        )}
      </Box>

      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
        <FormControl fullWidth size="small">
          <InputLabel>Language</InputLabel>
          <Select
            value={filters.language}
            onChange={(e) => handleFilterChange('language', e.target.value)}
            label="Language"
          >
            <MenuItem value="all">All Languages</MenuItem>
            <MenuItem value="en">English</MenuItem>
            <MenuItem value="vi">Vietnamese</MenuItem>
            <MenuItem value="ja">Japanese</MenuItem>
            <MenuItem value="zh">Chinese</MenuItem>
            <MenuItem value="ko">Korean</MenuItem>
          </Select>
        </FormControl>

        <FormControl fullWidth size="small">
          <InputLabel>Source</InputLabel>
          <Select
            value={filters.source}
            onChange={(e) => handleFilterChange('source', e.target.value)}
            label="Source"
          >
            <MenuItem value="all">All Sources</MenuItem>
            <MenuItem value="upload">Uploaded Documents</MenuItem>
            <MenuItem value="confluence">Confluence</MenuItem>
            <MenuItem value="repository">Git Repository</MenuItem>
            <MenuItem value="email">Email</MenuItem>
          </Select>
        </FormControl>

        <FormControl fullWidth size="small">
          <InputLabel>Date Range</InputLabel>
          <Select
            value={filters.dateRange}
            onChange={(e) => handleFilterChange('dateRange', e.target.value)}
            label="Date Range"
          >
            <MenuItem value="all">All Time</MenuItem>
            <MenuItem value="today">Today</MenuItem>
            <MenuItem value="week">This Week</MenuItem>
            <MenuItem value="month">This Month</MenuItem>
            <MenuItem value="quarter">This Quarter</MenuItem>
            <MenuItem value="year">This Year</MenuItem>
          </Select>
        </FormControl>

        <FormControl fullWidth size="small">
          <InputLabel>Document Type</InputLabel>
          <Select
            value={filters.documentType || 'all'}
            onChange={(e) => handleFilterChange('documentType', e.target.value)}
            label="Document Type"
          >
            <MenuItem value="all">All Types</MenuItem>
            <MenuItem value="pdf">PDF</MenuItem>
            <MenuItem value="docx">Word Document</MenuItem>
            <MenuItem value="txt">Text File</MenuItem>
            <MenuItem value="md">Markdown</MenuItem>
          </Select>
        </FormControl>
      </Box>
    </Paper>
  );
};

export default SearchFilters;
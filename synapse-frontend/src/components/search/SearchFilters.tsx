import React, { useState } from 'react';
import {
  Paper,
  Typography,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Checkbox,
  ListItemText,
  TextField,
  Box,
  Button,
  Collapse,
} from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { SearchFilters as SearchFiltersType } from '../../types/search';

interface SearchFiltersProps {
  filters: SearchFiltersType;
  onFiltersChange: (filters: SearchFiltersType) => void;
  open: boolean;
}

const SearchFilters: React.FC<SearchFiltersProps> = ({ filters, onFiltersChange, open }) => {
  const [localFilters, setLocalFilters] = useState<SearchFiltersType>(filters);

  const documentTypes = ['PDF', 'DOCX', 'TXT', 'HTML'];
  const sources = ['Upload', 'Confluence', 'GitHub', 'GitLab'];
  const languages = ['English', 'Vietnamese', 'Japanese', 'Chinese', 'Korean'];

  const handleFilterChange = (key: keyof SearchFiltersType, value: any) => {
    const newFilters = { ...localFilters, [key]: value };
    setLocalFilters(newFilters);
  };

  const handleApplyFilters = () => {
    onFiltersChange(localFilters);
  };

  const handleClearFilters = () => {
    const emptyFilters: SearchFiltersType = {};
    setLocalFilters(emptyFilters);
    onFiltersChange(emptyFilters);
  };

  return (
    <Collapse in={open}>
      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom>
          Search Filters
        </Typography>

        <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 2, mb: 3 }}>
          <FormControl>
            <InputLabel>Document Types</InputLabel>
            <Select
              multiple
              value={localFilters.documentTypes || []}
              onChange={(e) => handleFilterChange('documentTypes', e.target.value)}
              renderValue={(selected) => (selected as string[]).join(', ')}
            >
              {documentTypes.map((type) => (
                <MenuItem key={type} value={type}>
                  <Checkbox checked={(localFilters.documentTypes || []).includes(type)} />
                  <ListItemText primary={type} />
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          <FormControl>
            <InputLabel>Sources</InputLabel>
            <Select
              multiple
              value={localFilters.sources || []}
              onChange={(e) => handleFilterChange('sources', e.target.value)}
              renderValue={(selected) => (selected as string[]).join(', ')}
            >
              {sources.map((source) => (
                <MenuItem key={source} value={source}>
                  <Checkbox checked={(localFilters.sources || []).includes(source)} />
                  <ListItemText primary={source} />
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          <FormControl>
            <InputLabel>Languages</InputLabel>
            <Select
              multiple
              value={localFilters.languages || []}
              onChange={(e) => handleFilterChange('languages', e.target.value)}
              renderValue={(selected) => (selected as string[]).join(', ')}
            >
              {languages.map((language) => (
                <MenuItem key={language} value={language}>
                  <Checkbox checked={(localFilters.languages || []).includes(language)} />
                  <ListItemText primary={language} />
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>

        <LocalizationProvider dateAdapter={AdapterDateFns}>
          <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
            <DatePicker
              label="Start Date"
              value={localFilters.dateRange?.start ? new Date(localFilters.dateRange.start) : null}
              onChange={(date) => {
                const dateRange = localFilters.dateRange || { start: '', end: '' };
                handleFilterChange('dateRange', {
                  ...dateRange,
                  start: date ? date.toISOString() : ''
                });
              }}
              renderInput={(params) => <TextField {...params} />}
            />
            <DatePicker
              label="End Date"
              value={localFilters.dateRange?.end ? new Date(localFilters.dateRange.end) : null}
              onChange={(date) => {
                const dateRange = localFilters.dateRange || { start: '', end: '' };
                handleFilterChange('dateRange', {
                  ...dateRange,
                  end: date ? date.toISOString() : ''
                });
              }}
              renderInput={(params) => <TextField {...params} />}
            />
          </Box>
        </LocalizationProvider>

        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button variant="contained" onClick={handleApplyFilters}>
            Apply Filters
          </Button>
          <Button variant="outlined" onClick={handleClearFilters}>
            Clear All
          </Button>
        </Box>
      </Paper>
    </Collapse>
  );
};

export default SearchFilters;
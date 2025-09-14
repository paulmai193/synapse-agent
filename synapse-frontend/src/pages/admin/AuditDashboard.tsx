import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Grid,
  Card,
  CardContent,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  TextField,
  InputAdornment,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  IconButton,
  Button,
  Alert
} from '@mui/material';
import {
  Search as SearchIcon,
  Download as DownloadIcon,
  Warning as WarningIcon,
  Visibility as VisibilityIcon
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { useAppDispatch, useAppSelector } from '../../hooks/redux';
import { fetchAuditLogs, exportAuditLogs, fetchAuditAnalytics } from '../../store/slices/auditSlice';
import { AuditLog, AuditSearchCriteria } from '../../types/audit';
import { AuditLogDetails } from '../../components/admin/AuditLogDetails';
import { AnalyticsCharts } from '../../components/admin/AnalyticsCharts';

export const AuditDashboard: React.FC = () => {
  const dispatch = useAppDispatch();
  const { auditLogs, analytics, isLoading, error, totalCount } = useAppSelector(state => state.audit);
  
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);
  const [searchCriteria, setSearchCriteria] = useState<AuditSearchCriteria>({
    query: '',
    actionType: '',
    userId: '',
    startDate: null,
    endDate: null
  });
  const [selectedLog, setSelectedLog] = useState<AuditLog | null>(null);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);

  useEffect(() => {
    dispatch(fetchAuditLogs({ page, size: rowsPerPage, ...searchCriteria }));
    dispatch(fetchAuditAnalytics());
  }, [dispatch, page, rowsPerPage, searchCriteria]);

  const handleSearch = () => {
    setPage(0);
    dispatch(fetchAuditLogs({ page: 0, size: rowsPerPage, ...searchCriteria }));
  };

  const handleExport = async () => {
    try {
      const blob = await dispatch(exportAuditLogs({ 
        format: 'csv',
        criteria: searchCriteria 
      })).unwrap();
      
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `audit_logs_${new Date().toISOString().split('T')[0]}.csv`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error) {
      console.error('Failed to export audit logs:', error);
    }
  };

  const handleViewDetails = (log: AuditLog) => {
    setSelectedLog(log);
    setIsDetailsOpen(true);
  };

  const getActionTypeColor = (actionType: string) => {
    switch (actionType.toLowerCase()) {
      case 'login': return 'success';
      case 'logout': return 'info';
      case 'create': return 'primary';
      case 'update': return 'warning';
      case 'delete': return 'error';
      case 'search': return 'default';
      case 'qa': return 'secondary';
      default: return 'default';
    }
  };

  const getSeverityIcon = (severity: string) => {
    if (severity === 'HIGH' || severity === 'CRITICAL') {
      return <WarningIcon color="error" fontSize="small" />;
    }
    return null;
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Audit Logging & Analytics Dashboard
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {/* Analytics Cards */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Total Activities
              </Typography>
              <Typography variant="h4">
                {analytics?.totalActivities || 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Active Users (24h)
              </Typography>
              <Typography variant="h4">
                {analytics?.activeUsers24h || 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Failed Logins (24h)
              </Typography>
              <Typography variant="h4" color="error">
                {analytics?.failedLogins24h || 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Suspicious Activities
              </Typography>
              <Typography variant="h4" color="warning.main">
                {analytics?.suspiciousActivities || 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Analytics Charts */}
      <AnalyticsCharts analytics={analytics} />

      {/* Search Filters */}
      <Paper sx={{ p: 2, mb: 2 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={6} md={3}>
            <TextField
              placeholder="Search activities..."
              value={searchCriteria.query}
              onChange={(e) => setSearchCriteria({ ...searchCriteria, query: e.target.value })}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                )
              }}
              fullWidth
              size="small"
            />
          </Grid>
          
          <Grid item xs={12} sm={6} md={2}>
            <FormControl fullWidth size="small">
              <InputLabel>Action Type</InputLabel>
              <Select
                value={searchCriteria.actionType}
                onChange={(e) => setSearchCriteria({ ...searchCriteria, actionType: e.target.value })}
                label="Action Type"
              >
                <MenuItem value="">All</MenuItem>
                <MenuItem value="LOGIN">Login</MenuItem>
                <MenuItem value="LOGOUT">Logout</MenuItem>
                <MenuItem value="CREATE">Create</MenuItem>
                <MenuItem value="UPDATE">Update</MenuItem>
                <MenuItem value="DELETE">Delete</MenuItem>
                <MenuItem value="SEARCH">Search</MenuItem>
                <MenuItem value="QA">Q&A</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          
          <Grid item xs={12} sm={6} md={2}>
            <DatePicker
              label="Start Date"
              value={searchCriteria.startDate}
              onChange={(date) => setSearchCriteria({ ...searchCriteria, startDate: date })}
              slotProps={{ textField: { size: 'small', fullWidth: true } }}
            />
          </Grid>
          
          <Grid item xs={12} sm={6} md={2}>
            <DatePicker
              label="End Date"
              value={searchCriteria.endDate}
              onChange={(date) => setSearchCriteria({ ...searchCriteria, endDate: date })}
              slotProps={{ textField: { size: 'small', fullWidth: true } }}
            />
          </Grid>
          
          <Grid item xs={12} sm={6} md={2}>
            <Button
              variant="contained"
              onClick={handleSearch}
              fullWidth
              disabled={isLoading}
            >
              Search
            </Button>
          </Grid>
          
          <Grid item xs={12} sm={6} md={1}>
            <Button
              variant="outlined"
              onClick={handleExport}
              startIcon={<DownloadIcon />}
              disabled={isLoading}
            >
              Export
            </Button>
          </Grid>
        </Grid>
      </Paper>

      {/* Audit Logs Table */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Timestamp</TableCell>
              <TableCell>User</TableCell>
              <TableCell>Action</TableCell>
              <TableCell>Resource</TableCell>
              <TableCell>IP Address</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {auditLogs.map((log) => (
              <TableRow key={log.id}>
                <TableCell>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    {getSeverityIcon(log.severity)}
                    <Typography variant="body2">
                      {new Date(log.timestamp).toLocaleString()}
                    </Typography>
                  </Box>
                </TableCell>
                <TableCell>
                  <Typography variant="body2">
                    {log.username || 'System'}
                  </Typography>
                </TableCell>
                <TableCell>
                  <Chip
                    label={log.actionType}
                    size="small"
                    color={getActionTypeColor(log.actionType) as any}
                  />
                </TableCell>
                <TableCell>
                  <Typography variant="body2">
                    {log.resourceType} {log.resourceId && `(${log.resourceId.substring(0, 8)}...)`}
                  </Typography>
                </TableCell>
                <TableCell>
                  <Typography variant="body2" fontFamily="monospace">
                    {log.ipAddress}
                  </Typography>
                </TableCell>
                <TableCell>
                  <Chip
                    label={log.success ? 'Success' : 'Failed'}
                    size="small"
                    color={log.success ? 'success' : 'error'}
                  />
                </TableCell>
                <TableCell>
                  <IconButton
                    size="small"
                    onClick={() => handleViewDetails(log)}
                    color="primary"
                  >
                    <VisibilityIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        
        <TablePagination
          component="div"
          count={totalCount}
          page={page}
          onPageChange={(_, newPage) => setPage(newPage)}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={(e) => {
            setRowsPerPage(parseInt(e.target.value, 10));
            setPage(0);
          }}
          rowsPerPageOptions={[10, 25, 50, 100]}
        />
      </TableContainer>

      <AuditLogDetails
        open={isDetailsOpen}
        onClose={() => setIsDetailsOpen(false)}
        auditLog={selectedLog}
      />
    </Box>
  );
};
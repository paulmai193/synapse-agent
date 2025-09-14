import React, { useState, useEffect } from 'react';
import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Chip,
  IconButton,
  Menu,
  MenuItem,
  TextField,
  Box,
  Typography,
} from '@mui/material';
import { MoreVert, FilterList } from '@mui/icons-material';
import { documentApi } from '../../utils/documentApi';
import { Document } from '../../types/document';
import { useAuth } from '../../hooks/useAuth';

const DocumentList: React.FC = () => {
  const [documents, setDocuments] = useState<Document[]>([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalCount, setTotalCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [selectedDoc, setSelectedDoc] = useState<Document | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const { isAdmin } = useAuth();

  const loadDocuments = async () => {
    setLoading(true);
    try {
      const response = await documentApi.getDocuments(page, rowsPerPage);
      setDocuments(response.content || []);
      setTotalCount(response.totalElements || 0);
    } catch (error) {
      console.error('Failed to load documents:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDocuments();
  }, [page, rowsPerPage]);

  const handleMenuClick = (event: React.MouseEvent<HTMLElement>, doc: Document) => {
    setAnchorEl(event.currentTarget);
    setSelectedDoc(doc);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setSelectedDoc(null);
  };

  const handleStatusChange = async (status: string) => {
    if (selectedDoc) {
      try {
        await documentApi.updateDocumentStatus(selectedDoc.id, status);
        loadDocuments();
      } catch (error) {
        console.error('Failed to update document status:', error);
      }
    }
    handleMenuClose();
  };

  const handleDelete = async () => {
    if (selectedDoc) {
      try {
        await documentApi.deleteDocument(selectedDoc.id);
        loadDocuments();
      } catch (error) {
        console.error('Failed to delete document:', error);
      }
    }
    handleMenuClose();
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED': return 'success';
      case 'PROCESSING': return 'warning';
      case 'FAILED': return 'error';
      default: return 'default';
    }
  };

  const filteredDocuments = documents.filter(doc =>
    doc.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
    doc.metadata.tags.some(tag => tag.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  return (
    <Paper elevation={3}>
      <Box sx={{ p: 2 }}>
        <Typography variant="h6" gutterBottom>
          Documents
        </Typography>
        
        <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
          <TextField
            label="Search documents..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            size="small"
            sx={{ flexGrow: 1 }}
          />
          <IconButton>
            <FilterList />
          </IconButton>
        </Box>
      </Box>

      <TableContainer>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Title</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Processing</TableCell>
              <TableCell>Visibility</TableCell>
              <TableCell>Created</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredDocuments.map((doc) => (
              <TableRow key={doc.id}>
                <TableCell>{doc.title}</TableCell>
                <TableCell>
                  <Chip 
                    label={doc.status} 
                    color={doc.status === 'ACTIVE' ? 'success' : 'default'}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  <Chip 
                    label={doc.processingStatus} 
                    color={getStatusColor(doc.processingStatus)}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  <Chip 
                    label={doc.accessControl.visibility} 
                    variant="outlined"
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  {new Date(doc.createdAt).toLocaleDateString()}
                </TableCell>
                <TableCell>
                  <IconButton onClick={(e) => handleMenuClick(e, doc)}>
                    <MoreVert />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <TablePagination
        component="div"
        count={totalCount}
        page={page}
        onPageChange={(_, newPage) => setPage(newPage)}
        rowsPerPage={rowsPerPage}
        onRowsPerPageChange={(e) => setRowsPerPage(parseInt(e.target.value, 10))}
      />

      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
      >
        <MenuItem onClick={() => console.log('View document')}>
          View
        </MenuItem>
        {isAdmin() && (
          <>
            <MenuItem onClick={() => handleStatusChange('ACTIVE')}>
              Activate
            </MenuItem>
            <MenuItem onClick={() => handleStatusChange('INACTIVE')}>
              Deactivate
            </MenuItem>
            <MenuItem onClick={handleDelete}>
              Delete
            </MenuItem>
          </>
        )}
      </Menu>
    </Paper>
  );
};

export default DocumentList;
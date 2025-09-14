import React, { useState, useEffect } from 'react';
import {
  Drawer,
  Box,
  Typography,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  TextField,
  InputAdornment,
  Chip,
  Divider,
  Button,
  Menu,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions
} from '@mui/material';
import {
  Search as SearchIcon,
  MoreVert as MoreVertIcon,
  Delete as DeleteIcon,
  Bookmark as BookmarkIcon,
  Share as ShareIcon,
  Add as AddIcon,
  Close as CloseIcon
} from '@mui/icons-material';
import { useAppDispatch, useAppSelector } from '../../hooks/redux';
import {
  loadConversationHistory,
  createNewConversation,
  deleteConversation,
  loadConversation
} from '../../store/slices/qaSlice';
import { Conversation } from '../../types/qa';

interface ConversationHistoryProps {
  open: boolean;
  onClose: () => void;
}

export const ConversationHistory: React.FC<ConversationHistoryProps> = ({
  open,
  onClose
}) => {
  const dispatch = useAppDispatch();
  const { conversations, currentConversation, isLoading } = useAppSelector(state => state.qa);
  
  const [searchTerm, setSearchTerm] = useState('');
  const [filterType, setFilterType] = useState<'all' | 'bookmarked'>('all');
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [selectedConversation, setSelectedConversation] = useState<Conversation | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

  useEffect(() => {
    if (open) {
      dispatch(loadConversationHistory());
    }
  }, [open, dispatch]);

  const filteredConversations = conversations.filter(conversation => {
    const matchesSearch = conversation.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         conversation.messages.some(msg => 
                           msg.content.toLowerCase().includes(searchTerm.toLowerCase())
                         );
    const matchesFilter = filterType === 'all' || 
                         (filterType === 'bookmarked' && conversation.isBookmarked);
    
    return matchesSearch && matchesFilter;
  });

  const handleConversationClick = (conversation: Conversation) => {
    dispatch(loadConversation(conversation.id));
    onClose();
  };

  const handleNewConversation = () => {
    dispatch(createNewConversation());
    onClose();
  };

  const handleMenuClick = (event: React.MouseEvent<HTMLElement>, conversation: Conversation) => {
    event.stopPropagation();
    setAnchorEl(event.currentTarget);
    setSelectedConversation(conversation);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setSelectedConversation(null);
  };

  const handleDeleteClick = () => {
    setDeleteDialogOpen(true);
    handleMenuClose();
  };

  const handleDeleteConfirm = () => {
    if (selectedConversation) {
      dispatch(deleteConversation(selectedConversation.id));
    }
    setDeleteDialogOpen(false);
    setSelectedConversation(null);
  };

  const formatDate = (date: string) => {
    const now = new Date();
    const messageDate = new Date(date);
    const diffInHours = (now.getTime() - messageDate.getTime()) / (1000 * 60 * 60);
    
    if (diffInHours < 24) {
      return messageDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } else if (diffInHours < 168) { // 7 days
      return messageDate.toLocaleDateString([], { weekday: 'short' });
    } else {
      return messageDate.toLocaleDateString([], { month: 'short', day: 'numeric' });
    }
  };

  const getConversationPreview = (conversation: Conversation) => {
    const lastMessage = conversation.messages[conversation.messages.length - 1];
    if (!lastMessage) return 'No messages';
    
    return lastMessage.role === 'user' 
      ? lastMessage.content 
      : `AI: ${lastMessage.content.substring(0, 50)}...`;
  };

  return (
    <>
      <Drawer
        anchor="right"
        open={open}
        onClose={onClose}
        sx={{
          '& .MuiDrawer-paper': {
            width: 400,
            maxWidth: '90vw'
          }
        }}
      >
        <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
            <Typography variant="h6">Conversations</Typography>
            <IconButton onClick={onClose} size="small">
              <CloseIcon />
            </IconButton>
          </Box>
          
          <TextField
            fullWidth
            size="small"
            placeholder="Search conversations..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              )
            }}
            sx={{ mb: 2 }}
          />
          
          <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
            <Chip
              label="All"
              onClick={() => setFilterType('all')}
              color={filterType === 'all' ? 'primary' : 'default'}
              size="small"
            />
            <Chip
              label="Bookmarked"
              onClick={() => setFilterType('bookmarked')}
              color={filterType === 'bookmarked' ? 'primary' : 'default'}
              size="small"
            />
          </Box>
          
          <Button
            fullWidth
            variant="outlined"
            startIcon={<AddIcon />}
            onClick={handleNewConversation}
            disabled={isLoading}
          >
            New Conversation
          </Button>
        </Box>

        <Box sx={{ flex: 1, overflow: 'auto' }}>
          <List>
            {filteredConversations.map((conversation) => (
              <ListItem
                key={conversation.id}
                button
                onClick={() => handleConversationClick(conversation)}
                selected={currentConversation?.id === conversation.id}
                sx={{
                  borderLeft: currentConversation?.id === conversation.id ? 3 : 0,
                  borderColor: 'primary.main'
                }}
              >
                <ListItemText
                  primary={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Typography variant="subtitle2" noWrap>
                        {conversation.title}
                      </Typography>
                      {conversation.isBookmarked && (
                        <BookmarkIcon fontSize="small" color="primary" />
                      )}
                    </Box>
                  }
                  secondary={
                    <Box>
                      <Typography variant="caption" color="text.secondary" noWrap>
                        {getConversationPreview(conversation)}
                      </Typography>
                      <Typography variant="caption" color="text.secondary" display="block">
                        {formatDate(conversation.updatedAt)} • {conversation.messages.length} messages
                      </Typography>
                    </Box>
                  }
                />
                <ListItemSecondaryAction>
                  <IconButton
                    edge="end"
                    size="small"
                    onClick={(e) => handleMenuClick(e, conversation)}
                  >
                    <MoreVertIcon />
                  </IconButton>
                </ListItemSecondaryAction>
              </ListItem>
            ))}
            
            {filteredConversations.length === 0 && (
              <ListItem>
                <ListItemText
                  primary={
                    <Typography variant="body2" color="text.secondary" align="center">
                      {searchTerm ? 'No conversations found' : 'No conversations yet'}
                    </Typography>
                  }
                />
              </ListItem>
            )}
          </List>
        </Box>
      </Drawer>

      {/* Context Menu */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
      >
        <MenuItem onClick={handleMenuClose}>
          <ShareIcon sx={{ mr: 1 }} />
          Share
        </MenuItem>
        <MenuItem onClick={handleDeleteClick}>
          <DeleteIcon sx={{ mr: 1 }} />
          Delete
        </MenuItem>
      </Menu>

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>Delete Conversation</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete this conversation? This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleDeleteConfirm} color="error">
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};
import React, { useState, useEffect } from 'react';
import {
  Paper,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Typography,
  Box,
  Chip,
  Menu,
  MenuItem,
} from '@mui/material';
import { 
  Bookmark, 
  BookmarkBorder, 
  MoreVert, 
  Delete,
  Add 
} from '@mui/icons-material';
import { Conversation } from '../../types/qa';
import { qaApi } from '../../utils/qaApi';

interface ConversationListProps {
  selectedConversationId?: string;
  onSelectConversation: (conversation: Conversation | null) => void;
  onNewConversation: () => void;
}

const ConversationList: React.FC<ConversationListProps> = ({
  selectedConversationId,
  onSelectConversation,
  onNewConversation,
}) => {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [selectedConversation, setSelectedConversation] = useState<Conversation | null>(null);

  useEffect(() => {
    loadConversations();
  }, []);

  const loadConversations = async () => {
    try {
      const data = await qaApi.getConversations();
      setConversations(data);
    } catch (error) {
      console.error('Failed to load conversations:', error);
    }
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

  const handleBookmark = async () => {
    if (selectedConversation) {
      try {
        await qaApi.bookmarkConversation(
          selectedConversation.id, 
          !selectedConversation.bookmarked
        );
        loadConversations();
      } catch (error) {
        console.error('Failed to bookmark conversation:', error);
      }
    }
    handleMenuClose();
  };

  const handleDelete = async () => {
    if (selectedConversation) {
      try {
        await qaApi.deleteConversation(selectedConversation.id);
        if (selectedConversationId === selectedConversation.id) {
          onSelectConversation(null);
        }
        loadConversations();
      } catch (error) {
        console.error('Failed to delete conversation:', error);
      }
    }
    handleMenuClose();
  };

  const getConversationPreview = (conversation: Conversation) => {
    const lastMessage = conversation.messages[conversation.messages.length - 1];
    return lastMessage ? lastMessage.content.substring(0, 50) + '...' : 'No messages';
  };

  return (
    <Paper sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h6">Conversations</Typography>
          <IconButton onClick={onNewConversation} color="primary">
            <Add />
          </IconButton>
        </Box>
      </Box>

      <List sx={{ flexGrow: 1, overflow: 'auto' }}>
        {conversations.map((conversation) => (
          <ListItem
            key={conversation.id}
            button
            selected={selectedConversationId === conversation.id}
            onClick={() => onSelectConversation(conversation)}
          >
            <ListItemText
              primary={
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Typography variant="subtitle2" noWrap>
                    {conversation.title}
                  </Typography>
                  {conversation.bookmarked && (
                    <Bookmark fontSize="small" color="primary" />
                  )}
                </Box>
              }
              secondary={
                <Box>
                  <Typography variant="body2" color="text.secondary" noWrap>
                    {getConversationPreview(conversation)}
                  </Typography>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 0.5 }}>
                    <Chip 
                      label={`${conversation.messages.length} messages`}
                      size="small"
                      variant="outlined"
                    />
                    <Typography variant="caption" color="text.secondary">
                      {new Date(conversation.updatedAt).toLocaleDateString()}
                    </Typography>
                  </Box>
                </Box>
              }
            />
            <ListItemSecondaryAction>
              <IconButton 
                edge="end" 
                onClick={(e) => handleMenuClick(e, conversation)}
              >
                <MoreVert />
              </IconButton>
            </ListItemSecondaryAction>
          </ListItem>
        ))}
      </List>

      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
      >
        <MenuItem onClick={handleBookmark}>
          {selectedConversation?.bookmarked ? (
            <>
              <BookmarkBorder sx={{ mr: 1 }} />
              Remove Bookmark
            </>
          ) : (
            <>
              <Bookmark sx={{ mr: 1 }} />
              Bookmark
            </>
          )}
        </MenuItem>
        <MenuItem onClick={handleDelete}>
          <Delete sx={{ mr: 1 }} />
          Delete
        </MenuItem>
      </Menu>
    </Paper>
  );
};

export default ConversationList;
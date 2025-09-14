import React, { useState, useRef, useEffect } from 'react';
import {
  Box,
  Paper,
  TextField,
  IconButton,
  Typography,
  List,
  ListItem,
  Chip,
  CircularProgress,
  Collapse,
  Tooltip,
  Alert
} from '@mui/material';
import {
  Send as SendIcon,
  ThumbUp,
  ThumbDown,
  Bookmark,
  BookmarkBorder,
  ExpandMore,
  ContentCopy
} from '@mui/icons-material';
import { useAppDispatch, useAppSelector } from '../../hooks/redux';
import { askQuestion, provideFeedback, bookmarkConversation } from '../../store/slices/qaSlice';
import { QARequest, ConversationMessage } from '../../types/qa';
import { ConversationHistory } from './ConversationHistory';

interface QAInterfaceProps {
  onDocumentPreview?: (documentId: string) => void;
}

export const QAInterface: React.FC<QAInterfaceProps> = ({ onDocumentPreview }) => {
  const dispatch = useAppDispatch();
  const { currentConversation, isLoading, error } = useAppSelector(state => state.qa);
  
  const [question, setQuestion] = useState('');
  const [expandedSources, setExpandedSources] = useState<Set<string>>(new Set());
  const [showHistory, setShowHistory] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [currentConversation?.messages]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!question.trim() || isLoading) return;

    const request: QARequest = {
      question: question.trim(),
      language: 'auto',
      conversationId: currentConversation?.id
    };

    try {
      await dispatch(askQuestion(request)).unwrap();
      setQuestion('');
      inputRef.current?.focus();
    } catch (error) {
      console.error('Failed to ask question:', error);
    }
  };

  const handleFeedback = async (messageId: string, isHelpful: boolean) => {
    try {
      await dispatch(provideFeedback({
        messageId,
        isHelpful,
        comment: ''
      })).unwrap();
    } catch (error) {
      console.error('Failed to provide feedback:', error);
    }
  };

  const handleBookmark = async (messageId: string) => {
    try {
      await dispatch(bookmarkConversation(messageId)).unwrap();
    } catch (error) {
      console.error('Failed to bookmark conversation:', error);
    }
  };

  const toggleSourceExpansion = (sourceId: string) => {
    const newExpanded = new Set(expandedSources);
    if (newExpanded.has(sourceId)) {
      newExpanded.delete(sourceId);
    } else {
      newExpanded.add(sourceId);
    }
    setExpandedSources(newExpanded);
  };

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
  };

  const renderMessage = (message: ConversationMessage) => {
    const isUser = message.role === 'user';
    
    return (
      <ListItem
        key={message.id}
        sx={{
          flexDirection: 'column',
          alignItems: isUser ? 'flex-end' : 'flex-start',
          mb: 2
        }}
      >
        <Paper
          elevation={1}
          sx={{
            p: 2,
            maxWidth: '80%',
            bgcolor: isUser ? 'primary.light' : 'background.paper',
            color: isUser ? 'primary.contrastText' : 'text.primary'
          }}
        >
          <Typography variant="body1" sx={{ mb: 1 }}>
            {message.content}
          </Typography>
          
          {!isUser && message.sources && message.sources.length > 0 && (
            <Box sx={{ mt: 2 }}>
              <Typography variant="subtitle2" sx={{ mb: 1 }}>
                Sources ({message.sources.length}):
              </Typography>
              {message.sources.map((source) => (
                <Box key={source.id} sx={{ mb: 1 }}>
                  <Chip
                    label={`${source.title} (${Math.round(source.relevanceScore * 100)}%)`}
                    size="small"
                    onClick={() => toggleSourceExpansion(source.id)}
                    sx={{ mr: 1, mb: 1, cursor: 'pointer' }}
                  />
                  <Collapse in={expandedSources.has(source.id)}>
                    <Paper variant="outlined" sx={{ p: 1, mt: 1 }}>
                      <Typography variant="caption" color="text.secondary">
                        {source.snippet}
                      </Typography>
                    </Paper>
                  </Collapse>
                </Box>
              ))}
            </Box>
          )}
          
          {!isUser && (
            <Box sx={{ display: 'flex', alignItems: 'center', mt: 2, gap: 1 }}>
              <Tooltip title="Helpful">
                <IconButton
                  size="small"
                  onClick={() => handleFeedback(message.id, true)}
                  color={message.feedback?.isHelpful === true ? 'primary' : 'default'}
                >
                  <ThumbUp fontSize="small" />
                </IconButton>
              </Tooltip>
              
              <Tooltip title="Not helpful">
                <IconButton
                  size="small"
                  onClick={() => handleFeedback(message.id, false)}
                  color={message.feedback?.isHelpful === false ? 'error' : 'default'}
                >
                  <ThumbDown fontSize="small" />
                </IconButton>
              </Tooltip>
              
              <Tooltip title="Bookmark">
                <IconButton
                  size="small"
                  onClick={() => handleBookmark(message.id)}
                >
                  {message.isBookmarked ? <Bookmark /> : <BookmarkBorder />}
                </IconButton>
              </Tooltip>
              
              <Tooltip title="Copy">
                <IconButton
                  size="small"
                  onClick={() => copyToClipboard(message.content)}
                >
                  <ContentCopy fontSize="small" />
                </IconButton>
              </Tooltip>
              
              {message.confidenceScore && (
                <Chip
                  label={`${Math.round(message.confidenceScore * 100)}% confident`}
                  size="small"
                  color={message.confidenceScore > 0.8 ? 'success' : 
                         message.confidenceScore > 0.6 ? 'warning' : 'error'}
                />
              )}
            </Box>
          )}
        </Paper>
        
        <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5 }}>
          {new Date(message.timestamp).toLocaleTimeString()}
        </Typography>
      </ListItem>
    );
  };

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
        <Typography variant="h6">Q&A Assistant</Typography>
        <Typography variant="body2" color="text.secondary">
          Ask questions about your documents in any language
        </Typography>
      </Box>

      {error && (
        <Alert severity="error" sx={{ m: 2 }}>
          {error}
        </Alert>
      )}

      <Box sx={{ flex: 1, overflow: 'auto', p: 1 }}>
        {currentConversation?.messages.length === 0 ? (
          <Box sx={{ 
            display: 'flex', 
            flexDirection: 'column', 
            alignItems: 'center', 
            justifyContent: 'center',
            height: '100%',
            textAlign: 'center'
          }}>
            <Typography variant="h6" color="text.secondary" sx={{ mb: 2 }}>
              Start a conversation
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Ask questions about your documents and get intelligent answers with source citations
            </Typography>
          </Box>
        ) : (
          <List>
            {currentConversation?.messages.map(renderMessage)}
            {isLoading && (
              <ListItem sx={{ justifyContent: 'center' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <CircularProgress size={20} />
                  <Typography variant="body2" color="text.secondary">
                    Thinking...
                  </Typography>
                </Box>
              </ListItem>
            )}
          </List>
        )}
        <div ref={messagesEndRef} />
      </Box>

      <Box sx={{ p: 2, borderTop: 1, borderColor: 'divider' }}>
        <form onSubmit={handleSubmit}>
          <Box sx={{ display: 'flex', gap: 1 }}>
            <TextField
              ref={inputRef}
              fullWidth
              multiline
              maxRows={4}
              placeholder="Ask a question about your documents..."
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              disabled={isLoading}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault();
                  handleSubmit(e);
                }
              }}
            />
            <IconButton
              type="submit"
              disabled={!question.trim() || isLoading}
              color="primary"
            >
              <SendIcon />
            </IconButton>
          </Box>
        </form>
      </Box>

      <ConversationHistory
        open={showHistory}
        onClose={() => setShowHistory(false)}
      />
    </Box>
  );
};
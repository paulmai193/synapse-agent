import React, { useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Chip,
  IconButton,
  Collapse,
  Card,
  CardContent,
  Divider,
} from '@mui/material';
import { 
  Person, 
  SmartToy, 
  ThumbUp, 
  ThumbDown, 
  ExpandMore, 
  ExpandLess,
  OpenInNew 
} from '@mui/icons-material';
import { Message } from '../../types/qa';
import { qaApi } from '../../utils/qaApi';

interface ChatMessageProps {
  message: Message;
}

const ChatMessage: React.FC<ChatMessageProps> = ({ message }) => {
  const [showSources, setShowSources] = useState(false);
  const [feedback, setFeedback] = useState<'helpful' | 'not_helpful' | null>(
    message.feedback || null
  );

  const handleFeedback = async (newFeedback: 'helpful' | 'not_helpful') => {
    try {
      await qaApi.provideFeedback(message.id, newFeedback);
      setFeedback(newFeedback);
    } catch (error) {
      console.error('Failed to provide feedback:', error);
    }
  };

  const isQuestion = message.type === 'question';

  return (
    <Box sx={{ mb: 2 }}>
      <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-start' }}>
        <Box sx={{ 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'center',
          width: 40,
          height: 40,
          borderRadius: '50%',
          bgcolor: isQuestion ? 'primary.main' : 'secondary.main',
          color: 'white'
        }}>
          {isQuestion ? <Person /> : <SmartToy />}
        </Box>

        <Paper 
          sx={{ 
            flexGrow: 1, 
            p: 2,
            bgcolor: isQuestion ? 'grey.100' : 'background.paper',
            maxWidth: '80%'
          }}
        >
          <Typography variant="body1" sx={{ mb: 1 }}>
            {message.content}
          </Typography>

          {!isQuestion && message.confidence && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
              <Chip 
                label={`Confidence: ${(message.confidence * 100).toFixed(0)}%`}
                size="small"
                color={message.confidence > 0.8 ? 'success' : message.confidence > 0.6 ? 'warning' : 'error'}
              />
              <Typography variant="caption" color="text.secondary">
                {new Date(message.timestamp).toLocaleTimeString()}
              </Typography>
            </Box>
          )}

          {!isQuestion && message.sources && message.sources.length > 0 && (
            <Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                <IconButton 
                  size="small" 
                  onClick={() => setShowSources(!showSources)}
                >
                  {showSources ? <ExpandLess /> : <ExpandMore />}
                </IconButton>
                <Typography variant="body2" color="text.secondary">
                  {message.sources.length} source{message.sources.length > 1 ? 's' : ''}
                </Typography>
              </Box>

              <Collapse in={showSources}>
                <Box sx={{ mt: 1 }}>
                  {message.sources.map((source, index) => (
                    <Card key={index} sx={{ mb: 1 }}>
                      <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                          <Typography variant="subtitle2">
                            {source.title}
                          </Typography>
                          <Box sx={{ display: 'flex', gap: 1 }}>
                            <Chip 
                              label={`Score: ${source.score.toFixed(2)}`} 
                              size="small" 
                              variant="outlined" 
                            />
                            <IconButton 
                              size="small"
                              onClick={() => window.open(`/documents/${source.documentId}`, '_blank')}
                            >
                              <OpenInNew fontSize="small" />
                            </IconButton>
                          </Box>
                        </Box>
                        <Typography variant="body2" color="text.secondary">
                          {source.content.substring(0, 200)}...
                        </Typography>
                      </CardContent>
                    </Card>
                  ))}
                </Box>
              </Collapse>
            </Box>
          )}

          {!isQuestion && (
            <>
              <Divider sx={{ my: 1 }} />
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="caption" color="text.secondary">
                  Was this helpful?
                </Typography>
                <Box>
                  <IconButton 
                    size="small"
                    onClick={() => handleFeedback('helpful')}
                    color={feedback === 'helpful' ? 'success' : 'default'}
                  >
                    <ThumbUp fontSize="small" />
                  </IconButton>
                  <IconButton 
                    size="small"
                    onClick={() => handleFeedback('not_helpful')}
                    color={feedback === 'not_helpful' ? 'error' : 'default'}
                  >
                    <ThumbDown fontSize="small" />
                  </IconButton>
                </Box>
              </Box>
            </>
          )}
        </Paper>
      </Box>
    </Box>
  );
};

export default ChatMessage;
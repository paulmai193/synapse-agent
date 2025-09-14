import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Paper,
  Typography,
  CircularProgress,
} from '@mui/material';
import ChatMessage from './ChatMessage';
import ChatInput from './ChatInput';
import { Conversation, Message, QARequest } from '../../types/qa';
import { qaApi } from '../../utils/qaApi';

interface ChatWindowProps {
  conversation: Conversation | null;
  onConversationUpdate: (conversation: Conversation) => void;
}

const ChatWindow: React.FC<ChatWindowProps> = ({ 
  conversation, 
  onConversationUpdate 
}) => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (conversation) {
      setMessages(conversation.messages);
    } else {
      setMessages([]);
    }
  }, [conversation]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const handleSendMessage = async (request: QARequest) => {
    if (!request.question.trim()) return;

    // Add user question to messages
    const questionMessage: Message = {
      id: Date.now().toString(),
      type: 'question',
      content: request.question,
      timestamp: new Date().toISOString(),
    };

    setMessages(prev => [...prev, questionMessage]);
    setLoading(true);

    try {
      const response = await qaApi.askQuestion({
        ...request,
        conversationId: conversation?.id,
      });

      // Add AI response to messages
      const answerMessage: Message = {
        id: response.id,
        type: 'answer',
        content: response.answer,
        timestamp: new Date().toISOString(),
        sources: response.sources,
        confidence: response.confidence,
      };

      setMessages(prev => [...prev, answerMessage]);

      // Update conversation
      if (conversation) {
        const updatedConversation: Conversation = {
          ...conversation,
          messages: [...conversation.messages, questionMessage, answerMessage],
          updatedAt: new Date().toISOString(),
        };
        onConversationUpdate(updatedConversation);
      } else {
        // Create new conversation
        const newConversation: Conversation = {
          id: response.conversationId,
          title: request.question.substring(0, 50) + (request.question.length > 50 ? '...' : ''),
          messages: [questionMessage, answerMessage],
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
          bookmarked: false,
        };
        onConversationUpdate(newConversation);
      }
    } catch (error) {
      console.error('Failed to get answer:', error);
      
      // Add error message
      const errorMessage: Message = {
        id: Date.now().toString(),
        type: 'answer',
        content: 'Sorry, I encountered an error while processing your question. Please try again.',
        timestamp: new Date().toISOString(),
        confidence: 0,
      };
      
      setMessages(prev => [...prev, errorMessage]);
    } finally {
      setLoading(false);
    }
  };

  if (!conversation && messages.length === 0) {
    return (
      <Paper sx={{ 
        height: '100%', 
        display: 'flex', 
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        textAlign: 'center',
        p: 3
      }}>
        <Typography variant="h5" color="text.secondary" gutterBottom>
          Welcome to Q&A
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Ask any question about your documents and get intelligent answers with source citations.
        </Typography>
      </Paper>
    );
  }

  return (
    <Paper sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {conversation && (
        <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
          <Typography variant="h6" noWrap>
            {conversation.title}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            {conversation.messages.length} messages • Last updated {new Date(conversation.updatedAt).toLocaleString()}
          </Typography>
        </Box>
      )}

      <Box sx={{ 
        flexGrow: 1, 
        overflow: 'auto', 
        p: 2,
        display: 'flex',
        flexDirection: 'column'
      }}>
        {messages.map((message) => (
          <ChatMessage key={message.id} message={message} />
        ))}
        
        {loading && (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
            <Box sx={{ 
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'center',
              width: 40,
              height: 40,
              borderRadius: '50%',
              bgcolor: 'secondary.main',
              color: 'white'
            }}>
              <CircularProgress size={20} color="inherit" />
            </Box>
            <Typography variant="body2" color="text.secondary">
              Thinking...
            </Typography>
          </Box>
        )}
        
        <div ref={messagesEndRef} />
      </Box>

      <ChatInput 
        onSendMessage={handleSendMessage}
        loading={loading}
        conversationId={conversation?.id}
      />
    </Paper>
  );
};

export default ChatWindow;
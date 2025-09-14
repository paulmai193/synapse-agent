import React, { useState } from 'react';
import {
  Box,
  TextField,
  IconButton,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import { Send } from '@mui/icons-material';
import { QARequest } from '../../types/qa';

interface ChatInputProps {
  onSendMessage: (request: QARequest) => void;
  loading?: boolean;
  conversationId?: string;
}

const ChatInput: React.FC<ChatInputProps> = ({ 
  onSendMessage, 
  loading = false, 
  conversationId 
}) => {
  const [question, setQuestion] = useState('');
  const [language, setLanguage] = useState('auto');

  const handleSend = () => {
    if (question.trim()) {
      const request: QARequest = {
        question: question.trim(),
        language: language === 'auto' ? undefined : language,
        conversationId,
      };
      onSendMessage(request);
      setQuestion('');
    }
  };

  const handleKeyPress = (event: React.KeyboardEvent) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      handleSend();
    }
  };

  const languages = [
    { code: 'auto', name: 'Auto-detect' },
    { code: 'en', name: 'English' },
    { code: 'vi', name: 'Vietnamese' },
    { code: 'ja', name: 'Japanese' },
    { code: 'zh', name: 'Chinese' },
    { code: 'ko', name: 'Korean' },
  ];

  return (
    <Box sx={{ display: 'flex', gap: 2, p: 2, borderTop: 1, borderColor: 'divider' }}>
      <TextField
        fullWidth
        multiline
        maxRows={4}
        placeholder="Ask a question..."
        value={question}
        onChange={(e) => setQuestion(e.target.value)}
        onKeyPress={handleKeyPress}
        disabled={loading}
        variant="outlined"
      />
      
      <FormControl sx={{ minWidth: 120 }}>
        <InputLabel size="small">Language</InputLabel>
        <Select
          size="small"
          value={language}
          onChange={(e) => setLanguage(e.target.value)}
          disabled={loading}
        >
          {languages.map((lang) => (
            <MenuItem key={lang.code} value={lang.code}>
              {lang.name}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      <IconButton 
        onClick={handleSend} 
        disabled={!question.trim() || loading}
        color="primary"
      >
        <Send />
      </IconButton>
    </Box>
  );
};

export default ChatInput;
import React, { useState } from 'react';
import { Box, Grid } from '@mui/material';
import ConversationList from '../components/qa/ConversationList';
import ChatWindow from '../components/qa/ChatWindow';
import { Conversation } from '../types/qa';

const QA: React.FC = () => {
  const [selectedConversation, setSelectedConversation] = useState<Conversation | null>(null);

  const handleSelectConversation = (conversation: Conversation | null) => {
    setSelectedConversation(conversation);
  };

  const handleNewConversation = () => {
    setSelectedConversation(null);
  };

  const handleConversationUpdate = (conversation: Conversation) => {
    setSelectedConversation(conversation);
  };

  return (
    <Box sx={{ height: 'calc(100vh - 200px)' }}>
      <Grid container spacing={2} sx={{ height: '100%' }}>
        <Grid item xs={12} md={4}>
          <ConversationList
            selectedConversationId={selectedConversation?.id}
            onSelectConversation={handleSelectConversation}
            onNewConversation={handleNewConversation}
          />
        </Grid>
        <Grid item xs={12} md={8}>
          <ChatWindow
            conversation={selectedConversation}
            onConversationUpdate={handleConversationUpdate}
          />
        </Grid>
      </Grid>
    </Box>
  );
};

export default QA;
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { QAState, QARequest, QAResponse, Conversation, FeedbackRequest, ConversationMessage } from '../../types/qa';
import { qaApi } from '../../services/qaApi';

const initialState: QAState = {
  conversations: [],
  currentConversation: null,
  isLoading: false,
  error: null,
  searchSuggestions: []
};

// Async thunks
export const askQuestion = createAsyncThunk(
  'qa/askQuestion',
  async (request: QARequest, { rejectWithValue }) => {
    try {
      const response = await qaApi.askQuestion(request);
      return response;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to ask question');
    }
  }
);

export const loadConversationHistory = createAsyncThunk(
  'qa/loadConversationHistory',
  async (_, { rejectWithValue }) => {
    try {
      const conversations = await qaApi.getConversationHistory();
      return conversations;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to load conversation history');
    }
  }
);

export const loadConversation = createAsyncThunk(
  'qa/loadConversation',
  async (conversationId: string, { rejectWithValue }) => {
    try {
      const conversation = await qaApi.getConversation(conversationId);
      return conversation;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to load conversation');
    }
  }
);

export const createNewConversation = createAsyncThunk(
  'qa/createNewConversation',
  async (_, { rejectWithValue }) => {
    try {
      const conversation = await qaApi.createConversation();
      return conversation;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to create conversation');
    }
  }
);

export const deleteConversation = createAsyncThunk(
  'qa/deleteConversation',
  async (conversationId: string, { rejectWithValue }) => {
    try {
      await qaApi.deleteConversation(conversationId);
      return conversationId;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to delete conversation');
    }
  }
);

export const provideFeedback = createAsyncThunk(
  'qa/provideFeedback',
  async (feedback: FeedbackRequest, { rejectWithValue }) => {
    try {
      await qaApi.provideFeedback(feedback);
      return feedback;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to provide feedback');
    }
  }
);

export const bookmarkConversation = createAsyncThunk(
  'qa/bookmarkConversation',
  async (messageId: string, { rejectWithValue }) => {
    try {
      await qaApi.bookmarkMessage(messageId);
      return messageId;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to bookmark conversation');
    }
  }
);

export const getSearchSuggestions = createAsyncThunk(
  'qa/getSearchSuggestions',
  async (query: string, { rejectWithValue }) => {
    try {
      const suggestions = await qaApi.getSearchSuggestions(query);
      return suggestions;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to get suggestions');
    }
  }
);

const qaSlice = createSlice({
  name: 'qa',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    clearCurrentConversation: (state) => {
      state.currentConversation = null;
    },
    updateMessageFeedback: (state, action: PayloadAction<{ messageId: string; isHelpful: boolean }>) => {
      if (state.currentConversation) {
        const message = state.currentConversation.messages.find(m => m.id === action.payload.messageId);
        if (message) {
          message.feedback = {
            isHelpful: action.payload.isHelpful,
            timestamp: new Date().toISOString()
          };
        }
      }
    },
    updateMessageBookmark: (state, action: PayloadAction<string>) => {
      if (state.currentConversation) {
        const message = state.currentConversation.messages.find(m => m.id === action.payload);
        if (message) {
          message.isBookmarked = !message.isBookmarked;
        }
      }
    },
    addUserMessage: (state, action: PayloadAction<{ question: string; conversationId?: string }>) => {
      const userMessage: ConversationMessage = {
        id: `temp-${Date.now()}`,
        role: 'user',
        content: action.payload.question,
        timestamp: new Date().toISOString()
      };

      if (state.currentConversation) {
        state.currentConversation.messages.push(userMessage);
      } else {
        // Create a new conversation if none exists
        const newConversation: Conversation = {
          id: action.payload.conversationId || `conv-${Date.now()}`,
          title: action.payload.question.substring(0, 50) + (action.payload.question.length > 50 ? '...' : ''),
          messages: [userMessage],
          isBookmarked: false,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
          userId: 'current-user' // This should come from auth state
        };
        state.currentConversation = newConversation;
        state.conversations.unshift(newConversation);
      }
    }
  },
  extraReducers: (builder) => {
    builder
      // Ask Question
      .addCase(askQuestion.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(askQuestion.fulfilled, (state, action) => {
        state.isLoading = false;
        
        const response = action.payload;
        const assistantMessage: ConversationMessage = {
          id: response.id,
          role: 'assistant',
          content: response.answer,
          timestamp: response.timestamp,
          sources: response.sources,
          confidenceScore: response.confidenceScore
        };

        if (state.currentConversation) {
          state.currentConversation.messages.push(assistantMessage);
          state.currentConversation.updatedAt = response.timestamp;
          
          // Update conversation in the list
          const conversationIndex = state.conversations.findIndex(
            c => c.id === state.currentConversation?.id
          );
          if (conversationIndex >= 0) {
            state.conversations[conversationIndex] = { ...state.currentConversation };
          }
        }
      })
      .addCase(askQuestion.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })

      // Load Conversation History
      .addCase(loadConversationHistory.fulfilled, (state, action) => {
        state.conversations = action.payload;
      })

      // Load Conversation
      .addCase(loadConversation.fulfilled, (state, action) => {
        state.currentConversation = action.payload;
      })

      // Create New Conversation
      .addCase(createNewConversation.fulfilled, (state, action) => {
        state.currentConversation = action.payload;
        state.conversations.unshift(action.payload);
      })

      // Delete Conversation
      .addCase(deleteConversation.fulfilled, (state, action) => {
        state.conversations = state.conversations.filter(c => c.id !== action.payload);
        if (state.currentConversation?.id === action.payload) {
          state.currentConversation = null;
        }
      })

      // Provide Feedback
      .addCase(provideFeedback.fulfilled, (state, action) => {
        const { messageId, isHelpful } = action.payload;
        if (state.currentConversation) {
          const message = state.currentConversation.messages.find(m => m.id === messageId);
          if (message) {
            message.feedback = {
              isHelpful,
              timestamp: new Date().toISOString()
            };
          }
        }
      })

      // Bookmark Conversation
      .addCase(bookmarkConversation.fulfilled, (state, action) => {
        const messageId = action.payload;
        if (state.currentConversation) {
          const message = state.currentConversation.messages.find(m => m.id === messageId);
          if (message) {
            message.isBookmarked = !message.isBookmarked;
          }
        }
      })

      // Get Search Suggestions
      .addCase(getSearchSuggestions.fulfilled, (state, action) => {
        state.searchSuggestions = action.payload;
      });
  }
});

export const {
  clearError,
  clearCurrentConversation,
  updateMessageFeedback,
  updateMessageBookmark,
  addUserMessage
} = qaSlice.actions;

export default qaSlice.reducer;
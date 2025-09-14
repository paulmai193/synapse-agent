import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { QAInterface } from './QAInterface';
import qaReducer from '../../store/slices/qaSlice';
import authReducer from '../../store/slices/authSlice';
import { ConversationMessage } from '../../types/qa';

// Mock the API
jest.mock('../../services/qaApi', () => ({
  qaApi: {
    askQuestion: jest.fn(),
    provideFeedback: jest.fn(),
    bookmarkMessage: jest.fn(),
    getSearchSuggestions: jest.fn()
  }
}));

const mockStore = configureStore({
  reducer: {
    qa: qaReducer,
    auth: authReducer
  },
  preloadedState: {
    qa: {
      conversations: [],
      currentConversation: {
        id: 'test-conversation',
        title: 'Test Conversation',
        messages: [
          {
            id: 'msg-1',
            role: 'user',
            content: 'What is machine learning?',
            timestamp: '2024-01-01T10:00:00Z'
          },
          {
            id: 'msg-2',
            role: 'assistant',
            content: 'Machine learning is a subset of artificial intelligence...',
            timestamp: '2024-01-01T10:00:05Z',
            sources: [
              {
                id: 'source-1',
                documentId: 'doc-1',
                title: 'ML Basics',
                snippet: 'Machine learning fundamentals...',
                relevanceScore: 0.95,
                chunkIndex: 0,
                startPosition: 0,
                endPosition: 100
              }
            ],
            confidenceScore: 0.9
          }
        ] as ConversationMessage[],
        isBookmarked: false,
        createdAt: '2024-01-01T10:00:00Z',
        updatedAt: '2024-01-01T10:00:05Z',
        userId: 'user-1'
      },
      isLoading: false,
      error: null,
      searchSuggestions: []
    },
    auth: {
      user: { id: 'user-1', email: 'test@example.com', roles: ['USER'] },
      token: 'test-token',
      isAuthenticated: true,
      isLoading: false,
      error: null
    }
  }
});

const renderWithProvider = (component: React.ReactElement) => {
  return render(
    <Provider store={mockStore}>
      {component}
    </Provider>
  );
};

describe('QAInterface', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders Q&A interface with header', () => {
    renderWithProvider(<QAInterface />);
    
    expect(screen.getByText('Q&A Assistant')).toBeInTheDocument();
    expect(screen.getByText('Ask questions about your documents in any language')).toBeInTheDocument();
  });

  it('displays conversation messages', () => {
    renderWithProvider(<QAInterface />);
    
    expect(screen.getByText('What is machine learning?')).toBeInTheDocument();
    expect(screen.getByText('Machine learning is a subset of artificial intelligence...')).toBeInTheDocument();
  });

  it('shows source citations for assistant messages', () => {
    renderWithProvider(<QAInterface />);
    
    expect(screen.getByText('Sources (1):')).toBeInTheDocument();
    expect(screen.getByText('ML Basics (95%)')).toBeInTheDocument();
  });

  it('displays confidence score', () => {
    renderWithProvider(<QAInterface />);
    
    expect(screen.getByText('90% confident')).toBeInTheDocument();
  });

  it('allows user to type and submit questions', async () => {
    renderWithProvider(<QAInterface />);
    
    const input = screen.getByPlaceholderText('Ask a question about your documents...');
    const submitButton = screen.getByRole('button', { name: /send/i });
    
    fireEvent.change(input, { target: { value: 'What is deep learning?' } });
    expect(input).toHaveValue('What is deep learning?');
    
    fireEvent.click(submitButton);
    
    await waitFor(() => {
      expect(input).toHaveValue('');
    });
  });

  it('submits question on Enter key press', async () => {
    renderWithProvider(<QAInterface />);
    
    const input = screen.getByPlaceholderText('Ask a question about your documents...');
    
    fireEvent.change(input, { target: { value: 'Test question' } });
    fireEvent.keyDown(input, { key: 'Enter', code: 'Enter' });
    
    await waitFor(() => {
      expect(input).toHaveValue('');
    });
  });

  it('does not submit on Shift+Enter', () => {
    renderWithProvider(<QAInterface />);
    
    const input = screen.getByPlaceholderText('Ask a question about your documents...');
    
    fireEvent.change(input, { target: { value: 'Test question' } });
    fireEvent.keyDown(input, { key: 'Enter', code: 'Enter', shiftKey: true });
    
    expect(input).toHaveValue('Test question');
  });

  it('shows feedback buttons for assistant messages', () => {
    renderWithProvider(<QAInterface />);
    
    const thumbsUpButtons = screen.getAllByTitle('Helpful');
    const thumbsDownButtons = screen.getAllByTitle('Not helpful');
    
    expect(thumbsUpButtons).toHaveLength(1);
    expect(thumbsDownButtons).toHaveLength(1);
  });

  it('shows bookmark button for assistant messages', () => {
    renderWithProvider(<QAInterface />);
    
    const bookmarkButtons = screen.getAllByTitle('Bookmark');
    expect(bookmarkButtons).toHaveLength(1);
  });

  it('shows copy button for assistant messages', () => {
    renderWithProvider(<QAInterface />);
    
    const copyButtons = screen.getAllByTitle('Copy');
    expect(copyButtons).toHaveLength(1);
  });

  it('expands source details when clicked', () => {
    renderWithProvider(<QAInterface />);
    
    const sourceChip = screen.getByText('ML Basics (95%)');
    fireEvent.click(sourceChip);
    
    expect(screen.getByText('Machine learning fundamentals...')).toBeInTheDocument();
  });

  it('handles feedback submission', async () => {
    const mockProvideFeedback = jest.fn().mockResolvedValue({});
    jest.doMock('../../services/qaApi', () => ({
      qaApi: {
        provideFeedback: mockProvideFeedback
      }
    }));

    renderWithProvider(<QAInterface />);
    
    const thumbsUpButton = screen.getByTitle('Helpful');
    fireEvent.click(thumbsUpButton);
    
    await waitFor(() => {
      expect(mockProvideFeedback).toHaveBeenCalledWith({
        messageId: 'msg-2',
        isHelpful: true,
        comment: ''
      });
    });
  });

  it('shows empty state when no messages', () => {
    const emptyStore = configureStore({
      reducer: {
        qa: qaReducer,
        auth: authReducer
      },
      preloadedState: {
        qa: {
          conversations: [],
          currentConversation: {
            id: 'empty-conversation',
            title: 'Empty Conversation',
            messages: [],
            isBookmarked: false,
            createdAt: '2024-01-01T10:00:00Z',
            updatedAt: '2024-01-01T10:00:00Z',
            userId: 'user-1'
          },
          isLoading: false,
          error: null,
          searchSuggestions: []
        },
        auth: {
          user: { id: 'user-1', email: 'test@example.com', roles: ['USER'] },
          token: 'test-token',
          isAuthenticated: true,
          isLoading: false,
          error: null
        }
      }
    });

    render(
      <Provider store={emptyStore}>
        <QAInterface />
      </Provider>
    );
    
    expect(screen.getByText('Start a conversation')).toBeInTheDocument();
    expect(screen.getByText('Ask questions about your documents and get intelligent answers with source citations')).toBeInTheDocument();
  });

  it('shows loading indicator when processing', () => {
    const loadingStore = configureStore({
      reducer: {
        qa: qaReducer,
        auth: authReducer
      },
      preloadedState: {
        qa: {
          conversations: [],
          currentConversation: null,
          isLoading: true,
          error: null,
          searchSuggestions: []
        },
        auth: {
          user: { id: 'user-1', email: 'test@example.com', roles: ['USER'] },
          token: 'test-token',
          isAuthenticated: true,
          isLoading: false,
          error: null
        }
      }
    });

    render(
      <Provider store={loadingStore}>
        <QAInterface />
      </Provider>
    );
    
    expect(screen.getByText('Thinking...')).toBeInTheDocument();
  });

  it('shows error message when there is an error', () => {
    const errorStore = configureStore({
      reducer: {
        qa: qaReducer,
        auth: authReducer
      },
      preloadedState: {
        qa: {
          conversations: [],
          currentConversation: null,
          isLoading: false,
          error: 'Failed to process question',
          searchSuggestions: []
        },
        auth: {
          user: { id: 'user-1', email: 'test@example.com', roles: ['USER'] },
          token: 'test-token',
          isAuthenticated: true,
          isLoading: false,
          error: null
        }
      }
    });

    render(
      <Provider store={errorStore}>
        <QAInterface />
      </Provider>
    );
    
    expect(screen.getByText('Failed to process question')).toBeInTheDocument();
  });
});
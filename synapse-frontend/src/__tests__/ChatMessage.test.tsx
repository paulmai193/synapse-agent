import { render, screen, fireEvent } from '@testing-library/react';
import ChatMessage from '../components/qa/ChatMessage';
import { Message } from '../types/qa';

// Mock QA API
jest.mock('../utils/qaApi', () => ({
  qaApi: {
    provideFeedback: jest.fn(),
  },
}));

describe('ChatMessage Component', () => {
  const mockQuestionMessage: Message = {
    id: '1',
    type: 'question',
    content: 'What is the test question?',
    timestamp: '2024-01-01T00:00:00Z',
  };

  const mockAnswerMessage: Message = {
    id: '2',
    type: 'answer',
    content: 'This is the test answer.',
    timestamp: '2024-01-01T00:01:00Z',
    confidence: 0.85,
    sources: [
      {
        documentId: 'doc1',
        title: 'Test Document',
        content: 'Test content for the document',
        score: 0.9,
      }
    ],
  };

  test('renders question message', () => {
    render(<ChatMessage message={mockQuestionMessage} />);
    
    expect(screen.getByText('What is the test question?')).toBeInTheDocument();
  });

  test('renders answer message with confidence', () => {
    render(<ChatMessage message={mockAnswerMessage} />);
    
    expect(screen.getByText('This is the test answer.')).toBeInTheDocument();
    expect(screen.getByText('Confidence: 85%')).toBeInTheDocument();
  });

  test('shows sources when expand button clicked', () => {
    render(<ChatMessage message={mockAnswerMessage} />);
    
    const expandButton = screen.getByRole('button');
    fireEvent.click(expandButton);
    
    expect(screen.getByText('Test Document')).toBeInTheDocument();
    expect(screen.getByText('Score: 0.90')).toBeInTheDocument();
  });

  test('shows feedback buttons for answer messages', () => {
    render(<ChatMessage message={mockAnswerMessage} />);
    
    expect(screen.getByText('Was this helpful?')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '' })).toBeInTheDocument(); // Thumb up/down buttons
  });
});
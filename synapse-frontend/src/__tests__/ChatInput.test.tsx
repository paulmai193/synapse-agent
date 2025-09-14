import { render, screen, fireEvent } from '@testing-library/react';
import ChatInput from '../components/qa/ChatInput';

describe('ChatInput Component', () => {
  const mockOnSendMessage = jest.fn();

  beforeEach(() => {
    mockOnSendMessage.mockClear();
  });

  test('renders chat input', () => {
    render(<ChatInput onSendMessage={mockOnSendMessage} />);
    
    expect(screen.getByPlaceholderText('Ask a question...')).toBeInTheDocument();
    expect(screen.getByLabelText('Language')).toBeInTheDocument();
  });

  test('calls onSendMessage when send button clicked', () => {
    render(<ChatInput onSendMessage={mockOnSendMessage} />);
    
    const input = screen.getByPlaceholderText('Ask a question...');
    const sendButton = screen.getByRole('button', { name: '' }); // Send icon button
    
    fireEvent.change(input, { target: { value: 'Test question' } });
    fireEvent.click(sendButton);
    
    expect(mockOnSendMessage).toHaveBeenCalledWith({
      question: 'Test question',
      language: undefined,
      conversationId: undefined,
    });
  });

  test('calls onSendMessage when Enter key pressed', () => {
    render(<ChatInput onSendMessage={mockOnSendMessage} />);
    
    const input = screen.getByPlaceholderText('Ask a question...');
    
    fireEvent.change(input, { target: { value: 'Test question' } });
    fireEvent.keyPress(input, { key: 'Enter', code: 'Enter' });
    
    expect(mockOnSendMessage).toHaveBeenCalledWith({
      question: 'Test question',
      language: undefined,
      conversationId: undefined,
    });
  });

  test('disables send when question is empty', () => {
    render(<ChatInput onSendMessage={mockOnSendMessage} />);
    
    const sendButton = screen.getByRole('button', { name: '' });
    expect(sendButton).toBeDisabled();
  });

  test('clears input after sending', () => {
    render(<ChatInput onSendMessage={mockOnSendMessage} />);
    
    const input = screen.getByPlaceholderText('Ask a question...') as HTMLInputElement;
    const sendButton = screen.getByRole('button', { name: '' });
    
    fireEvent.change(input, { target: { value: 'Test question' } });
    fireEvent.click(sendButton);
    
    expect(input.value).toBe('');
  });
});
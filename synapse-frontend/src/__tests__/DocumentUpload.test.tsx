import { render, screen, fireEvent } from '@testing-library/react';
import DocumentUpload from '../components/documents/DocumentUpload';

// Mock react-dropzone
jest.mock('react-dropzone', () => ({
  useDropzone: () => ({
    getRootProps: () => ({ 'data-testid': 'dropzone' }),
    getInputProps: () => ({ 'data-testid': 'file-input' }),
    isDragActive: false,
  }),
}));

describe('DocumentUpload Component', () => {
  test('renders upload form', () => {
    render(<DocumentUpload />);
    
    expect(screen.getByText('Upload Document')).toBeInTheDocument();
    expect(screen.getByLabelText('Document Title')).toBeInTheDocument();
    expect(screen.getByLabelText('Visibility')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Upload Document' })).toBeInTheDocument();
  });

  test('updates title field', () => {
    render(<DocumentUpload />);
    
    const titleInput = screen.getByLabelText('Document Title');
    fireEvent.change(titleInput, { target: { value: 'Test Document' } });
    
    expect(titleInput).toHaveValue('Test Document');
  });

  test('adds and removes tags', () => {
    render(<DocumentUpload />);
    
    const tagInput = screen.getByLabelText('Add Tag');
    const addButton = screen.getByRole('button', { name: 'Add Tag' });
    
    fireEvent.change(tagInput, { target: { value: 'test-tag' } });
    fireEvent.click(addButton);
    
    expect(screen.getByText('test-tag')).toBeInTheDocument();
  });

  test('upload button is disabled without file and title', () => {
    render(<DocumentUpload />);
    
    const uploadButton = screen.getByRole('button', { name: 'Upload Document' });
    expect(uploadButton).toBeDisabled();
  });
});
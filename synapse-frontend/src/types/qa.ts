export interface QARequest {
  question: string;
  language?: string;
  conversationId?: string;
}

export interface QAResponse {
  id: string;
  answer: string;
  confidence: number;
  sources: QASource[];
  language: string;
  processingTime: number;
  conversationId: string;
}

export interface QASource {
  documentId: string;
  title: string;
  content: string;
  score: number;
  page?: number;
}

export interface Conversation {
  id: string;
  title: string;
  messages: Message[];
  createdAt: string;
  updatedAt: string;
  bookmarked: boolean;
}

export interface Message {
  id: string;
  type: 'question' | 'answer';
  content: string;
  timestamp: string;
  sources?: QASource[];
  confidence?: number;
  feedback?: 'helpful' | 'not_helpful';
}
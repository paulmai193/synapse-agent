export interface QARequest {
  question: string;
  language: string;
  conversationId?: string;
  context?: {
    documentIds?: string[];
    projectId?: string;
    departmentIds?: string[];
  };
}

export interface QAResponse {
  id: string;
  answer: string;
  language: string;
  sources: SourceReference[];
  confidenceScore: number;
  conversationId: string;
  timestamp: string;
  processingTime: number;
}

export interface SourceReference {
  id: string;
  documentId: string;
  title: string;
  snippet: string;
  relevanceScore: number;
  chunkIndex: number;
  startPosition: number;
  endPosition: number;
}

export interface ConversationMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: string;
  sources?: SourceReference[];
  confidenceScore?: number;
  feedback?: MessageFeedback;
  isBookmarked?: boolean;
}

export interface MessageFeedback {
  isHelpful: boolean;
  comment?: string;
  timestamp: string;
}

export interface Conversation {
  id: string;
  title: string;
  messages: ConversationMessage[];
  isBookmarked: boolean;
  createdAt: string;
  updatedAt: string;
  userId: string;
}

export interface FeedbackRequest {
  messageId: string;
  isHelpful: boolean;
  comment?: string;
}

export interface QAState {
  conversations: Conversation[];
  currentConversation: Conversation | null;
  isLoading: boolean;
  error: string | null;
  searchSuggestions: string[];
}

export interface QAAnalytics {
  totalQuestions: number;
  averageConfidenceScore: number;
  mostCommonTopics: string[];
  languageDistribution: Record<string, number>;
  userSatisfactionRate: number;
}
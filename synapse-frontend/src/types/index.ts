export interface User {
  id: number;
  username: string;
  email: string;
  roles: Role[];
  status: 'ACTIVE' | 'INACTIVE';
  projectId?: number;
  departmentIds: number[];
}

export interface Role {
  id: number;
  name: string;
  description?: string;
}

export interface Project {
  id: number;
  name: string;
  description?: string;
  status: 'ACTIVE' | 'INACTIVE';
}

export interface Department {
  id: number;
  name: string;
  description?: string;
  status: 'ACTIVE' | 'INACTIVE';
}

export interface Document {
  id: string;
  title: string;
  content: string;
  status: 'ACTIVE' | 'INACTIVE';
  projectId?: number;
  departmentIds: number[];
  uploadedBy: number;
  createdAt: string;
  updatedAt: string;
}

export interface SearchResult {
  documentId: string;
  title: string;
  content: string;
  relevanceScore: number;
  source: string;
}

export interface QAResponse {
  question: string;
  answer: string;
  confidence: number;
  language: string;
  conversationId: string;
  sources: SearchResult[];
  responseTimeMs: number;
}
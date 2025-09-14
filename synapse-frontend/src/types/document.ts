export interface Document {
  id: string;
  title: string;
  content: string;
  originalFormat: string;
  language: string;
  status: 'ACTIVE' | 'INACTIVE';
  deleted: boolean;
  metadata: {
    author: string;
    createdDate: string;
    source: string;
    sourceId: string;
    tags: string[];
    fileSize: number;
    mimeType: string;
  };
  accessControl: {
    projectId?: string;
    departmentIds: string[];
    uploadedBy: string;
    visibility: 'PROJECT' | 'DEPARTMENT' | 'RESTRICTED';
  };
  processingStatus: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  createdAt: string;
  updatedAt: string;
}

export interface DocumentUploadRequest {
  title: string;
  visibility: 'PROJECT' | 'DEPARTMENT';
  departmentIds?: string[];
  tags?: string[];
}
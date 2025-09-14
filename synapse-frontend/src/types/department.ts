export interface Department {
  id: string;
  name: string;
  description?: string;
  status: 'ACTIVE' | 'INACTIVE';
  userCount?: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateDepartmentRequest {
  name: string;
  description?: string;
  status: 'ACTIVE' | 'INACTIVE';
}

export interface UpdateDepartmentRequest {
  name?: string;
  description?: string;
  status?: 'ACTIVE' | 'INACTIVE';
}

export interface DepartmentState {
  departments: Department[];
  selectedDepartment: Department | null;
  isLoading: boolean;
  error: string | null;
  totalCount: number;
}
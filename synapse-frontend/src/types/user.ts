export interface User {
  id: string;
  username: string;
  email: string;
  status: 'ACTIVE' | 'INACTIVE';
  roles: Role[];
  project?: Project;
  departments: Department[];
  createdAt: string;
  updatedAt: string;
}

export interface Role {
  id: string;
  name: string;
  description?: string;
  permissions?: string[];
}

export interface Project {
  id: string;
  name: string;
  description?: string;
  status: 'ACTIVE' | 'INACTIVE';
}

export interface Department {
  id: string;
  name: string;
  description?: string;
  status: 'ACTIVE' | 'INACTIVE';
}

export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  status: 'ACTIVE' | 'INACTIVE';
  roles: string[];
  projectId?: string | null;
  departmentIds: string[];
}

export interface UpdateUserRequest {
  username?: string;
  email?: string;
  password?: string;
  status?: 'ACTIVE' | 'INACTIVE';
  roles?: string[];
  projectId?: string | null;
  departmentIds?: string[];
}

export interface BulkUpdateRequest {
  userIds: string[];
  updates: {
    status?: string;
    addRoles?: string[];
    removeRoles?: string[];
    projectId?: string;
  };
}

export interface UserState {
  users: User[];
  selectedUser: User | null;
  isLoading: boolean;
  error: string | null;
  totalCount: number;
}
export interface User {
  id: string;
  username: string;
  email: string;
  roles: Role[];
  projectId?: string;
  departmentIds: string[];
  status: 'ACTIVE' | 'INACTIVE';
}

export interface Role {
  id: string;
  name: 'SYSTEM_ADMIN' | 'PROJECT_ADMIN' | 'DEPARTMENT_ADMIN' | 'USER';
  permissions: string[];
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  loading: boolean;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}
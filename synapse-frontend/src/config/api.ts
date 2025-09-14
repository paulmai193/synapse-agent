export const API_CONFIG = {
  BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  TIMEOUT: 30000,
  ENDPOINTS: {
    AUTH: {
      LOGIN: '/auth/login',
      REGISTER: '/auth/register',
      REFRESH: '/auth/refresh',
      LOGOUT: '/auth/logout'
    },
    USERS: '/users',
    PROJECTS: '/projects',
    DEPARTMENTS: '/departments',
    DOCUMENTS: '/documents',
    SEARCH: '/search',
    QA: '/search/qa',
    AUDIT: '/audit'
  }
};

export const STORAGE_KEYS = {
  TOKEN: 'synapse_token',
  REFRESH_TOKEN: 'synapse_refresh_token',
  USER: 'synapse_user',
  THEME: 'synapse_theme'
};
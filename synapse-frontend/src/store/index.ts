import { configureStore } from '@reduxjs/toolkit';
import authReducer from './authSlice';
import userReducer from './slices/userSlice';
import projectReducer from './slices/projectSlice';
import departmentReducer from './slices/departmentSlice';
import auditReducer from './slices/auditSlice';
import qaReducer from './slices/qaSlice';
import uiReducer from './slices/uiSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    users: userReducer,
    projects: projectReducer,
    departments: departmentReducer,
    audit: auditReducer,
    qa: qaReducer,
    ui: uiReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
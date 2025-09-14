import { configureStore } from '@reduxjs/toolkit';
import authSlice from './slices/authSlice';
import uiSlice from './slices/uiSlice';
import qaSlice from './slices/qaSlice';
import userSlice from './slices/userSlice';
import projectSlice from './slices/projectSlice';
import departmentSlice from './slices/departmentSlice';

export const store = configureStore({
  reducer: {
    auth: authSlice,
    ui: uiSlice,
    qa: qaSlice,
    users: userSlice,
    projects: projectSlice,
    departments: departmentSlice,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ['persist/PERSIST'],
      },
    }),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
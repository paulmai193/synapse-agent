import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { UserState, User, CreateUserRequest, UpdateUserRequest, BulkUpdateRequest } from '../../types/user';
import { userApi } from '../../services/userApi';

const initialState: UserState = {
  users: [],
  selectedUser: null,
  isLoading: false,
  error: null,
  totalCount: 0
};

export const fetchUsers = createAsyncThunk(
  'users/fetchUsers',
  async (params?: { page?: number; size?: number; search?: string }, { rejectWithValue }) => {
    try {
      const response = await userApi.getUsers(params);
      return response;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch users');
    }
  }
);

export const fetchUser = createAsyncThunk(
  'users/fetchUser',
  async (userId: string, { rejectWithValue }) => {
    try {
      const user = await userApi.getUser(userId);
      return user;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch user');
    }
  }
);

export const createUser = createAsyncThunk(
  'users/createUser',
  async (userData: CreateUserRequest, { rejectWithValue }) => {
    try {
      const user = await userApi.createUser(userData);
      return user;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to create user');
    }
  }
);

export const updateUser = createAsyncThunk(
  'users/updateUser',
  async ({ id, ...userData }: UpdateUserRequest & { id: string }, { rejectWithValue }) => {
    try {
      const user = await userApi.updateUser(id, userData);
      return user;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to update user');
    }
  }
);

export const deleteUser = createAsyncThunk(
  'users/deleteUser',
  async (userId: string, { rejectWithValue }) => {
    try {
      await userApi.deleteUser(userId);
      return userId;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to delete user');
    }
  }
);

export const bulkUpdateUsers = createAsyncThunk(
  'users/bulkUpdateUsers',
  async (bulkData: BulkUpdateRequest, { rejectWithValue }) => {
    try {
      await userApi.bulkUpdateUsers(bulkData);
      return bulkData.userIds;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to bulk update users');
    }
  }
);

export const importUsers = createAsyncThunk(
  'users/importUsers',
  async (formData: FormData, { rejectWithValue }) => {
    try {
      const result = await userApi.importUsers(formData);
      return result;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to import users');
    }
  }
);

export const exportUsers = createAsyncThunk(
  'users/exportUsers',
  async (params: { userIds?: string[]; format: string }, { rejectWithValue }) => {
    try {
      const blob = await userApi.exportUsers(params);
      return blob;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to export users');
    }
  }
);

const userSlice = createSlice({
  name: 'users',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    clearSelectedUser: (state) => {
      state.selectedUser = null;
    }
  },
  extraReducers: (builder) => {
    builder
      // Fetch Users
      .addCase(fetchUsers.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchUsers.fulfilled, (state, action) => {
        state.isLoading = false;
        state.users = action.payload.users;
        state.totalCount = action.payload.totalCount;
      })
      .addCase(fetchUsers.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })

      // Fetch User
      .addCase(fetchUser.fulfilled, (state, action) => {
        state.selectedUser = action.payload;
      })

      // Create User
      .addCase(createUser.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(createUser.fulfilled, (state, action) => {
        state.isLoading = false;
        state.users.unshift(action.payload);
        state.totalCount += 1;
      })
      .addCase(createUser.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })

      // Update User
      .addCase(updateUser.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(updateUser.fulfilled, (state, action) => {
        state.isLoading = false;
        const index = state.users.findIndex(user => user.id === action.payload.id);
        if (index !== -1) {
          state.users[index] = action.payload;
        }
        if (state.selectedUser?.id === action.payload.id) {
          state.selectedUser = action.payload;
        }
      })
      .addCase(updateUser.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })

      // Delete User
      .addCase(deleteUser.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(deleteUser.fulfilled, (state, action) => {
        state.isLoading = false;
        state.users = state.users.filter(user => user.id !== action.payload);
        state.totalCount -= 1;
        if (state.selectedUser?.id === action.payload) {
          state.selectedUser = null;
        }
      })
      .addCase(deleteUser.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })

      // Bulk Update Users
      .addCase(bulkUpdateUsers.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(bulkUpdateUsers.fulfilled, (state) => {
        state.isLoading = false;
      })
      .addCase(bulkUpdateUsers.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })

      // Import Users
      .addCase(importUsers.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(importUsers.fulfilled, (state) => {
        state.isLoading = false;
      })
      .addCase(importUsers.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })

      // Export Users
      .addCase(exportUsers.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(exportUsers.fulfilled, (state) => {
        state.isLoading = false;
      })
      .addCase(exportUsers.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      });
  }
});

export const { clearError, clearSelectedUser } = userSlice.actions;
export default userSlice.reducer;
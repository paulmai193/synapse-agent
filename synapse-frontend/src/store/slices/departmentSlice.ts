import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { DepartmentState, Department, CreateDepartmentRequest, UpdateDepartmentRequest } from '../../types/department';
import { departmentApi } from '../../services/departmentApi';

const initialState: DepartmentState = {
  departments: [],
  selectedDepartment: null,
  isLoading: false,
  error: null,
  totalCount: 0
};

export const fetchDepartments = createAsyncThunk(
  'departments/fetchDepartments',
  async (_, { rejectWithValue }) => {
    try {
      const response = await departmentApi.getDepartments();
      return response;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch departments');
    }
  }
);

export const createDepartment = createAsyncThunk(
  'departments/createDepartment',
  async (departmentData: CreateDepartmentRequest, { rejectWithValue }) => {
    try {
      const department = await departmentApi.createDepartment(departmentData);
      return department;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to create department');
    }
  }
);

export const updateDepartment = createAsyncThunk(
  'departments/updateDepartment',
  async ({ id, ...departmentData }: UpdateDepartmentRequest & { id: string }, { rejectWithValue }) => {
    try {
      const department = await departmentApi.updateDepartment(id, departmentData);
      return department;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to update department');
    }
  }
);

export const deleteDepartment = createAsyncThunk(
  'departments/deleteDepartment',
  async (departmentId: string, { rejectWithValue }) => {
    try {
      await departmentApi.deleteDepartment(departmentId);
      return departmentId;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to delete department');
    }
  }
);

export const assignUserToDepartment = createAsyncThunk(
  'departments/assignUserToDepartment',
  async ({ userId, departmentId }: { userId: string; departmentId: string }, { rejectWithValue }) => {
    try {
      await departmentApi.assignUser(departmentId, userId);
      return { userId, departmentId };
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to assign user to department');
    }
  }
);

export const removeUserFromDepartment = createAsyncThunk(
  'departments/removeUserFromDepartment',
  async ({ userId, departmentId }: { userId: string; departmentId: string }, { rejectWithValue }) => {
    try {
      await departmentApi.removeUser(departmentId, userId);
      return { userId, departmentId };
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to remove user from department');
    }
  }
);

const departmentSlice = createSlice({
  name: 'departments',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    clearSelectedDepartment: (state) => {
      state.selectedDepartment = null;
    }
  },
  extraReducers: (builder) => {
    builder
      // Fetch Departments
      .addCase(fetchDepartments.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchDepartments.fulfilled, (state, action) => {
        state.isLoading = false;
        state.departments = action.payload.departments || action.payload;
        state.totalCount = action.payload.totalCount || action.payload.length;
      })
      .addCase(fetchDepartments.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })

      // Create Department
      .addCase(createDepartment.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(createDepartment.fulfilled, (state, action) => {
        state.isLoading = false;
        state.departments.unshift(action.payload);
        state.totalCount += 1;
      })
      .addCase(createDepartment.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })

      // Update Department
      .addCase(updateDepartment.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(updateDepartment.fulfilled, (state, action) => {
        state.isLoading = false;
        const index = state.departments.findIndex(department => department.id === action.payload.id);
        if (index !== -1) {
          state.departments[index] = action.payload;
        }
        if (state.selectedDepartment?.id === action.payload.id) {
          state.selectedDepartment = action.payload;
        }
      })
      .addCase(updateDepartment.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })

      // Delete Department
      .addCase(deleteDepartment.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(deleteDepartment.fulfilled, (state, action) => {
        state.isLoading = false;
        state.departments = state.departments.filter(department => department.id !== action.payload);
        state.totalCount -= 1;
        if (state.selectedDepartment?.id === action.payload) {
          state.selectedDepartment = null;
        }
      })
      .addCase(deleteDepartment.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      });
  }
});

export const { clearError, clearSelectedDepartment } = departmentSlice.actions;
export default departmentSlice.reducer;
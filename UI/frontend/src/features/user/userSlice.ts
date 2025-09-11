import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface UserProfile {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  addresses: Address[];
}

interface Address {
  id: string;
  type: 'HOME' | 'OFFICE' | 'OTHER';
  street: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  isDefault: boolean;
}

interface UserState {
  profile: UserProfile | null;
  isLoading: boolean;
  error: string | null;
}

const initialState: UserState = {
  profile: null,
  isLoading: false,
  error: null,
};

const userSlice = createSlice({
  name: 'user',
  initialState,
  reducers: {
    setProfile: (state, action: PayloadAction<UserProfile>) => {
      state.profile = action.payload;
    },
    updateProfile: (state, action: PayloadAction<Partial<UserProfile>>) => {
      if (state.profile) {
        state.profile = { ...state.profile, ...action.payload };
      }
    },
    addAddress: (state, action: PayloadAction<Address>) => {
      if (state.profile) {
        state.profile.addresses.push(action.payload);
      }
    },
    updateAddress: (state, action: PayloadAction<{ id: string; address: Partial<Address> }>) => {
      if (state.profile) {
        const index = state.profile.addresses.findIndex(addr => addr.id === action.payload.id);
        if (index !== -1) {
          state.profile.addresses[index] = { ...state.profile.addresses[index], ...action.payload.address };
        }
      }
    },
    removeAddress: (state, action: PayloadAction<string>) => {
      if (state.profile) {
        state.profile.addresses = state.profile.addresses.filter(addr => addr.id !== action.payload);
      }
    },
    clearError: (state) => {
      state.error = null;
    },
    clearProfile: (state) => {
      state.profile = null;
    }
  },
});

export const { 
  setProfile, 
  updateProfile, 
  addAddress, 
  updateAddress, 
  removeAddress, 
  clearError, 
  clearProfile 
} = userSlice.actions;
export default userSlice.reducer;
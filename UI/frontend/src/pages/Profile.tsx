import React, { useState } from 'react';
import {
  Container,
  Typography,
  Box,
  Paper,
  Grid,
  TextField,
  Button,
  Avatar,
  Divider,
  Tab,
  Tabs,
  Alert,
  CircularProgress,
} from '@mui/material';
import {
  Person,
  Email,
  LocationOn,
  Security,
  Save,
  Edit,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../app/hooks';
import { updateProfile } from '../features/user/userSlice';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => {
  return (
    <div hidden={value !== index}>
      {value === index && <Box sx={{ pt: 3 }}>{children}</Box>}
    </div>
  );
};

const Profile: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { user, isAuthenticated } = useAppSelector(state => state.auth);
  const { isLoading, error } = useAppSelector(state => state.user);
  
  const [tabValue, setTabValue] = useState(0);
  const [editMode, setEditMode] = useState(false);
  const [profileData, setProfileData] = useState({
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    email: user?.email || '',
    phone: '',
  });
  
  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  
  const [addressData, setAddressData] = useState({
    addressLine1: '',
    addressLine2: '',
    city: '',
    state: '',
    postalCode: '',
    country: 'United States',
  });

  if (!isAuthenticated) {
    navigate('/login');
    return null;
  }

  const handleProfileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setProfileData({
      ...profileData,
      [e.target.name]: e.target.value,
    });
  };

  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPasswordData({
      ...passwordData,
      [e.target.name]: e.target.value,
    });
  };

  const handleAddressChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setAddressData({
      ...addressData,
      [e.target.name]: e.target.value,
    });
  };

  const handleTabChange = (_: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
    setEditMode(false);
  };

  const handleSaveProfile = async () => {
    const result = await dispatch(updateProfile(profileData));
    if (updateProfile.fulfilled.match(result)) {
      setEditMode(false);
    }
  };

  const handleUpdatePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      alert('Passwords do not match');
      return;
    }
    // Password update logic would go here
    console.log('Update password:', passwordData);
  };

  const handleSaveAddress = async (e: React.FormEvent) => {
    e.preventDefault();
    // Address save logic would go here
    console.log('Save address:', addressData);
  };

  const getInitials = () => {
    if (user?.firstName && user?.lastName) {
      return `${user.firstName[0]}${user.lastName[0]}`.toUpperCase();
    }
    return user?.username?.[0]?.toUpperCase() || 'U';
  };

  return (
    <Container maxWidth="md">
      <Paper sx={{ mt: 4 }}>
        <Box sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
            <Avatar
              sx={{ 
                width: 80, 
                height: 80, 
                bgcolor: 'primary.main',
                fontSize: '2rem',
                mr: 3
              }}
            >
              {getInitials()}
            </Avatar>
            <Box>
              <Typography variant="h4">
                {user?.firstName} {user?.lastName}
              </Typography>
              <Typography variant="body1" color="text.secondary">
                @{user?.username}
              </Typography>
            </Box>
          </Box>

          <Tabs value={tabValue} onChange={handleTabChange}>
            <Tab icon={<Person />} label="Personal Info" />
            <Tab icon={<Security />} label="Security" />
            <Tab icon={<LocationOn />} label="Addresses" />
          </Tabs>
        </Box>
        
        <Divider />
        
        <Box sx={{ p: 3 }}>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}
          
          <TabPanel value={tabValue} index={0}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
              <Typography variant="h6">Personal Information</Typography>
              <Button
                startIcon={editMode ? <Save /> : <Edit />}
                onClick={editMode ? handleSaveProfile : () => setEditMode(true)}
                disabled={isLoading}
              >
                {editMode ? 'Save' : 'Edit'}
              </Button>
            </Box>
            
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="First Name"
                  name="firstName"
                  value={profileData.firstName}
                  onChange={handleProfileChange}
                  disabled={!editMode}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Last Name"
                  name="lastName"
                  value={profileData.lastName}
                  onChange={handleProfileChange}
                  disabled={!editMode}
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Email"
                  name="email"
                  type="email"
                  value={profileData.email}
                  onChange={handleProfileChange}
                  disabled={!editMode}
                  InputProps={{
                    startAdornment: <Email sx={{ mr: 1, color: 'text.secondary' }} />,
                  }}
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Phone Number"
                  name="phone"
                  value={profileData.phone}
                  onChange={handleProfileChange}
                  disabled={!editMode}
                />
              </Grid>
            </Grid>
          </TabPanel>
          
          <TabPanel value={tabValue} index={1}>
            <Typography variant="h6" gutterBottom>
              Change Password
            </Typography>
            
            <form onSubmit={handleUpdatePassword}>
              <Grid container spacing={2}>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    type="password"
                    label="Current Password"
                    name="currentPassword"
                    value={passwordData.currentPassword}
                    onChange={handlePasswordChange}
                    required
                  />
                </Grid>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    type="password"
                    label="New Password"
                    name="newPassword"
                    value={passwordData.newPassword}
                    onChange={handlePasswordChange}
                    required
                  />
                </Grid>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    type="password"
                    label="Confirm New Password"
                    name="confirmPassword"
                    value={passwordData.confirmPassword}
                    onChange={handlePasswordChange}
                    required
                  />
                </Grid>
                <Grid item xs={12}>
                  <Button
                    type="submit"
                    variant="contained"
                    disabled={isLoading}
                  >
                    Update Password
                  </Button>
                </Grid>
              </Grid>
            </form>
            
            <Box sx={{ mt: 4 }}>
              <Typography variant="h6" gutterBottom>
                Security Settings
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Last login: {new Date().toLocaleString()}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Account created: {user?.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'N/A'}
              </Typography>
            </Box>
          </TabPanel>
          
          <TabPanel value={tabValue} index={2}>
            <Typography variant="h6" gutterBottom>
              Saved Addresses
            </Typography>
            
            <form onSubmit={handleSaveAddress}>
              <Grid container spacing={2}>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Address Line 1"
                    name="addressLine1"
                    value={addressData.addressLine1}
                    onChange={handleAddressChange}
                    required
                  />
                </Grid>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Address Line 2 (Optional)"
                    name="addressLine2"
                    value={addressData.addressLine2}
                    onChange={handleAddressChange}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="City"
                    name="city"
                    value={addressData.city}
                    onChange={handleAddressChange}
                    required
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="State"
                    name="state"
                    value={addressData.state}
                    onChange={handleAddressChange}
                    required
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Postal Code"
                    name="postalCode"
                    value={addressData.postalCode}
                    onChange={handleAddressChange}
                    required
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Country"
                    name="country"
                    value={addressData.country}
                    onChange={handleAddressChange}
                    disabled
                  />
                </Grid>
                <Grid item xs={12}>
                  <Button
                    type="submit"
                    variant="contained"
                    disabled={isLoading}
                  >
                    Save Address
                  </Button>
                </Grid>
              </Grid>
            </form>
          </TabPanel>
        </Box>
      </Paper>
    </Container>
  );
};

export default Profile;
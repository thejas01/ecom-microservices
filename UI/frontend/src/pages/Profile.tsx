import React from 'react';
import { Container, Typography } from '@mui/material';

const Profile: React.FC = () => {
  return (
    <Container>
      <Typography variant="h4" component="h1" gutterBottom>
        Profile
      </Typography>
      <Typography variant="body1">
        User profile management will be implemented here.
      </Typography>
    </Container>
  );
};

export default Profile;
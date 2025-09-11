import React from 'react';
import { Container, Typography } from '@mui/material';

const Checkout: React.FC = () => {
  return (
    <Container>
      <Typography variant="h4" component="h1" gutterBottom>
        Checkout
      </Typography>
      <Typography variant="body1">
        Checkout process will be implemented here.
      </Typography>
    </Container>
  );
};

export default Checkout;
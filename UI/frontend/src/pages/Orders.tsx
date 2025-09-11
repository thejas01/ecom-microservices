import React from 'react';
import { Container, Typography } from '@mui/material';

const Orders: React.FC = () => {
  return (
    <Container>
      <Typography variant="h4" component="h1" gutterBottom>
        My Orders
      </Typography>
      <Typography variant="body1">
        Order history will be implemented here.
      </Typography>
    </Container>
  );
};

export default Orders;
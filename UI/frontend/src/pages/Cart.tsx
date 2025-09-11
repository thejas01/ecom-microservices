import React from 'react';
import { Container, Typography } from '@mui/material';

const Cart: React.FC = () => {
  return (
    <Container>
      <Typography variant="h4" component="h1" gutterBottom>
        Shopping Cart
      </Typography>
      <Typography variant="body1">
        Shopping cart will be implemented here.
      </Typography>
    </Container>
  );
};

export default Cart;
import React, { useEffect } from 'react';
import {
  Container,
  Typography,
  Box,
  Paper,
  Grid,
  Card,
  CardContent,
  Chip,
  Button,
  CircularProgress,
  Alert,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Divider,
  CardMedia,
} from '@mui/material';
import {
  ExpandMore,
  LocalShipping,
  CheckCircle,
  Cancel,
  Pending,
  ShoppingBag,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../app/hooks';
import { fetchUserOrders } from '../features/orders/ordersSlice';
import { format } from 'date-fns';

const getStatusIcon = (status: string) => {
  switch (status.toUpperCase()) {
    case 'PENDING':
      return <Pending color="warning" />;
    case 'PROCESSING':
      return <LocalShipping color="info" />;
    case 'DELIVERED':
      return <CheckCircle color="success" />;
    case 'CANCELLED':
      return <Cancel color="error" />;
    default:
      return <Pending />;
  }
};

const getStatusColor = (status: string): "default" | "primary" | "secondary" | "error" | "info" | "success" | "warning" => {
  switch (status.toUpperCase()) {
    case 'PENDING':
      return 'warning';
    case 'PROCESSING':
      return 'info';
    case 'DELIVERED':
      return 'success';
    case 'CANCELLED':
      return 'error';
    default:
      return 'default';
  }
};

const Orders: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  
  const { orders, isLoading, error } = useAppSelector(state => state.orders);
  const { isAuthenticated } = useAppSelector(state => state.auth);

  useEffect(() => {
    if (isAuthenticated) {
      dispatch(fetchUserOrders());
    }
  }, [dispatch, isAuthenticated]);

  if (!isAuthenticated) {
    return (
      <Container>
        <Box sx={{ textAlign: 'center', py: 8 }}>
          <ShoppingBag sx={{ fontSize: 60, color: 'text.secondary', mb: 2 }} />
          <Typography variant="h5" gutterBottom>
            Please log in to view your orders
          </Typography>
          <Button
            variant="contained"
            onClick={() => navigate('/login')}
            sx={{ mt: 2 }}
          >
            Login
          </Button>
        </Box>
      </Container>
    );
  }

  if (isLoading) {
    return (
      <Container>
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  if (error) {
    return (
      <Container>
        <Alert severity="error" sx={{ mt: 2 }}>
          {error}
        </Alert>
      </Container>
    );
  }

  if (!orders || orders.length === 0) {
    return (
      <Container>
        <Box sx={{ textAlign: 'center', py: 8 }}>
          <ShoppingBag sx={{ fontSize: 60, color: 'text.secondary', mb: 2 }} />
          <Typography variant="h5" gutterBottom>
            No orders yet
          </Typography>
          <Typography variant="body1" color="text.secondary" gutterBottom>
            Start shopping to see your orders here
          </Typography>
          <Button
            variant="contained"
            onClick={() => navigate('/products')}
            sx={{ mt: 2 }}
          >
            Start Shopping
          </Button>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Typography variant="h4" component="h1" gutterBottom sx={{ mb: 4 }}>
        My Orders
      </Typography>

      {orders.map((order) => (
        <Accordion key={order.id} defaultExpanded={false} sx={{ mb: 2 }}>
          <AccordionSummary expandIcon={<ExpandMore />}>
            <Grid container alignItems="center" spacing={2}>
              <Grid item xs={12} sm={3}>
                <Typography variant="subtitle1">
                  Order #{order.id.slice(-8).toUpperCase()}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {format(new Date(order.createdAt), 'MMM dd, yyyy')}
                </Typography>
              </Grid>
              
              <Grid item xs={12} sm={3}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  {getStatusIcon(order.status)}
                  <Chip
                    label={order.status}
                    color={getStatusColor(order.status)}
                    size="small"
                  />
                </Box>
              </Grid>
              
              <Grid item xs={12} sm={3}>
                <Typography variant="body2" color="text.secondary">
                  Items: {order.items.reduce((sum, item) => sum + item.quantity, 0)}
                </Typography>
              </Grid>
              
              <Grid item xs={12} sm={3}>
                <Typography variant="h6">
                  ${order.totalAmount.toFixed(2)}
                </Typography>
              </Grid>
            </Grid>
          </AccordionSummary>
          
          <AccordionDetails>
            <Box>
              <Grid container spacing={3}>
                <Grid item xs={12} md={8}>
                  <Paper sx={{ p: 2 }}>
                    <Typography variant="h6" gutterBottom>
                      Order Items
                    </Typography>
                    {order.items.map((item) => (
                      <Box key={item.id}>
                        <Box sx={{ display: 'flex', py: 2 }}>
                          <CardMedia
                            component="img"
                            sx={{ width: 80, height: 80, objectFit: 'cover', mr: 2 }}
                            image={item.product.imageUrl || '/api/placeholder/80/80'}
                            alt={item.product.name}
                          />
                          <Box sx={{ flexGrow: 1 }}>
                            <Typography variant="subtitle1">
                              {item.product.name}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              Quantity: {item.quantity} × ${item.price.toFixed(2)}
                            </Typography>
                          </Box>
                          <Typography variant="subtitle1">
                            ${(item.quantity * item.price).toFixed(2)}
                          </Typography>
                        </Box>
                        <Divider />
                      </Box>
                    ))}
                  </Paper>
                </Grid>
                
                <Grid item xs={12} md={4}>
                  <Paper sx={{ p: 2, mb: 2 }}>
                    <Typography variant="h6" gutterBottom>
                      Shipping Address
                    </Typography>
                    <Typography variant="body2">
                      {order.shippingAddress.firstName} {order.shippingAddress.lastName}<br />
                      {order.shippingAddress.addressLine1}<br />
                      {order.shippingAddress.addressLine2 && <>{order.shippingAddress.addressLine2}<br /></>}
                      {order.shippingAddress.city}, {order.shippingAddress.state} {order.shippingAddress.postalCode}<br />
                      {order.shippingAddress.country}
                    </Typography>
                  </Paper>
                  
                  <Paper sx={{ p: 2 }}>
                    <Typography variant="h6" gutterBottom>
                      Order Summary
                    </Typography>
                    <Box>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                        <Typography variant="body2">Subtotal</Typography>
                        <Typography variant="body2">
                          ${(order.totalAmount / 1.08).toFixed(2)}
                        </Typography>
                      </Box>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                        <Typography variant="body2">Tax</Typography>
                        <Typography variant="body2">
                          ${(order.totalAmount - order.totalAmount / 1.08).toFixed(2)}
                        </Typography>
                      </Box>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                        <Typography variant="body2">Shipping</Typography>
                        <Typography variant="body2">Free</Typography>
                      </Box>
                      <Divider sx={{ my: 1 }} />
                      <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                        <Typography variant="subtitle1">Total</Typography>
                        <Typography variant="subtitle1">
                          ${order.totalAmount.toFixed(2)}
                        </Typography>
                      </Box>
                    </Box>
                  </Paper>
                </Grid>
              </Grid>
              
              <Box sx={{ mt: 2, display: 'flex', gap: 2 }}>
                <Button
                  variant="outlined"
                  onClick={() => navigate(`/orders/${order.id}`)}
                >
                  View Details
                </Button>
                {order.status === 'DELIVERED' && (
                  <Button
                    variant="contained"
                    onClick={() => navigate('/products')}
                  >
                    Buy Again
                  </Button>
                )}
              </Box>
            </Box>
          </AccordionDetails>
        </Accordion>
      ))}
    </Container>
  );
};

export default Orders;
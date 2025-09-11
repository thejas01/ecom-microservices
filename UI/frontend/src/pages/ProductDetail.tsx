import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Grid,
  Typography,
  Button,
  Box,
  Paper,
  Chip,
  Divider,
  TextField,
  CircularProgress,
  Alert,
  IconButton,
  Breadcrumbs,
  Link,
  Card,
  CardMedia,
  Tabs,
  Tab,
} from '@mui/material';
import {
  AddShoppingCart,
  Remove,
  Add,
  ArrowBack,
  LocalShipping,
  Security,
  Assignment,
} from '@mui/icons-material';
import { useAppDispatch, useAppSelector } from '../app/hooks';
import { fetchProductById, clearCurrentProduct } from '../features/products/productsSlice';
import { addToCart } from '../features/cart/cartSlice';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index, ...other }) => {
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`product-tabpanel-${index}`}
      aria-labelledby={`product-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
};

const ProductDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const [quantity, setQuantity] = useState(1);
  const [tabValue, setTabValue] = useState(0);
  const [imageError, setImageError] = useState(false);

  const { currentProduct, isLoading, error } = useAppSelector(state => state.products);
  const { isAuthenticated } = useAppSelector(state => state.auth);

  useEffect(() => {
    if (id) {
      dispatch(fetchProductById(id));
    }
    return () => {
      dispatch(clearCurrentProduct());
    };
  }, [dispatch, id]);

  const handleQuantityChange = (change: number) => {
    const newQuantity = quantity + change;
    if (newQuantity >= 1 && newQuantity <= 99) {
      setQuantity(newQuantity);
    }
  };

  const handleAddToCart = async () => {
    if (!currentProduct) return;
    
    try {
      await dispatch(addToCart({ 
        productId: currentProduct.id, 
        quantity 
      })).unwrap();
      // Optionally show success message
    } catch (err) {
      console.error('Failed to add to cart:', err);
    }
  };

  const handleTabChange = (_: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  if (isLoading) {
    return (
      <Container>
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  if (error || !currentProduct) {
    return (
      <Container>
        <Alert 
          severity="error" 
          sx={{ mt: 2 }}
          action={
            <Button color="inherit" onClick={() => navigate('/products')}>
              Back to Products
            </Button>
          }
        >
          {error || 'Product not found'}
        </Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ mb: 3 }}>
        <Button
          startIcon={<ArrowBack />}
          onClick={() => navigate('/products')}
          sx={{ mb: 2 }}
        >
          Back to Products
        </Button>
        
        <Breadcrumbs aria-label="breadcrumb">
          <Link
            underline="hover"
            color="inherit"
            href="/products"
            onClick={(e) => {
              e.preventDefault();
              navigate('/products');
            }}
          >
            Products
          </Link>
          <Link
            underline="hover"
            color="inherit"
            href="#"
            onClick={(e) => {
              e.preventDefault();
              navigate(`/products?category=${currentProduct.categoryId}`);
            }}
          >
            {currentProduct.categoryName}
          </Link>
          <Typography color="text.primary">{currentProduct.name}</Typography>
        </Breadcrumbs>
      </Box>

      <Grid container spacing={4}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardMedia
              component="img"
              image={!imageError ? (currentProduct.imageUrl || '/api/placeholder/600/600') : '/api/placeholder/600/600'}
              alt={currentProduct.name}
              onError={() => setImageError(true)}
              sx={{
                height: 500,
                objectFit: 'contain',
                backgroundColor: 'grey.100',
              }}
            />
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Box>
            <Typography variant="h4" component="h1" gutterBottom>
              {currentProduct.name}
            </Typography>
            
            <Chip 
              label={currentProduct.categoryName} 
              color="primary" 
              sx={{ mb: 2 }}
            />

            <Typography variant="h3" color="primary" gutterBottom>
              ${currentProduct.price.toFixed(2)}
            </Typography>

            <Divider sx={{ my: 2 }} />

            <Typography variant="body1" paragraph>
              {currentProduct.description}
            </Typography>

            <Box sx={{ my: 3 }}>
              <Typography variant="subtitle1" gutterBottom>
                Quantity
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Paper sx={{ display: 'flex', alignItems: 'center' }} variant="outlined">
                  <IconButton 
                    onClick={() => handleQuantityChange(-1)}
                    disabled={quantity <= 1}
                  >
                    <Remove />
                  </IconButton>
                  <TextField
                    value={quantity}
                    onChange={(e) => {
                      const val = parseInt(e.target.value);
                      if (!isNaN(val) && val >= 1 && val <= 99) {
                        setQuantity(val);
                      }
                    }}
                    sx={{ 
                      width: 60, 
                      '& .MuiOutlinedInput-notchedOutline': { border: 'none' },
                      '& input': { textAlign: 'center' }
                    }}
                  />
                  <IconButton 
                    onClick={() => handleQuantityChange(1)}
                    disabled={quantity >= 99}
                  >
                    <Add />
                  </IconButton>
                </Paper>
                
                <Button
                  variant="contained"
                  size="large"
                  startIcon={<AddShoppingCart />}
                  onClick={handleAddToCart}
                  disabled={!isAuthenticated}
                  fullWidth
                >
                  Add to Cart
                </Button>
              </Box>
              {!isAuthenticated && (
                <Alert severity="info" sx={{ mt: 2 }}>
                  Please <Link href="/login" onClick={(e) => {
                    e.preventDefault();
                    navigate('/login');
                  }}>login</Link> to add items to cart
                </Alert>
              )}
            </Box>

            <Box sx={{ display: 'flex', gap: 3, my: 3 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <LocalShipping color="action" />
                <Typography variant="body2">Free shipping on orders over $50</Typography>
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Security color="action" />
                <Typography variant="body2">Secure payment</Typography>
              </Box>
            </Box>
          </Box>
        </Grid>
      </Grid>

      <Box sx={{ mt: 4 }}>
        <Tabs value={tabValue} onChange={handleTabChange} aria-label="product details tabs">
          <Tab label="Description" />
          <Tab label="Specifications" />
          <Tab label="Shipping & Returns" />
        </Tabs>
        
        <TabPanel value={tabValue} index={0}>
          <Typography variant="h6" gutterBottom>
            Product Description
          </Typography>
          <Typography variant="body1" paragraph>
            {currentProduct.description}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Product ID: {currentProduct.id}
          </Typography>
        </TabPanel>
        
        <TabPanel value={tabValue} index={1}>
          <Typography variant="h6" gutterBottom>
            Product Specifications
          </Typography>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
              <Typography variant="body2" color="text.secondary">Category:</Typography>
              <Typography variant="body2">{currentProduct.categoryName}</Typography>
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
              <Typography variant="body2" color="text.secondary">Price:</Typography>
              <Typography variant="body2">${currentProduct.price}</Typography>
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
              <Typography variant="body2" color="text.secondary">Added:</Typography>
              <Typography variant="body2">
                {new Date(currentProduct.createdAt).toLocaleDateString()}
              </Typography>
            </Box>
          </Box>
        </TabPanel>
        
        <TabPanel value={tabValue} index={2}>
          <Typography variant="h6" gutterBottom>
            Shipping Information
          </Typography>
          <Typography variant="body1" paragraph>
            We offer free standard shipping on all orders over $50. Orders are processed within 1-2 business days.
          </Typography>
          <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
            Return Policy
          </Typography>
          <Typography variant="body1">
            We accept returns within 30 days of purchase. Items must be unused and in original packaging.
          </Typography>
        </TabPanel>
      </Box>
    </Container>
  );
};

export default ProductDetail;
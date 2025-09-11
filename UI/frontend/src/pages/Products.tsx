import React, { useEffect, useState } from 'react';
import {
  Container,
  Grid,
  Card,
  CardMedia,
  CardContent,
  CardActions,
  Typography,
  Button,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Box,
  Pagination,
  CircularProgress,
  Alert,
  Chip,
  InputAdornment,
  IconButton,
  Slider,
  Paper,
  Drawer,
  useTheme,
  useMediaQuery,
} from '@mui/material';
import { Search, FilterList, AddShoppingCart, Clear } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../app/hooks';
import { 
  fetchProducts, 
  fetchCategories, 
  setSearchQuery, 
  setSelectedCategory,
  clearError 
} from '../features/products/productsSlice';
import { addToCart } from '../features/cart/cartSlice';
import type { ProductSearchParams } from '../types';

const Products: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  
  const {
    products,
    categories,
    totalPages,
    currentPage,
    isLoading,
    error,
    searchQuery,
    selectedCategory
  } = useAppSelector(state => state.products);
  
  const [localSearch, setLocalSearch] = useState(searchQuery);
  const [priceRange, setPriceRange] = useState<number[]>([0, 1000]);
  const [sortBy, setSortBy] = useState('name');
  const [filterDrawerOpen, setFilterDrawerOpen] = useState(false);

  useEffect(() => {
    dispatch(fetchCategories());
  }, [dispatch]);

  useEffect(() => {
    const params: ProductSearchParams = {
      page: currentPage,
      size: 12,
      sort: sortBy,
      ...(searchQuery && { search: searchQuery }),
      ...(selectedCategory && { category: selectedCategory }),
      minPrice: priceRange[0],
      maxPrice: priceRange[1],
    };
    dispatch(fetchProducts(params));
  }, [dispatch, currentPage, searchQuery, selectedCategory, sortBy, priceRange]);

  const handleSearch = () => {
    dispatch(setSearchQuery(localSearch));
  };

  const handleClearSearch = () => {
    setLocalSearch('');
    dispatch(setSearchQuery(''));
  };

  const handleCategoryChange = (category: string) => {
    dispatch(setSelectedCategory(category));
  };

  const handlePageChange = (_: React.ChangeEvent<unknown>, page: number) => {
    dispatch(fetchProducts({ 
      page: page - 1, 
      size: 12,
      sort: sortBy,
      ...(searchQuery && { search: searchQuery }),
      ...(selectedCategory && { category: selectedCategory }),
      minPrice: priceRange[0],
      maxPrice: priceRange[1],
    }));
  };

  const handleAddToCart = async (productId: string) => {
    try {
      await dispatch(addToCart({ productId, quantity: 1 })).unwrap();
    } catch (err) {
      console.error('Failed to add to cart:', err);
    }
  };

  const handleProductClick = (productId: string) => {
    navigate(`/products/${productId}`);
  };

  const filters = (
    <Box sx={{ p: 2 }}>
      <Typography variant="h6" gutterBottom>
        Filters
      </Typography>
      
      <FormControl fullWidth sx={{ mb: 3 }}>
        <InputLabel>Category</InputLabel>
        <Select
          value={selectedCategory || ''}
          label="Category"
          onChange={(e) => handleCategoryChange(e.target.value)}
        >
          <MenuItem value="">All Categories</MenuItem>
          {categories.map(category => (
            <MenuItem key={category.id} value={category.id}>
              {category.name}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      <Box sx={{ mb: 3 }}>
        <Typography gutterBottom>Price Range</Typography>
        <Slider
          value={priceRange}
          onChange={(_, newValue) => setPriceRange(newValue as number[])}
          valueLabelDisplay="auto"
          min={0}
          max={1000}
          step={10}
        />
        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
          <Typography variant="caption">${priceRange[0]}</Typography>
          <Typography variant="caption">${priceRange[1]}</Typography>
        </Box>
      </Box>

      <FormControl fullWidth>
        <InputLabel>Sort By</InputLabel>
        <Select
          value={sortBy}
          label="Sort By"
          onChange={(e) => setSortBy(e.target.value)}
        >
          <MenuItem value="name">Name (A-Z)</MenuItem>
          <MenuItem value="name,desc">Name (Z-A)</MenuItem>
          <MenuItem value="price">Price (Low to High)</MenuItem>
          <MenuItem value="price,desc">Price (High to Low)</MenuItem>
          <MenuItem value="createdAt,desc">Newest First</MenuItem>
        </Select>
      </FormControl>
    </Box>
  );

  if (error) {
    return (
      <Container>
        <Alert 
          severity="error" 
          onClose={() => dispatch(clearError())}
          sx={{ mt: 2 }}
        >
          {error}
        </Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Products
        </Typography>
        
        <Paper sx={{ p: 2, mb: 3 }}>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} md={8}>
              <TextField
                fullWidth
                placeholder="Search products..."
                value={localSearch}
                onChange={(e) => setLocalSearch(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <Search />
                    </InputAdornment>
                  ),
                  endAdornment: localSearch && (
                    <InputAdornment position="end">
                      <IconButton onClick={handleClearSearch} size="small">
                        <Clear />
                      </IconButton>
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <Box sx={{ display: 'flex', gap: 1 }}>
                <Button
                  variant="contained"
                  onClick={handleSearch}
                  fullWidth
                >
                  Search
                </Button>
                {isMobile && (
                  <Button
                    variant="outlined"
                    onClick={() => setFilterDrawerOpen(true)}
                    startIcon={<FilterList />}
                  >
                    Filters
                  </Button>
                )}
              </Box>
            </Grid>
          </Grid>
        </Paper>

        {(searchQuery || selectedCategory) && (
          <Box sx={{ mb: 2 }}>
            {searchQuery && (
              <Chip
                label={`Search: ${searchQuery}`}
                onDelete={() => dispatch(setSearchQuery(''))}
                sx={{ mr: 1 }}
              />
            )}
            {selectedCategory && (
              <Chip
                label={`Category: ${categories.find(c => c.id === selectedCategory)?.name}`}
                onDelete={() => dispatch(setSelectedCategory(null))}
              />
            )}
          </Box>
        )}
      </Box>

      <Grid container spacing={3}>
        {!isMobile && (
          <Grid item xs={12} md={3}>
            <Paper elevation={1}>
              {filters}
            </Paper>
          </Grid>
        )}
        
        <Grid item xs={12} md={9}>
          {isLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
              <CircularProgress />
            </Box>
          ) : products.length === 0 ? (
            <Alert severity="info">
              No products found. Try adjusting your filters.
            </Alert>
          ) : (
            <>
              <Grid container spacing={2}>
                {products.map(product => (
                  <Grid key={product.id} item xs={12} sm={6} lg={4}>
                    <Card 
                      sx={{ 
                        height: '100%', 
                        display: 'flex', 
                        flexDirection: 'column',
                        cursor: 'pointer',
                        '&:hover': {
                          boxShadow: 4,
                        }
                      }}
                    >
                      <CardMedia
                        component="img"
                        height="200"
                        image={product.imageUrl || '/api/placeholder/200/200'}
                        alt={product.name}
                        onClick={() => handleProductClick(product.id)}
                      />
                      <CardContent sx={{ flexGrow: 1 }}>
                        <Typography 
                          gutterBottom 
                          variant="h6" 
                          component="div"
                          onClick={() => handleProductClick(product.id)}
                        >
                          {product.name}
                        </Typography>
                        <Typography 
                          variant="body2" 
                          color="text.secondary"
                          sx={{ mb: 1 }}
                        >
                          {product.description}
                        </Typography>
                        <Chip 
                          label={product.categoryName} 
                          size="small" 
                          sx={{ mb: 1 }}
                        />
                        <Typography variant="h6" color="primary">
                          ${product.price.toFixed(2)}
                        </Typography>
                      </CardContent>
                      <CardActions>
                        <Button 
                          size="small" 
                          variant="contained"
                          startIcon={<AddShoppingCart />}
                          onClick={() => handleAddToCart(product.id)}
                          fullWidth
                        >
                          Add to Cart
                        </Button>
                      </CardActions>
                    </Card>
                  </Grid>
                ))}
              </Grid>

              {totalPages > 1 && (
                <Box sx={{ mt: 4, display: 'flex', justifyContent: 'center' }}>
                  <Pagination
                    count={totalPages}
                    page={currentPage + 1}
                    onChange={handlePageChange}
                    color="primary"
                    size={isMobile ? 'small' : 'medium'}
                  />
                </Box>
              )}
            </>
          )}
        </Grid>
      </Grid>

      <Drawer
        anchor="left"
        open={filterDrawerOpen}
        onClose={() => setFilterDrawerOpen(false)}
      >
        <Box sx={{ width: 280 }}>
          {filters}
        </Box>
      </Drawer>
    </Container>
  );
};

export default Products;
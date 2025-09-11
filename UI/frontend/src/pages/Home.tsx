import React from 'react';
import { Container, Typography, Box, Paper } from '@mui/material';
import { useNavigate } from 'react-router-dom';

const Home: React.FC = () => {
  const navigate = useNavigate();

  const handleProductsClick = () => {
    console.log('Navigating to products...');
    navigate('/products');
  };

  const handleRegisterClick = () => {
    console.log('Navigating to register...');
    navigate('/register');
  };

  return (
    <Container maxWidth="lg">
      <Box sx={{ textAlign: 'center', py: 8 }}>
        <Typography variant="h2" component="h1" gutterBottom>
          Welcome to E-Commerce Store
        </Typography>
        <Typography variant="h5" component="h2" gutterBottom color="text.secondary">
          Discover amazing products at great prices
        </Typography>
        
        {/* Simple HTML buttons as fallback */}
        <Box sx={{ mt: 4 }}>
          <button 
            onClick={handleProductsClick}
            style={{
              backgroundColor: '#1976d2',
              color: 'white',
              padding: '12px 32px',
              fontSize: '16px',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              marginRight: '16px'
            }}
          >
            Shop Now
          </button>
          
          <button 
            onClick={handleRegisterClick}
            style={{
              backgroundColor: 'transparent',
              color: '#1976d2',
              padding: '12px 32px',
              fontSize: '16px',
              border: '2px solid #1976d2',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Create Account
          </button>
        </Box>
      </Box>
      
      <Paper sx={{ mt: 4, p: 3, backgroundColor: '#f5f5f5' }}>
        <Typography variant="h6" gutterBottom>
          Quick Navigation
        </Typography>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, mt: 2 }}>
          <Typography>
            • Click "Products" in the header to browse items
          </Typography>
          <Typography>
            • Or use these links:
          </Typography>
          <Box sx={{ display: 'flex', gap: 3, ml: 2, mt: 1 }}>
            <a 
              href="#" 
              onClick={(e) => { e.preventDefault(); handleProductsClick(); }}
              style={{ color: '#1976d2', textDecoration: 'underline' }}
            >
              Browse Products
            </a>
            <a 
              href="#" 
              onClick={(e) => { e.preventDefault(); handleRegisterClick(); }}
              style={{ color: '#1976d2', textDecoration: 'underline' }}
            >
              Register
            </a>
            <a 
              href="#" 
              onClick={(e) => { e.preventDefault(); navigate('/login'); }}
              style={{ color: '#1976d2', textDecoration: 'underline' }}
            >
              Login
            </a>
          </Box>
        </Box>
        
        <Box sx={{ mt: 3, p: 2, backgroundColor: '#e3f2fd', borderRadius: 1 }}>
          <Typography variant="body2" color="text.secondary">
            <strong>Note:</strong> Make sure your backend services are running on port 8080 for full functionality.
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            Frontend is running on: {window.location.href}
          </Typography>
        </Box>
      </Paper>
    </Container>
  );
};

export default Home;
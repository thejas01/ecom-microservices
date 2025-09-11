import React from 'react';
import { Box, Typography, Container, Grid2 as Grid, Link, IconButton } from '@mui/material';
import { Facebook, Twitter, Instagram, Email } from '@mui/icons-material';

const Footer: React.FC = () => {
  return (
    <Box 
      component="footer" 
      sx={{ 
        bgcolor: 'primary.main', 
        color: 'white', 
        py: 4, 
        mt: 'auto' 
      }}
    >
      <Container maxWidth="lg">
        <Grid container spacing={4}>
          <Grid size={4}>
            <Typography variant="h6" gutterBottom>
              About Us
            </Typography>
            <Typography variant="body2">
              Your one-stop shop for all your needs. Quality products, great prices, and excellent customer service.
            </Typography>
          </Grid>
          
          <Grid size={4}>
            <Typography variant="h6" gutterBottom>
              Quick Links
            </Typography>
            <Box>
              <Link href="/products" color="inherit" sx={{ display: 'block', mb: 1 }}>
                Products
              </Link>
              <Link href="/cart" color="inherit" sx={{ display: 'block', mb: 1 }}>
                Shopping Cart
              </Link>
              <Link href="/orders" color="inherit" sx={{ display: 'block', mb: 1 }}>
                Order History
              </Link>
              <Link href="/contact" color="inherit" sx={{ display: 'block' }}>
                Contact Us
              </Link>
            </Box>
          </Grid>
          
          <Grid size={4}>
            <Typography variant="h6" gutterBottom>
              Connect With Us
            </Typography>
            <Box>
              <IconButton color="inherit" aria-label="Facebook">
                <Facebook />
              </IconButton>
              <IconButton color="inherit" aria-label="Twitter">
                <Twitter />
              </IconButton>
              <IconButton color="inherit" aria-label="Instagram">
                <Instagram />
              </IconButton>
              <IconButton color="inherit" aria-label="Email">
                <Email />
              </IconButton>
            </Box>
          </Grid>
        </Grid>
        
        <Box mt={4} pt={2} sx={{ borderTop: '1px solid rgba(255, 255, 255, 0.1)' }}>
          <Typography variant="body2" align="center">
            © {new Date().getFullYear()} E-Commerce Store. All rights reserved.
          </Typography>
        </Box>
      </Container>
    </Box>
  );
};

export default Footer;
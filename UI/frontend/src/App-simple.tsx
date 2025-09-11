import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Container, Typography, AppBar, Toolbar } from '@mui/material';

function Home() {
  return (
    <Container>
      <Typography variant="h2" component="h1" gutterBottom>
        Welcome to E-Commerce Store
      </Typography>
      <Typography variant="h5" component="h2" gutterBottom color="text.secondary">
        The app is working!
      </Typography>
    </Container>
  );
}

function App() {
  return (
    <Router>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6">
            E-Commerce Store
          </Typography>
        </Toolbar>
      </AppBar>
      <Container sx={{ mt: 4 }}>
        <Routes>
          <Route path="/" element={<Home />} />
        </Routes>
      </Container>
    </Router>
  );
}

export default App;
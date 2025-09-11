import React, { useState } from 'react';
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  Box,
  Badge,
  IconButton,
  Menu,
  MenuItem,
  useTheme,
  useMediaQuery,
  Drawer,
  List,
  ListItemButton,
  ListItemText,
  Divider,
} from '@mui/material';
import { ShoppingCart, Person, Menu as MenuIcon, AdminPanelSettings } from '@mui/icons-material';
import { Link, useNavigate } from 'react-router-dom';
import { useAppSelector, useAppDispatch } from '../../app/hooks';
import { logout } from '../../features/auth/authSlice';
import { ROUTES } from '../../utils/constants';

const Header: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  
  const { isAuthenticated, user } = useAppSelector(state => state.auth);
  const { cart } = useAppSelector(state => state.cart);
  const isAdmin = user?.roles?.includes('ROLE_ADMIN');

  const handleLogout = async () => {
    await dispatch(logout());
    navigate(ROUTES.HOME);
  };

  const handleProfileMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleProfileMenuClose = () => {
    setAnchorEl(null);
  };

  const handleMobileMenuToggle = () => {
    setMobileMenuOpen(!mobileMenuOpen);
  };

  const profileMenu = (
    <Menu
      anchorEl={anchorEl}
      open={Boolean(anchorEl)}
      onClose={handleProfileMenuClose}
    >
      <MenuItem onClick={() => {
        handleProfileMenuClose();
        navigate(ROUTES.PROFILE);
      }}>
        Profile
      </MenuItem>
      <MenuItem onClick={() => {
        handleProfileMenuClose();
        navigate(ROUTES.ORDERS);
      }}>
        My Orders
      </MenuItem>
      {isAdmin && (
        <MenuItem onClick={() => {
          handleProfileMenuClose();
          navigate('/admin');
        }}>
          Admin Panel
        </MenuItem>
      )}
      <Divider />
      <MenuItem onClick={() => {
        handleProfileMenuClose();
        handleLogout();
      }}>
        Logout
      </MenuItem>
    </Menu>
  );

  const mobileDrawer = (
    <Drawer
      anchor="right"
      open={mobileMenuOpen}
      onClose={handleMobileMenuToggle}
    >
      <Box sx={{ width: 250 }}>
        <List>
          <ListItemButton component={Link} to={ROUTES.PRODUCTS} onClick={handleMobileMenuToggle}>
            <ListItemText primary="Products" />
          </ListItemButton>
          <ListItemButton component={Link} to={ROUTES.CART} onClick={handleMobileMenuToggle}>
            <ListItemText primary={`Cart (${cart?.totalItems || 0})`} />
          </ListItemButton>
          {isAuthenticated ? (
            <>
              <ListItemButton component={Link} to={ROUTES.ORDERS} onClick={handleMobileMenuToggle}>
                <ListItemText primary="Orders" />
              </ListItemButton>
              <ListItemButton component={Link} to={ROUTES.PROFILE} onClick={handleMobileMenuToggle}>
                <ListItemText primary="Profile" />
              </ListItemButton>
              {isAdmin && (
                <ListItemButton component={Link} to="/admin" onClick={handleMobileMenuToggle}>
                  <ListItemText primary="Admin Panel" />
                </ListItemButton>
              )}
              <Divider />
              <ListItemButton onClick={() => {
                handleMobileMenuToggle();
                handleLogout();
              }}>
                <ListItemText primary="Logout" />
              </ListItemButton>
            </>
          ) : (
            <>
              <ListItemButton component={Link} to={ROUTES.LOGIN} onClick={handleMobileMenuToggle}>
                <ListItemText primary="Login" />
              </ListItemButton>
              <ListItemButton component={Link} to={ROUTES.REGISTER} onClick={handleMobileMenuToggle}>
                <ListItemText primary="Register" />
              </ListItemButton>
            </>
          )}
        </List>
      </Box>
    </Drawer>
  );

  return (
    <AppBar position="sticky">
      <Toolbar>
        <Typography 
          variant="h6" 
          component={Link} 
          to={ROUTES.HOME}
          sx={{ 
            flexGrow: 1, 
            textDecoration: 'none', 
            color: 'inherit' 
          }}
        >
          E-Commerce Store
        </Typography>

        {isMobile ? (
          <>
            <IconButton
              color="inherit"
              component={Link}
              to={ROUTES.CART}
            >
              <Badge badgeContent={cart?.totalItems || 0} color="secondary">
                <ShoppingCart />
              </Badge>
            </IconButton>
            <IconButton
              color="inherit"
              onClick={handleMobileMenuToggle}
              edge="end"
            >
              <MenuIcon />
            </IconButton>
          </>
        ) : (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Button
              color="inherit"
              component={Link}
              to={ROUTES.PRODUCTS}
            >
              Products
            </Button>

            <IconButton
              color="inherit"
              component={Link}
              to={ROUTES.CART}
            >
              <Badge badgeContent={cart?.totalItems || 0} color="secondary">
                <ShoppingCart />
              </Badge>
            </IconButton>

            {isAuthenticated ? (
              <>
                {isAdmin && (
                  <IconButton
                    color="inherit"
                    component={Link}
                    to="/admin"
                    title="Admin Panel"
                  >
                    <AdminPanelSettings />
                  </IconButton>
                )}
                <Button
                  color="inherit"
                  onClick={handleProfileMenuOpen}
                  startIcon={<Person />}
                >
                  {user?.firstName || 'Profile'}
                </Button>
              </>
            ) : (
              <>
                <Button
                  color="inherit"
                  component={Link}
                  to={ROUTES.LOGIN}
                >
                  Login
                </Button>
                <Button
                  color="inherit"
                  component={Link}
                  to={ROUTES.REGISTER}
                  variant="outlined"
                  sx={{ borderColor: 'white', color: 'white' }}
                >
                  Register
                </Button>
              </>
            )}
          </Box>
        )}
      </Toolbar>
      {profileMenu}
      {mobileDrawer}
    </AppBar>
  );
};

export default Header;
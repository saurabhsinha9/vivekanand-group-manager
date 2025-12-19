
import { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  AppBar, Toolbar, Typography, IconButton, Button, Drawer,
  List, ListItemButton, ListItemText, Box, Divider, useMediaQuery
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '@mui/material/styles';

export default function NavBar() {
  const { user, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const theme = useTheme();
  const isDesktop = useMediaQuery(theme.breakpoints.up('md'));

  const toggleDrawer = (state) => () => setOpen(state);

  const publicLinks = [
    { to: '/about', label: 'About' },
    { to: '/gallery', label: 'Gallery' },
    { to: '/mission', label: 'Mission' },
    { to: '/contact', label: 'Contact' },
  ];

  const authedLinks = [
    { to: '/members', label: 'Members' },
    { to: '/events', label: 'Events' },
    { to: '/finance', label: 'Finance' },
    { to: '/posts', label: 'Communication' },
    { to: '/uploads', label: 'Uploads' },
    { to: '/change-password', label: 'Change Password' },
  ];

  const adminLinks = [
    { to: '/admin/gallery', label: 'Update Gallery' },
    { to: '/admin/pages', label: 'Update Content' },
    { to: '/admin/register', label: 'Register Member' },
  ];

  const renderLinks = (links) =>
    links.map((l) => (
      <Button
        key={l.to}
        component={Link}
        to={l.to}
        color="inherit"
        sx={{ textTransform: 'none', fontWeight: 500 }}
      >
        {l.label}
      </Button>
    ));

  const drawerContent = (
    <Box sx={{ width: 280 }} role="presentation" onClick={toggleDrawer(false)} onKeyDown={toggleDrawer(false)}>
      <Box sx={{ px: 2, py: 2 }}>
        <Typography variant="h6">Vivekanand Group Manager</Typography>
      </Box>
      <Divider />
      <List>
        {publicLinks.map((l) => (
          <ListItemButton key={l.to} component={Link} to={l.to}>
            <ListItemText primary={l.label} />
          </ListItemButton>
        ))}
        {user && (
          <>
            <Divider sx={{ my: 1 }} />
            {authedLinks.map((l) => (
              <ListItemButton key={l.to} component={Link} to={l.to}>
                <ListItemText primary={l.label} />
              </ListItemButton>
            ))}
          </>
        )}
        {user?.role === 'ADMIN' && (
          <>
            <Divider sx={{ my: 1 }} />
            {adminLinks.map((l) => (
              <ListItemButton key={l.to} component={Link} to={l.to}>
                <ListItemText primary={l.label} />
              </ListItemButton>
            ))}
          </>
        )}
      </List>
      <Divider sx={{ my: 1 }} />
      <Box sx={{ px: 2, pb: 2 }}>
        {!user ? (
          <Button fullWidth variant="contained" component={Link} to="/login">
            Login
          </Button>
        ) : (
          <Button fullWidth variant="outlined" onClick={logout}>
            Logout
          </Button>
        )}
      </Box>
    </Box>
  );

  return (
    <>
      <AppBar position="sticky" elevation={2}>
        <Toolbar sx={{ minHeight: { xs: 56, sm: 64 }, justifyContent: 'space-between' }}>
          {/* Left: Brand */}
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            {!isDesktop && (
              <IconButton
                color="inherit"
                edge="start"
                sx={{ mr: 1 }}
                onClick={toggleDrawer(true)}
                aria-label="open navigation"
              >
                <MenuIcon />
              </IconButton>
            )}
            <Typography
              variant="h6"
              component={Link}
              to="/about"
              sx={{ textDecoration: 'none', color: 'inherit', fontWeight: 600 }}
            >
              Vivekanand Group Manager
            </Typography>
          </Box>

          {/* Right: Links */}
          {isDesktop && (
            <Box sx={{ display: 'flex', gap: 1 }}>
              {renderLinks(publicLinks)}
              {user && renderLinks(authedLinks)}
              {user?.role === 'ADMIN' && renderLinks(adminLinks)}
              {!user ? (
                <Button component={Link} to="/login" color="inherit">
                  Login
                </Button>
              ) : (
                <Button color="inherit" onClick={logout}>
                  Logout
                </Button>
              )}
            </Box>
          )}
        </Toolbar>
      </AppBar>

      {/* Mobile Drawer */}
      <Drawer anchor="left" open={open} onClose={toggleDrawer(false)} keepMounted>
        {drawerContent}
      </Drawer>
       </>
  );
}
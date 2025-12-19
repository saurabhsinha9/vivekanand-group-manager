
import { useState } from 'react';
import api from '../api/axios';
import {
  Container, Paper, Typography, Grid, TextField, Button, Alert, MenuItem
} from '@mui/material';
import { useAuth } from '../context/AuthContext';

const ROLES = [
  { value: 'MEMBER', label: 'Member' },
  // If you want admin to create admins/moderators too, add:
  // { value: 'ADMIN', label: 'Admin' },
  // { value: 'MODERATOR', label: 'Moderator' },
];

export default function AdminRegister() {
  const { user } = useAuth();
  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
    role: 'MEMBER', // default role
  });
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState(null);
  const [err, setErr] = useState(null);

  if (user?.role !== 'ADMIN') {
    return (
      <Container sx={{ mt: 3 }}>
        <Typography>You need admin rights.</Typography>
      </Container>
    );
  }

  const validate = () => {
    if (!form.username?.trim()) return 'Username is required';
    if (!form.email?.trim()) return 'Email is required';
    // basic email check
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) return 'Invalid email';
    if (!form.password || form.password.length < 6) return 'Password must be at least 6 characters';
    if (!form.role) return 'Role is required';
    return null;
  };

  const handleChange = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  const handleSubmit = async () => {
    setErr(null); setMsg(null);
    const v = validate();
    if (v) { setErr(v); return; }
    setLoading(true);
    try {
      const payload = {
        username: form.username.trim(),
        email: form.email.trim(),
        password: form.password,
        role: form.role,
      };
      await api.post('/auth/register', payload);
      setMsg(`User "${form.username}" registered successfully as ${form.role}.`);
      setForm({ username: '', email: '', password: '', role: 'MEMBER' });
    } catch (e) {
      const message =
        e.response?.data?.message ||
        e.response?.data?.error ||
        e.message ||
        'Registration failed';
      setErr(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container sx={{ mt: 3 }}>
      <Typography variant="h5">Register New Member</Typography>
      {err && <Alert severity="error" sx={{ mt: 2 }}>{err}</Alert>}
      {msg && <Alert severity="success" sx={{ mt: 2 }}>{msg}</Alert>}

      <Paper sx={{ p: 2, mt: 2 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <TextField
              label="Username"
              fullWidth
              value={form.username}
              onChange={handleChange('username')}
              autoComplete="off"
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              label="Email"
              fullWidth
              value={form.email}
              onChange={handleChange('email')}
              type="email"
              autoComplete="off"
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              label="Password"
              fullWidth
              value={form.password}
              onChange={handleChange('password')}
              type="password"
              autoComplete="new-password"
              helperText="Minimum 6 characters"
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              select
              label="Role"
              fullWidth
              value={form.role}
              onChange={handleChange('role')}
              helperText="Default: MEMBER"
            >
              {ROLES.map(r => (
                <MenuItem key={r.value} value={r.value}>{r.label}</MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid item xs={12}>
            <Button
              variant="contained"
              onClick={handleSubmit}
              disabled={loading}
            >
              {loading ? 'Registering...' : 'Register'}
            </Button>
          </Grid>
        </Grid>
      </Paper>
    </Container>
  );
}

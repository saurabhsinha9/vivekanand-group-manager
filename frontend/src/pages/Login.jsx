
import { useState } from 'react';
import { Container, TextField, Button, Paper, Typography } from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
export default function Login(){ const [username,setUsername]=useState(''); const [password,setPassword]=useState(''); const [error,setError]=useState(''); const { login }=useAuth(); const nav=useNavigate();
  const submit=async()=>{ try{ await login(username,password); nav('/members'); }catch(e){ setError(e.response?.data?.error || 'Login failed'); } };
  return (<Container maxWidth="sm" sx={{ mt: 4 }}><Paper sx={{ p:3 }}>
    <Typography variant="h5">Login</Typography>
    <TextField fullWidth label="Username" value={username} onChange={e=>setUsername(e.target.value)} sx={{mt:2}} />
    <TextField fullWidth label="Password" type="password" value={password} onChange={e=>setPassword(e.target.value)} sx={{mt:2}} />
    {error && <Typography color="error" sx={{mt:1}}>{error}</Typography>}
    <Button variant="contained" sx={{mt:2}} onClick={submit}>Login</Button>
  </Paper></Container>);
}

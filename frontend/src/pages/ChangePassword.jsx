
import { useState } from 'react';
import api from '../api/axios';
import { Container, Paper, Typography, TextField, Button } from '@mui/material';
export default function ChangePassword() {
  const [oldPassword,setOld] = useState(''); const [newPassword,setNew] = useState(''); const [msg,setMsg]=useState('');
  const submit = async () => { try { await api.post('/auth/change-password',{ oldPassword, newPassword }); setMsg('Password changed'); setOld(''); setNew(''); } catch(e){ setMsg(e.response?.data?.error || 'Failed'); } };
  return (
    <Container sx={{ mt:3 }}>
      <Paper sx={{ p:2 }}>
        <Typography variant="h5">Change Password</Typography>
        <TextField label="Old Password" type="password" fullWidth sx={{mt:2}} value={oldPassword} onChange={e=>setOld(e.target.value)}/>
        <TextField label="New Password" type="password" fullWidth sx={{mt:2}} value={newPassword} onChange={e=>setNew(e.target.value)}/>
        <Button variant="contained" sx={{mt:2}} onClick={submit}>Change</Button>
        {msg && <Typography sx={{mt:1}}>{msg}</Typography>}
      </Paper>
    </Container>
  );
}

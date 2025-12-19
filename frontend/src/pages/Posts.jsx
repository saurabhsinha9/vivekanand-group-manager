
import { useEffect, useState } from 'react';
import api from '../api/axios';
import { Container, Paper, Typography, TextField, Button, Grid, MenuItem, Switch, FormControlLabel, Table, TableHead, TableRow, TableCell, TableBody } from '@mui/material';
import { useAuth } from '../context/AuthContext';
const templates = ['', 'EVENT_REMINDER', 'THANK_YOU'];
export default function Posts(){ const [posts,setPosts]=useState([]); const [message,setMessage]=useState(''); const [broadcast,setBroadcast]=useState(true); const [templateCode,setTemplate]=useState(''); const { user }=useAuth();
  const load=async()=>{ const {data}=await api.get('/posts'); setPosts(data); }; useEffect(()=>{ load(); }, []);
  const createPost=async()=>{ const { data: post } = await api.post('/posts', { message, authorUserId: 0, broadcast, templateCode }); setMessage(''); setBroadcast(true); setTemplate(''); load(); };
  return (<Container sx={{ mt:3 }}>
    <Typography variant="h5">Communication Board</Typography>
    {user && (<Paper sx={{ p:2, mt:2 }}>
      <TextField fullWidth label="Message" value={message} onChange={e=>setMessage(e.target.value)} />
      <Grid container spacing={2} sx={{ mt:1 }}>
        <Grid item xs={12} sm={6}><TextField select fullWidth label="Template" value={templateCode} onChange={e=>setTemplate(e.target.value)}>{templates.map(t => <MenuItem key={t} value={t}>{t||'None'}</MenuItem>)}</TextField></Grid>
        <Grid item xs={12} sm={6}><FormControlLabel control={<Switch checked={broadcast} onChange={e=>setBroadcast(e.target.checked)} />} label="Broadcast" /></Grid>
      </Grid>
      <Button sx={{ mt:1 }} variant="contained" onClick={createPost}>Post</Button>
    </Paper>)}
    <Paper sx={{ mt:2 }}>
      <Table>
        <TableHead><TableRow><TableCell>Message</TableCell><TableCell>Broadcast</TableCell><TableCell>Created At</TableCell></TableRow></TableHead>
        <TableBody>{posts.map(p => (<TableRow key={p.id}><TableCell>{p.message}</TableCell><TableCell>{p.broadcast?'Yes':'No'}</TableCell><TableCell>{p.createdAt}</TableCell></TableRow>))}</TableBody>
      </Table>
    </Paper>
  </Container>);
}

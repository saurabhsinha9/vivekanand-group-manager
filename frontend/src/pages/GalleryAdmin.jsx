
// src/pages/GalleryAdmin.jsx
import { useEffect, useState } from 'react';
import api from '../api/axios';
import { Container, Paper, Typography, Grid, TextField, Button, Table, TableHead, TableRow, TableCell, TableBody } from '@mui/material';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function GalleryAdmin(){
  const { user } = useAuth();
  const [albums,setAlbums]=useState([]);
  const [form,setForm]=useState({ name:'', description:'', coverUploadId:'', visible:true });

  const load=async()=>{ const {data}=await api.get('/gallery/albums'); setAlbums(data); };
  useEffect(()=>{ load(); }, []);

  const create = async () => {
    const payload = {
      name: form.name,
      description: form.description,
      coverUploadId: form.coverUploadId ? parseInt(form.coverUploadId,10) : null,
      visible: form.visible
    };
    await api.post('/gallery/admin', payload);
    setForm({ name:'', description:'', coverUploadId:'', visible:true });
    load();
  };
  const toggleVisibility = async (a) => {
    await api.put(`/gallery/admin/${a.id}`, {
      name: a.name, description: a.description, coverUploadId: a.coverUploadId, visible: !a.visible
    });
    load();
  };
  const remove = async (id) => { await api.delete(`/gallery/admin/${id}`); load(); };

  if (user?.role !== 'ADMIN') return <Container sx={{mt:3}}><Typography>You need admin rights.</Typography></Container>;

  return (
    <Container sx={{ mt:3 }}>
      <Typography variant="h5">Gallery Admin</Typography>

      <Paper sx={{ p:2, mt:2 }}>
        <Typography>Create Album</Typography>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}><TextField fullWidth label="Name" value={form.name} onChange={e=>setForm({...form, name:e.target.value})}/></Grid>
          <Grid item xs={12}><TextField fullWidth label="Description" value={form.description} onChange={e=>setForm({...form, description:e.target.value})}/></Grid>
          <Grid item xs={12} sm={6}><TextField fullWidth label="Cover Upload ID (optional)" value={form.coverUploadId} onChange={e=>setForm({...form, coverUploadId:e.target.value})}/></Grid>
          <Grid item xs={12}><Button variant="contained" onClick={create}>Create</Button></Grid>
        </Grid>
      </Paper>

      <Paper sx={{ mt:2 }}>
        <Table>
          <TableHead><TableRow><TableCell>Name</TableCell><TableCell>Visible</TableCell><TableCell>Actions</TableCell></TableRow></TableHead>
          <TableBody>
            {albums.map(a => (
              <TableRow key={a.id}>
                <TableCell>{a.name}</TableCell>
                <TableCell>{a.visible ? 'Yes' : 'No'}</TableCell>
                <TableCell>
                  <Button variant="outlined" onClick={()=>toggleVisibility(a)}>{a.visible?'Hide':'Show'}</Button>
                  <Button sx={{ ml:1 }} color="error" onClick={()=>remove(a.id)}>Delete</Button>
                  <Button sx={{ ml:1 }} component={Link} to={`/albums/${a.id}`}>Open Album</Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
    </Container>
  );
}

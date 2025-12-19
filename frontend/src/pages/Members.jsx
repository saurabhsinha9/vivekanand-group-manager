
import { useEffect, useState } from 'react';
import api from '../api/axios';
import { Container, Paper, Typography, Button, TextField, Grid, Switch, FormControlLabel, Table, TableHead, TableRow, TableCell, TableBody, TablePagination } from '@mui/material';
import { useAuth } from '../context/AuthContext';
export default function Members(){ const [members,setMembers]=useState([]); const [q,setQ]=useState(''); const [page,setPage]=useState(0); const [rowsPerPage,setRowsPerPage]=useState(10); const [count,setCount]=useState(0); const [onlyActive,setOnlyActive]=useState(true); const [form,setForm]=useState({ fullName:'', phone:'', email:'', address:'', notifyEmail:true, notifyWhatsapp:true }); const { user } = useAuth();
  const load=async()=>{ const { data } = await api.get('/members/page', { params: { q, onlyActive, page, size: rowsPerPage } }); setMembers(data.content); setCount(data.totalElements); };
  useEffect(()=>{ load(); }, [q, page, rowsPerPage, onlyActive]);
  const create=async()=>{ await api.post('/members', form); setForm({ fullName:'', phone:'', email:'', address:'', notifyEmail:true, notifyWhatsapp:true }); load(); };
  const remove=async(id)=>{ await api.delete(`/members/${id}`); load(); };
  async function downloadMembersPdf(){ const res=await api.get('/members/report',{ responseType:'blob' }); const blob=new Blob([res.data],{ type:'application/pdf' }); const url=URL.createObjectURL(blob); const a=document.createElement('a'); a.href=url; a.download='members_report.pdf'; a.click(); URL.revokeObjectURL(url); }
  return (<Container sx={{ mt: 3 }}>
    <Typography variant="h5">Members</Typography>
    <Paper sx={{ p:2, mt:2 }}>
      <Grid container spacing={2} alignItems="center">
        <Grid item xs={12} sm={6}><TextField fullWidth label="Search (name/email)" value={q} onChange={e=>{ setPage(0); setQ(e.target.value); }}/></Grid>
        <Grid item xs={12} sm={6}><FormControlLabel control={<Switch checked={onlyActive} onChange={e=>{ setPage(0); setOnlyActive(e.target.checked); }}/>} label="Only Active" /></Grid>
      </Grid>
    </Paper>
    {user?.role==='ADMIN' && (<Paper sx={{ p:2, mt:2 }}>
      <Typography>Add Member</Typography>
      <Grid container spacing={2}>
        <Grid item xs={12} sm={6}><TextField fullWidth label="Full Name" value={form.fullName} onChange={e=>setForm({...form, fullName:e.target.value})}/></Grid>
        <Grid item xs={12} sm={6}><TextField fullWidth label="Phone" value={form.phone} onChange={e=>setForm({...form, phone:e.target.value})}/></Grid>
        <Grid item xs={12} sm={6}><TextField fullWidth label="Email" value={form.email} onChange={e=>setForm({...form, email:e.target.value})}/></Grid>
        <Grid item xs={12} sm={6}><TextField fullWidth label="Address" value={form.address} onChange={e=>setForm({...form, address:e.target.value})}/></Grid>
        <Grid item xs={12} sm={6}><FormControlLabel control={<Switch checked={form.notifyEmail} onChange={e=>setForm({...form, notifyEmail:e.target.checked})}/>} label="Notify via Email" /></Grid>
        <Grid item xs={12} sm={6}><FormControlLabel control={<Switch checked={form.notifyWhatsapp} onChange={e=>setForm({...form, notifyWhatsapp:e.target.checked})}/>} label="Notify via WhatsApp" /></Grid>
        <Grid item xs={12}><Button variant="contained" onClick={create}>Create</Button></Grid>
      </Grid>
    </Paper>)}
    <Paper sx={{ mt:2 }}>
      <Table>
        <TableHead><TableRow><TableCell>Name</TableCell><TableCell>Phone</TableCell><TableCell>Email</TableCell><TableCell>Notify</TableCell><TableCell>Actions</TableCell></TableRow></TableHead>
        <TableBody>
          {members.map(m => (
            <TableRow key={m.id}>
              <TableCell>{m.fullName}</TableCell>
              <TableCell>{m.phone}</TableCell>
              <TableCell>{m.email}</TableCell>
              <TableCell>{`${m.notifyEmail?'Email':''} ${m.notifyWhatsapp?'WhatsApp':''}`}</TableCell>
              <TableCell>{user?.role==='ADMIN' && (<Button color="error" onClick={()=>remove(m.id)}>Delete</Button>)}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
      <TablePagination component="div" count={count} page={page} onPageChange={(_,p)=>setPage(p)} rowsPerPage={rowsPerPage} onRowsPerPageChange={e=>{setRowsPerPage(parseInt(e.target.value,10)); setPage(0);}} />
    </Paper>
    <Button variant="outlined" sx={{ mt:2 }} onClick={downloadMembersPdf}>Download Members PDF</Button>
  </Container>);
}

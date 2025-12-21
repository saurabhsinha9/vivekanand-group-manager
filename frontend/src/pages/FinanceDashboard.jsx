
import { useEffect, useMemo, useState } from 'react';
import api from '../api/axios';
import { Container, Paper, Typography, Grid, TextField, MenuItem, Button, Table, TableHead, TableRow, TableCell, TableBody } from '@mui/material';
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer, LineChart, Line, XAxis, YAxis, CartesianGrid } from 'recharts';

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8'];
const catOptions = [ 'DONATIONS','PRASAD','DECORATION','SOUND','MISC' ];

export default function FinanceDashboard() {
  const [eventId, setEventId] = useState('');
  const [events, setEvents] = useState([]);
  const [records, setRecords] = useState([]);
  const [balance, setBalance] = useState('0.00');
  const [form, setForm] = useState({ type:'BUDGET', category:'MISC', amount:'', description:'' });
  const [receiptFile, setReceiptFile] = useState(null);
  const [summary, setSummary] = useState({ balance: 0, categories: {} });

  useEffect(()=>{ (async()=>{ const {data} = await api.get('/events'); setEvents(data); })(); }, []);

  const load = async () => {
    if (!eventId) return;
    const {data: recs} = await api.get(`/finance/event/${eventId}`);
    const {data: bal} = await api.get(`/finance/event/${eventId}/balance`);
    const {data: sum} = await api.get(`/finance/event/${eventId}/summary`);
    setRecords(recs); setBalance(bal); setSummary(sum);
  };

  useEffect(()=>{ load(); }, [eventId]);

  const addRecord = async () => {
    let uploadId = null;
    if (receiptFile) {
      const formData = new FormData(); formData.append('file', receiptFile);
      const { data: up } = await api.post('/uploads', formData, { headers: { 'Content-Type':'multipart/form-data' } });
      uploadId = up.id;
    }
    await api.post('/finance', { ...form, amount: parseFloat(form.amount), eventId: parseInt(eventId), uploadId });
    setForm({ type:'BUDGET', category:'MISC', amount:'', description:'' }); setReceiptFile(null); load();
  };

  const pieData = useMemo(()=> Object.entries(summary.categories || {}).map(([name, value]) => ({ name, value: parseFloat(value) })), [summary]);

  // simple time series (by timestamp string)
  const lineData = useMemo(()=> records.map(r => ({ x: new Date(r.timestamp).toLocaleString(), y: (r.type==='EXPENSE' ? -r.amount : r.amount) })), [records]);

  async function downloadCsv(){ const res = await api.get(`/finance/event/${eventId}/export.csv`, { responseType:'blob' }); const url = URL.createObjectURL(res.data); const a = document.createElement('a'); a.href=url; a.download=`finance_event_${eventId}.csv`; a.click(); URL.revokeObjectURL(url); }

  return (
    <Container sx={{ mt:3 }}>
      <Typography variant="h5">Finance Dashboard</Typography>
      <Grid container spacing={2} sx={{ mt:1 }}>
        <Grid item xs={12} sm={6}>
          <TextField select label="Select Event" fullWidth value={eventId} onChange={e=>setEventId(e.target.value)}>
            {events.map(e => <MenuItem key={e.id} value={e.id}>{e.name}</MenuItem>)}
          </TextField>
        </Grid>
      </Grid>

      {eventId && (
        <>
          <Paper sx={{ p:2, mt:2 }}>
            <Typography>Balance: ₹ {balance}</Typography>
            <Button sx={{ ml:2 }} onClick={downloadCsv}>Export CSV</Button>
          </Paper>

          <Paper sx={{ p:2, mt:2 }}>
            <Typography>Add Record</Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={3}>
                <TextField select label="Type" fullWidth value={form.type} onChange={e=>setForm({...form, type:e.target.value})}>
                  <MenuItem value="BUDGET">BUDGET</MenuItem>
                  <MenuItem value="INCOME">INCOME</MenuItem>
                  <MenuItem value="EXPENSE">EXPENSE</MenuItem>
                </TextField>
              </Grid>
              <Grid item xs={12} sm={3}>
                <TextField select label="Category" fullWidth value={form.category} onChange={e=>setForm({...form, category:e.target.value})}>
                  {catOptions.map(c => <MenuItem key={c} value={c}>{c}</MenuItem>)}
                </TextField>
              </Grid>
              <Grid item xs={12} sm={3}>
                <TextField label="Amount" fullWidth value={form.amount} onChange={e=>setForm({...form, amount:e.target.value})}/>
              </Grid>
              <Grid item xs={12} sm={3}>
                <TextField label="Description" fullWidth value={form.description} onChange={e=>setForm({...form, description:e.target.value})}/>
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField type="file" fullWidth onChange={e=>setReceiptFile(e.target.files?.[0] ?? null)} inputProps={{ accept:'application/pdf,image/png,image/jpeg' }} />
              </Grid>
              <Grid item xs={12}><Button variant="contained" onClick={addRecord}>Add</Button></Grid>
            </Grid>
          </Paper>

          <Grid container spacing={2} sx={{ mt:2 }}>
            <Grid item xs={12} md={6}>
              <Paper sx={{ p:2 }}>
                <Typography>Totals by Category</Typography>
                <ResponsiveContainer width="100%" height={300}>
                  <PieChart>
                    <Pie data={pieData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={100}>
                      {pieData.map((entry, index) => (<Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />))}
                    </Pie>
                    <Tooltip /><Legend />
                  </PieChart>
                </ResponsiveContainer>
              </Paper>
            </Grid>
            <Grid item xs={12} md={6}>
              <Paper sx={{ p:2 }}>
                <Typography>Income/Expense Timeline</Typography>
                <ResponsiveContainer width="100%" height={300}>
                  <LineChart data={lineData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="x" tick={{ fontSize: 12 }} />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Line type="monotone" dataKey="y" stroke="#8884d8" dot={false} name="Net" />
                  </LineChart>
                </ResponsiveContainer>
              </Paper>
            </Grid>
          </Grid>

          <Paper sx={{ mt:2 }}>
            <Table>
              <TableHead><TableRow><TableCell>Type</TableCell><TableCell>Category</TableCell><TableCell>Description</TableCell><TableCell>Amount</TableCell><TableCell>Timestamp</TableCell><TableCell>Receipt</TableCell></TableRow></TableHead>
              <TableBody>
                {records.map(r => (
                  <TableRow key={r.id}>
                    <TableCell>{r.type}</TableCell>
                    <TableCell>{r.category}</TableCell>
                    <TableCell>{r.description}</TableCell>
                    <TableCell>{r.amount}</TableCell>
                    <TableCell>{r.timestamp}</TableCell>
                    <TableCell>{r.uploadId ?
                     <Button onClick={async () => {
                       try {
                         const res = await api.get(`/uploads/${r.uploadId}`, { responseType: 'blob' });
                         const url = URL.createObjectURL(res.data);
                         const a = document.createElement('a');
                         a.href = url;
                         a.download = r.description || `receipt_${r.uploadId}`;
                         a.click();
                         URL.revokeObjectURL(url);
                       } catch (e) {
                         console.error('Download failed', e);
                       }
                     }}>
                       Download
                     </Button>
                     : '-'}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Paper>
        </>
      )}
    </Container>
  );
}

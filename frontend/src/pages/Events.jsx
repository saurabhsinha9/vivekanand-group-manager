
import { useEffect, useState } from 'react';
import api from '../api/axios';
import {
  Container, Paper, Typography, Grid, TextField, Button,
  Table, TableHead, TableRow, TableCell, TableBody, TablePagination
} from '@mui/material';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { TimePicker } from '@mui/x-date-pickers/TimePicker';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import dayjs from 'dayjs';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Events() {
  const [events, setEvents] = useState([]);
  const [q, setQ] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [count, setCount] = useState(0);

  const [form, setForm] = useState({
    name: '',
    description: '',
    startDate: null,  // required
    endDate: null,    // required
    startTime: null,  // optional
    endTime: null,    // optional
    location: ''
  });

  const { user } = useAuth();

  const load = async () => {
    const { data } = await api.get('/events/page', { params: { q, page, size: rowsPerPage } });
    setEvents(data.content);
    setCount(data.totalElements);
  };
  useEffect(() => { load(); }, [q, page, rowsPerPage]);

  const create = async () => {
    if (!form.startDate || !form.endDate) {
      alert('Start Date and End Date are required');
      return;
    }
    const payload = {
      name: form.name,
      description: form.description,
      location: form.location,
      startDate: dayjs(form.startDate).format('YYYY-MM-DD'),
      endDate: dayjs(form.endDate).format('YYYY-MM-DD'),
      startTime: form.startTime ? dayjs(form.startTime).format('HH:mm') : null,
      endTime: form.endTime ? dayjs(form.endTime).format('HH:mm') : null
    };
    try {
      await api.post('/events', payload);  // Backend DTO: EventCreateRequest
      setForm({
        name: '',
        description: '',
        startDate: null,
        endDate: null,
        startTime: null,
        endTime: null,
        location: ''
      });
      load();
    } catch (e) {
      const msg = e.response?.data?.error || e.message || 'Failed to create event';
      alert(msg);
    }
  };

  return (
    <Container sx={{ mt: 3 }}>
      <Typography variant="h5">Events</Typography>

      <Paper sx={{ p: 2, mt: 2 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={6}>
            <TextField fullWidth label="Search events" value={q} onChange={e => { setPage(0); setQ(e.target.value); }} />
          </Grid>
        </Grid>
      </Paper>

      {user?.role === 'ADMIN' && (
        <Paper sx={{ p: 2, mt: 2 }}>
          <Typography>Create Event</Typography>
          <LocalizationProvider dateAdapter={AdapterDayjs}>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField fullWidth label="Name" value={form.name}
                  onChange={e => setForm({ ...form, name: e.target.value })} />
              </Grid>
              <Grid item xs={12}>
                <TextField fullWidth label="Description" value={form.description}
                  onChange={e => setForm({ ...form, description: e.target.value })} />
              </Grid>

              {/* Mandatory dates */}
              <Grid item xs={12} sm={6}>
                <DatePicker
                  label="Start Date *"
                  value={form.startDate ? dayjs(form.startDate) : null}
                  onChange={(v) => setForm({ ...form, startDate: v ? v.toISOString() : null })}
                  slotProps={{ textField: { fullWidth: true, required: true } }}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <DatePicker
                  label="End Date *"
                  value={form.endDate ? dayjs(form.endDate) : null}
                  onChange={(v) => setForm({ ...form, endDate: v ? v.toISOString() : null })}
                  slotProps={{ textField: { fullWidth: true, required: true } }}
                />
              </Grid>

              {/* Optional times */}
              <Grid item xs={12} sm={6}>
                <TimePicker
                  label="Start Time (optional)"
                  value={form.startTime ? dayjs(form.startTime) : null}
                  onChange={(v) => setForm({ ...form, startTime: v ? v.toISOString() : null })}
                  slotProps={{ textField: { fullWidth: true } }}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TimePicker
                  label="End Time (optional)"
                  value={form.endTime ? dayjs(form.endTime) : null}
                  onChange={(v) => setForm({ ...form, endTime: v ? v.toISOString() : null })}
                  slotProps={{ textField: { fullWidth: true } }}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField fullWidth label="Location" value={form.location}
                  onChange={e => setForm({ ...form, location: e.target.value })} />
              </Grid>
              <Grid item xs={12}>
                <Button variant="contained" onClick={create}>Create</Button>
              </Grid>
            </Grid>
          </LocalizationProvider>
        </Paper>
      )}

      <Paper sx={{ mt: 2 }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Schedule</TableCell>
              <TableCell>Location</TableCell>
              <TableCell>Detail</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {events.map(e => (
              <TableRow key={e.id}>
                <TableCell>{e.name}</TableCell>
                <TableCell>{e.startTime} - {e.endTime}</TableCell>
                <TableCell>{e.location}</TableCell>
                <TableCell><Button component={Link} to={`/events/${e.id}`}>Open</Button></TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <TablePagination
          component="div"
          count={count}
          page={page}
          onPageChange={(_, p) => setPage(p)}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={e => { setRowsPerPage(parseInt(e.target.value, 10)); setPage(0); }}
        />
      </Paper>
    </Container>
  );
}

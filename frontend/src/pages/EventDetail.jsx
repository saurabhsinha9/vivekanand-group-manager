
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../api/axios';
import {
  Container, Paper, Typography, TextField, Button, Grid,
  Table, TableHead, TableRow, TableCell, TableBody, Autocomplete
} from '@mui/material';
import { useAuth } from '../context/AuthContext';

export default function EventDetail() {
  const { id } = useParams();
  const [event, setEvent] = useState(null);
  const [participants, setParticipants] = useState([]);
  const [pForm, setPForm] = useState({ role: '' });

  // Autocomplete state
  const [memberQuery, setMemberQuery] = useState('');
  const [memberOptions, setMemberOptions] = useState([]);
  const [selectedMember, setSelectedMember] = useState(null);

  const { user } = useAuth();

  const load = async () => {
    const { data: e } = await api.get(`/events/${id}`);
    // Use friendly participant view (includes memberName)
    const { data: ps } = await api.get(`/events/${id}/participants/view`);
    setEvent(e);
    setParticipants(ps);
  };
  useEffect(() => { load(); }, [id]);

  // Debounced member lookup
  useEffect(() => {
    const t = setTimeout(async () => {
      if (!memberQuery) { setMemberOptions([]); return; }
      const { data } = await api.get('/members/lookup', { params: { q: memberQuery } });
      setMemberOptions(data);
    }, 300);
    return () => clearTimeout(t);
  }, [memberQuery]);

  const addParticipant = async () => {
    const body = selectedMember
      ? { memberId: selectedMember.id, role: pForm.role }
      : { memberExternalKey: memberQuery, role: pForm.role };

    if (!pForm.role) {
      alert('Please enter a role');
      return;
    }
    if (!selectedMember && !memberQuery) {
      alert('Type a member name, phone, or email');
      return;
    }

    try {
      await api.post(`/events/${id}/participants`, body);
      setSelectedMember(null); setMemberQuery(''); setPForm({ role: '' });
      load();
    } catch (e) {
      const msg = e.response?.data?.error || e.message || 'Failed to add participant';
      alert(msg);
    }
  };

  const removeParticipant = async (pid) => { await api.delete(`/events/participants/${pid}`); load(); };

  // Blob download helpers (PDF)
  async function downloadBlob(path, filename) {
    const res = await api.get(path, { responseType: 'blob' });
    const blob = new Blob([res.data], { type: 'application/pdf' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a'); a.href = url; a.download = filename; a.click();
    URL.revokeObjectURL(url);
  }

  return (
    <Container sx={{ mt: 3 }}>
      {event && (
        <>
          <Typography variant="h5">{event.name}</Typography>
          <Typography>{event.description}</Typography>
          <Typography>Schedule: {event.startTime} - {event.endTime}</Typography>
          <Typography>Location: {event.location}</Typography>

          <Paper sx={{ p: 2, mt: 2 }}>
            <Typography variant="h6">Participants</Typography>

            {user && (
              <Grid container spacing={2} sx={{ mt: 1 }}>
                <Grid item xs={12} sm={6}>
                  <Autocomplete
                    options={memberOptions}
                    getOptionLabel={(opt) => `${opt.fullName} (${opt.phone || ''} ${opt.email || ''})`}
                    onInputChange={(_, value) => setMemberQuery(value)}
                    onChange={(_, value) => setSelectedMember(value)}
                    renderInput={(params) => (
                      <TextField {...params} fullWidth label="Member" placeholder="Type name/phone/email" />
                    )}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Role"
                    value={pForm.role}
                    onChange={(e) => setPForm({ ...pForm, role: e.target.value })}
                  />
                </Grid>
                <Grid item xs={12}>
                  <Button variant="contained" onClick={addParticipant}>Add Participant</Button>
                </Grid>
              </Grid>
            )}

            <Table sx={{ mt: 2 }}>
              <TableHead>
                <TableRow>
                  <TableCell>Member</TableCell>
                  <TableCell>Role</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {participants.map((p) => (
                  <TableRow key={p.id}>
                    <TableCell>{p.memberName}</TableCell>
                    <TableCell>{p.role}</TableCell>
                    <TableCell>
                      <Button color="error" onClick={() => removeParticipant(p.id)}>Remove</Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Paper>

          <Button
            variant="outlined"
            sx={{ mt: 2 }}
            onClick={() => downloadBlob(`/finance/event/${id}/report?eventName=${encodeURIComponent(event.name)}`, `finance_event_${id}.pdf`)}
          >
            Download Finance PDF
          </Button>
          <Button
            variant="outlined"
            sx={{ mt: 2, ml: 2 }}
            onClick={() => downloadBlob(`/events/${id}/participants/report`, `participants_event_${id}.pdf`)}
          >
            Download Participants PDF
          </Button>
        </>
      )}
    </Container>
  );
}

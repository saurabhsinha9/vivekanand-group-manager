
import { useState } from 'react';
import api from '../api/axios';
import { Container, Paper, Typography, Button, Grid, TextField, LinearProgress, Link } from '@mui/material';
export default function Uploads() {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const onSelect = (e) => { setError(''); setResult(null); setFile(e.target.files?.[0] ?? null); };
  const upload = async () => {
    if (!file) { setError('Please select a file (pdf/png/jpg/mp4)'); return; }
    const form = new FormData(); form.append('file', file);
    try { setUploading(true); const { data } = await api.post('/uploads', form, { headers: { 'Content-Type': 'multipart/form-data' } }); setResult(data); setError(''); } catch (e) { setError(e.response?.data?.error || 'Upload failed'); } finally { setUploading(false); }
  };
  const downloadUrl = result ? `${import.meta.env.VITE_API_URL || 'http://localhost:8080/api'}/uploads/${result.id}` : null;
  return (
    <Container sx={{ mt: 3 }}>
      <Typography variant="h5">Upload Bills / Invoices / Receipts</Typography>
      <Paper sx={{ p: 2, mt: 2 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={8}><TextField type="file" fullWidth inputProps={{ accept: 'application/pdf,image/png,image/jpeg,video/mp4' }} onChange={onSelect} /></Grid>
          <Grid item xs={12} sm={4}><Button variant="contained" onClick={upload} disabled={uploading} fullWidth>{uploading ? 'Uploading…' : 'Upload'}</Button></Grid>
          {uploading && (<Grid item xs={12}><LinearProgress /></Grid>)}
          {error && (<Grid item xs={12}><Typography color="error">{error}</Typography></Grid>)}
        </Grid>
      </Paper>
      {result && (
        <Paper sx={{ p: 2, mt: 2 }}>
          <Typography variant="h6">Upload Details</Typography>
          <Typography>ID: {result.id}</Typography>
          <Typography>File: {result.originalFilename}</Typography>
          <Typography>Type: {result.contentType}</Typography>
          <Typography>Size: {Math.round((result.sizeBytes || 0) / 1024)} KB</Typography>
          <Typography>Stored at: {result.storagePath}</Typography>
          <Button sx={{ mt: 2 }} variant="outlined" component={Link} href={downloadUrl} target="_blank" rel="noopener">Download</Button>
        </Paper>
      )}
    </Container>
  );
}

import { useState, useEffect } from 'react';
import api from '../api/axios';
import {
  Container, Paper, Typography, Button, Grid, TextField,
  LinearProgress, Link, Table, TableHead, TableRow, TableCell, TableBody
} from '@mui/material';

export default function Uploads() {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [uploads, setUploads] = useState([]);

  const base = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

  const onSelect = (e) => {
    setError('');
    setResult(null);
    setFile(e.target.files?.[0] ?? null);
  };

  const loadUploads = async () => {
    try {
      const { data } = await api.get('/uploads');
      setUploads(data);
    } catch (e) {
      setError('Failed to load uploads');
    }
  };

  useEffect(() => { loadUploads(); }, []);

  const upload = async () => {
    if (!file) {
      setError('Please select a file (pdf/png/jpg/mp4)');
      return;
    }
    const form = new FormData();
    form.append('file', file);
    try {
      setUploading(true);
      const { data } = await api.post('/uploads', form, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      setResult(data);
      setError('');
      loadUploads();
    } catch (e) {
      setError(e.response?.data?.error || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  const deleteUpload = async (id) => {
    try {
      await api.delete(`/uploads/${id}`);
      setUploads(uploads.filter(u => u.id !== id));
    } catch (e) {
      setError('Delete failed');
    }
  };

  const downloadUrl = result ? `${base}/uploads/${result.id}` : null;

  return (
    <Container sx={{ mt: 3 }}>
      <Typography variant="h5">Manage Uploads</Typography>

      {/* Upload form */}
      <Paper sx={{ p: 2, mt: 2 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={8}>
            <TextField
              type="file"
              fullWidth
              inputProps={{ accept: 'application/pdf,image/png,image/jpeg,video/mp4' }}
              onChange={onSelect}
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <Button variant="contained" onClick={upload} disabled={uploading} fullWidth>
              {uploading ? 'Uploading…' : 'Upload'}
            </Button>
          </Grid>
          {uploading && (<Grid item xs={12}><LinearProgress /></Grid>)}
          {error && (<Grid item xs={12}><Typography color="error">{error}</Typography></Grid>)}
        </Grid>
      </Paper>

      {/* Upload result */}
      {result && (
        <Paper sx={{ p: 2, mt: 2 }}>
          <Typography variant="h6">Upload Details</Typography>
          <Typography>ID: {result.id}</Typography>
          <Typography>File: {result.originalFilename}</Typography>
          <Typography>Type: {result.contentType}</Typography>
          <Typography>Size: {Math.round((result.sizeBytes || 0) / 1024)} KB</Typography>
          <Typography>Stored at: {result.storagePath}</Typography>
          <Button sx={{ mt: 2 }} variant="outlined" component={Link} href={downloadUrl} target="_blank" rel="noopener">
            Download
          </Button>
        </Paper>
      )}

      {/* Uploads list */}
      <Paper sx={{ p: 2, mt: 2 }}>
        <Typography variant="h6">All Uploads</Typography>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Filename</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Size</TableCell>
              <TableCell>Uploaded At</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {uploads.map(u => (
              <TableRow key={u.id}>
                <TableCell>{u.id}</TableCell>
                <TableCell>{u.originalFilename}</TableCell>
                <TableCell>{u.contentType}</TableCell>
                <TableCell>{Math.round((u.sizeBytes || 0) / 1024)} KB</TableCell>
                <TableCell>{u.uploadedAt}</TableCell>
                <TableCell>
                  <Button variant="outlined" component={Link}
                          href={`${base}/uploads/${u.id}`} target="_blank" rel="noopener">
                    Download
                  </Button>
                  <Button color="error" sx={{ ml: 1 }} onClick={() => deleteUpload(u.id)}>
                    Delete
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
    </Container>
  );
}
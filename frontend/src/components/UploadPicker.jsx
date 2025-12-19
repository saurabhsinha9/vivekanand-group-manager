
import { useEffect, useMemo, useState } from 'react';
import api from '../api/axios';
import {
  Dialog, DialogTitle, DialogContent, DialogActions,
  Button, TextField, ToggleButtonGroup, ToggleButton,
  Grid, Card, CardMedia, CardContent, Typography
} from '@mui/material';

export default function UploadPicker({ open, onClose, onSelect }) {
  const [uploads, setUploads] = useState([]);
  const [query, setQuery] = useState('');
  const [type, setType] = useState('images'); // 'images' | 'videos' | 'pdfs' | 'all'
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!open) return;
    (async () => {
      try { setLoading(true);
        const { data } = await api.get('/uploads'); // existing endpoint
        setUploads(data || []);
      } finally { setLoading(false); }
    })();
  }, [open]);

  const filtered = useMemo(() => {
    const q = (query || '').toLowerCase();
    return (uploads || [])
      .filter(u => {
        const ct = (u.contentType || '').toLowerCase();
        const name = (u.originalFilename || '').toLowerCase();
        const matchQ = !q || name.includes(q);
        const matchType =
          type === 'all' ||
          (type === 'images' && ct.startsWith('image/')) ||
          (type === 'videos' && ct === 'video/mp4') ||
          (type === 'pdfs' && ct === 'application/pdf');
        return matchQ && matchType;
      });
  }, [uploads, query, type]);

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="md">
      <DialogTitle>Select Upload</DialogTitle>
      <DialogContent dividers>
        <ToggleButtonGroup
          exclusive
          value={type}
          onChange={(_, v) => v && setType(v)}
          size="small"
          sx={{ mb: 2 }}
        >
          <ToggleButton value="images">Images</ToggleButton>
          <ToggleButton value="videos">Videos</ToggleButton>
          <ToggleButton value="pdfs">PDFs</ToggleButton>
          <ToggleButton value="all">All</ToggleButton>
        </ToggleButtonGroup>

        <TextField
          fullWidth
          label="Search by filename"
          value={query}
          onChange={e => setQuery(e.target.value)}
        />

        {loading && <Typography sx={{ mt:2 }}>Loading...</Typography>}

        {!loading && (
          <Grid container spacing={2} sx={{ mt: 2 }}>
            {filtered.map(u => (
              <Grid item xs={12} sm={6} md={4} key={u.id}>
                <Card sx={{ cursor: 'pointer' }} onClick={() => onSelect(u)}>
                  {u.contentType?.startsWith('image/') ? (
                    <CardMedia
                      component="img"
                      height="160"
                      image={`${import.meta.env.VITE_API_URL || 'http://localhost:8080/api'}/uploads/${u.id}`}
                      alt={u.originalFilename}
                    />
                  ) : (
                    <CardContent>
                      <Typography variant="subtitle1">
                        {u.originalFilename}
                      </Typography>
                      <Typography variant="body2">
                        {u.contentType}
                      </Typography>
                    </CardContent>
                  )}
                </Card>
              </Grid>
            ))}
            {filtered.length === 0 && !loading && (
              <Grid item xs={12}>
                <Typography>No uploads found. Try changing filters or search.</Typography>
              </Grid>
            )}
          </Grid>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Close</Button>
      </DialogActions>
    </Dialog>
  );
}

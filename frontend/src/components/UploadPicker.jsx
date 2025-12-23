
import { useEffect, useMemo, useRef, useState } from 'react';
import api from '../api/axios';
import {
  Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, ToggleButtonGroup,
  ToggleButton, Grid, Card, CardMedia, CardContent, Typography, Skeleton
} from '@mui/material';

export default function UploadPicker({ open, onClose, onSelect }) {
  const [uploads, setUploads] = useState([]);
  const [query, setQuery] = useState('');
  const [type, setType] = useState('images'); // images | videos | pdfs | all
  const [loading, setLoading] = useState(false);
  const abortRef = useRef(null);

  useEffect(() => {
    if (!open) return;
    (async () => {
      try {
        setLoading(true);
        abortRef.current?.abort();
        abortRef.current = new AbortController();
        const { data } = await api.get('/uploads', { signal: abortRef.current.signal });
        setUploads(data || []);
      } catch (err) {
        if (err.name !== 'CanceledError') console.error('Load uploads failed', err);
      } finally {
        setLoading(false);
      }
    })();
    return () => abortRef.current?.abort();
  }, [open]);

  const [debouncedQuery, setDebouncedQuery] = useState('');
  useEffect(() => {
    const t = setTimeout(() => setDebouncedQuery((query || '').toLowerCase()), 250);
    return () => clearTimeout(t);
  }, [query]);

  const filtered = useMemo(() => {
    const q = debouncedQuery;
    return (uploads || []).filter(u => {
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
  }, [uploads, debouncedQuery, type]);

  const base = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="md">
      <DialogTitle>Select Upload</DialogTitle>
      <DialogContent dividers>
        <ToggleButtonGroup exclusive value={type} onChange={(_, v) => v && setType(v)} size="small" sx={{ mb: 2 }}>
          <ToggleButton value="images">Images</ToggleButton>
          <ToggleButton value="videos">Videos</ToggleButton>
          <ToggleButton value="pdfs">PDFs</ToggleButton>
          <ToggleButton value="all">All</ToggleButton>
        </ToggleButtonGroup>

        <TextField fullWidth label="Search by filename" value={query} onChange={e => setQuery(e.target.value)} />

        {loading && (
          <Grid container spacing={2} sx={{ mt: 2 }}>
            {Array.from({ length: 6 }).map((_, i) => (
              <Grid item xs={12} sm={6} md={4} key={i}>
                <Skeleton variant="rectangular" height={160} />
              </Grid>
            ))}
          </Grid>
        )}

        {!loading && (
          <Grid container spacing={2} sx={{ mt: 2 }}>
            {filtered.map(u => (
              <Grid item xs={12} sm={6} md={4} key={u.id}>
                <Card sx={{
                  cursor: 'pointer',
                  borderRadius: 2,
                  overflow: 'hidden',
                  transition: 'transform .2s ease, box-shadow .2s ease',
                  '&:hover': { transform: 'translateY(-2px)', boxShadow: 4 }
                }} onClick={() => onSelect(u)}>
                  {u.contentType?.startsWith('image/') ? (
                    <CardMedia component="img" height="160"
                      image={`${base}/public/uploads/${u.id}/poster`} alt={u.originalFilename ?? 'image'} loading="lazy" />
                  ) : u.contentType === 'video/mp4' ? (
                    <CardMedia component="img" height="160"
                      image={`${base}/public/uploads/${u.id}/poster`} alt={u.originalFilename ?? 'video poster'} loading="lazy" />
                  ) : (
                    <CardContent>
                      <Typography variant="subtitle1">{u.originalFilename ?? '(file)'}</Typography>
                      <Typography variant="body2">{u.contentType || 'unknown'}</Typography>
                    </CardContent>
                  )}
                </Card>
              </Grid>
            ))}
            {filtered.length === 0 && !loading && (
              <Grid item xs={12}><Typography>No uploads found. Try changing filters or search.</Typography></Grid>
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

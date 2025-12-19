
// src/pages/AdminPageEditor.jsx
import { useEffect, useState } from 'react';
import api from '../api/axios';
import {
  Container, Paper, Typography, Grid, TextField, Button, Alert,
  Table, TableHead, TableRow, TableCell, TableBody, MenuItem
} from '@mui/material';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const PAGES = [
  { slug: 'about',   label: 'About' },
  { slug: 'mission', label: 'Mission' },
  { slug: 'contact', label: 'Contact' },
];

export default function AdminPageEditor() {
  const { user } = useAuth();

  // Form state for the selected page
  const [slug, setSlug] = useState('about');
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [status, setStatus] = useState('');

  // Overview table state
  const [overview, setOverview] = useState([]); // [{slug,label,title,status}]
  const [msg, setMsg] = useState(null);
  const [err, setErr] = useState(null);

  // Load draft into the editor form
  const loadDraft = async (s = slug) => {
    setErr(null); setMsg(null);
    try {
      const { data } = await api.get(`/admin/pages/${s}/draft`);
      setSlug(s);
      setTitle(data.title || '');
      setContent(data.content || '');
      setStatus(data.status || '');
      setMsg(`Loaded draft for "${s}"`);
    } catch (e) {
      setErr(e.response?.data?.message || e.message);
    }
  };

  // Save draft
  const saveDraft = async () => {
    setErr(null); setMsg(null);
    try {
      const { data } = await api.put(`/admin/pages/${slug}`, { title, content });
      setStatus(data.status || '');
      setMsg('Draft saved.');
      // refresh overview row
      await loadOverview();
    } catch (e) {
      setErr(e.response?.data?.message || e.message);
    }
  };

  // Publish draft
  const publish = async () => {
    setErr(null); setMsg(null);
    try {
      const { data } = await api.post(`/admin/pages/${slug}/publish`);
      setStatus(data.status || '');
      setMsg('Published successfully.');
      // refresh overview row
      await loadOverview();
    } catch (e) {
      setErr(e.response?.data?.message || e.message);
    }
  };

  // Overview table (public status & title)
  const loadOverview = async () => {
    try {
      const rows = await Promise.all(PAGES.map(async p => {
        const { data } = await api.get(`/public/pages/${p.slug}`);
        return { slug: p.slug, label: p.label, title: data.title, status: data.status };
      }));
      setOverview(rows);
    } catch (e) {
      // If public endpoint fails, still show slugs
      setOverview(PAGES.map(p => ({ slug: p.slug, label: p.label, title: '-', status: 'UNKNOWN' })));
    }
  };

  // Initial load: overview and default draft
  useEffect(() => { loadOverview(); }, []);
  useEffect(() => { loadDraft(slug); }, [slug]);

  if (user?.role !== 'ADMIN') {
    return (
      <Container sx={{ mt: 3 }}>
        <Typography>You need admin rights.</Typography>
      </Container>
    );
  }

  return (
    <Container sx={{ mt: 3 }}>
      <Typography variant="h5">Static Pages Admin</Typography>

      {err && <Alert severity="error" sx={{ mt: 2 }}>{err}</Alert>}
      {msg && <Alert severity="success" sx={{ mt: 2 }}>{msg}</Alert>}

      {/* === Editor === */}
      <Paper sx={{ p: 2, mt: 2 }}>
        <Typography>Edit Static Page</Typography>
        <Grid container spacing={2} sx={{ mt: 1 }}>
          <Grid item xs={12} sm={4}>
            <TextField
              select
              fullWidth
              label="Select Page"
              value={slug}
              onChange={e => setSlug(e.target.value)}
            >
              {PAGES.map(p => (
                <MenuItem key={p.slug} value={p.slug}>{p.label}</MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid item xs={12} sm={4}>
            <TextField
              fullWidth
              label="Status"
              value={status || ''}
              InputProps={{ readOnly: true }}
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <Button variant="outlined" onClick={() => loadDraft(slug)} fullWidth>
              Load Draft
            </Button>
          </Grid>

          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Title"
              value={title}
              onChange={e => setTitle(e.target.value)}
            />
          </Grid>

          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Content"
              value={content}
              onChange={e => setContent(e.target.value)}
              multiline
              minRows={8}
            />
          </Grid>

          <Grid item xs={12} sm={6}>
            <Button variant="contained" onClick={saveDraft} fullWidth>
              Save Draft
            </Button>
          </Grid>
          <Grid item xs={12} sm={6}>
            <Button variant="contained" color="success" onClick={publish} fullWidth>
              Publish
            </Button>
          </Grid>

          <Grid item xs={12}>
            <Button
              component={Link}
              to={`/${slug}`}
              variant="text"
            >
              View Public {slug.toUpperCase()}
            </Button>
          </Grid>
        </Grid>
      </Paper>

      {/* === Overview Table === */}
      <Paper sx={{ mt: 2 }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Page</TableCell>
              <TableCell>Slug</TableCell>
              <TableCell>Title (Public)</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {overview.map(r => (
              <TableRow key={r.slug}>
                <TableCell>{r.label}</TableCell>
                <TableCell>{r.slug}</TableCell>
                <TableCell>{r.title}</TableCell>
                <TableCell>{r.status}</TableCell>
                <TableCell>
                  <Button variant="outlined" onClick={() => { setSlug(r.slug); loadDraft(r.slug); }}>
                    Load Draft
                  </Button>
                  <Button sx={{ ml: 1 }} component={Link} to={`/${r.slug}`}>
                    Open Public
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

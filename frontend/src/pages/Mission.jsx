import { useEffect, useState } from 'react';
import api from '../api/axios';
import { Container, Typography, CircularProgress, Alert } from '@mui/material';

export default function Mission() {
  const [page, setPage] = useState(null);
  const [err, setErr] = useState(null);

  useEffect(() => {
    api.get('/public/pages/mission')
      .then(({ data }) => setPage(data))
      .catch(e => setErr(e.message));
  }, []);

  if (err) return <Alert severity="error">{err}</Alert>;
  if (!page) return <CircularProgress />;

  return (
    <Container sx={{ mt: 3 }}>
      <Typography variant="h4">{page.title}</Typography>
      <Typography sx={{ mt: 2, whiteSpace: 'pre-line' }}>{page.content}</Typography>
    </Container>
  );
}
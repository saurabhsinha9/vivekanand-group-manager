
import { useEffect, useState } from 'react';
import api from '../api/axios';
import {
  Container, Typography, Grid, Card, CardMedia, CardContent, CardActions,
  Button, Pagination
} from '@mui/material';

export default function Gallery() {
  const base = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

  // Albums view state
  const [albums, setAlbums] = useState([]);
  const [selectedAlbum, setSelectedAlbum] = useState(null);

  // Items view state
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(12);
  const [totalPages, setTotalPages] = useState(1);

  // Load visible albums (PUBLIC)
  const loadAlbums = async () => {
    try {
      const { data } = await api.get('/gallery/albums'); // public endpoint
      setAlbums((data || []).filter(a => a.visible));
    } catch (e) {
      console.error('Failed to load albums', e);
      setAlbums([]);
    }
  };

  // Load visible items for selected album (PUBLIC)
  const loadItems = async (albumId, pg = 0) => {
    try {
      const { data } = await api.get(`/gallery/albums/${albumId}/items`, {
        params: { page: pg, size } // onlyVisible is forced true server-side
      });
      setItems(data?.content || []);
      setTotalPages(data?.totalPages || 1);
      setPage(pg);
    } catch (e) {
      console.error('Failed to load album items', e);
      setItems([]);
      setTotalPages(1);
      setPage(0);
    }
  };

  useEffect(() => { loadAlbums(); }, []);
  useEffect(() => { if (selectedAlbum) loadItems(selectedAlbum.id, 0); }, [selectedAlbum, size]);

  const openAlbum = (album) => setSelectedAlbum(album);
  const backToAlbums = () => { setSelectedAlbum(null); setItems([]); };

  // Public download (no JWT, uses /api/public/uploads/{id})
  async function downloadPublic(uploadId, filename) {
    try {
      const res = await fetch(`${base}/public/uploads/${uploadId}`);
      if (!res.ok) throw new Error(`Download failed: ${res.status}`);
      const blob = await res.blob();
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a'); a.href = url; a.download = filename || `image_${uploadId}`; a.click();
      URL.revokeObjectURL(url);
    } catch (e) {
      setError('Failed to download file.');
    }
  }

  return (
    <Container sx={{ mt: 3 }}>
      {!selectedAlbum && (
        <>
          <Typography variant="h4">Gallery</Typography>
          <Grid container spacing={2} sx={{ mt: 2 }}>
            {albums.map(a => (
              <Grid item xs={12} sm={6} md={4} key={a.id}>
                <Card>
                  {/* Album cover (public image) or placeholder */}
                  {a.coverUploadId ? (
                    <CardMedia
                      component="img"
                      height="200"
                      image={`${base}/public/uploads/${a.coverUploadId}`}
                      alt={a.name}
                    />
                  ) : (
                    <CardMedia
                      component="img"
                      height="200"
                      image="data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' width='400' height='200'><rect width='100%' height='100%' fill='%23ccc'/><text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' fill='%23666' font-size='20'>No Cover</text></svg>"
                      alt="No Cover"
                    />
                  )}
                  <CardContent>
                    <Typography variant="h6">{a.name}</Typography>
                    {a.description && <Typography variant="body2">{a.description}</Typography>}
                  </CardContent>
                  <CardActions>
                    <Button variant="contained" onClick={() => openAlbum(a)}>Open</Button>
                  </CardActions>
                </Card>
              </Grid>
            ))}
            {albums.length === 0 && (
              <Grid item xs={12}>
                <Typography>No public albums available.</Typography>
              </Grid>
            )}
          </Grid>
        </>
      )}

      {selectedAlbum && (
        <>
          <Typography variant="h5">{selectedAlbum.name}</Typography>
          <Button sx={{ mt: 1 }} onClick={backToAlbums}>← Back to Albums</Button>

          <Grid container spacing={2} sx={{ mt: 2 }}>
            {items.map(it => (
              <Grid item xs={12} sm={6} md={4} key={it.id}>
                <Card>
                  {/* Public image path for thumbnails */}
                  {it.contentType === 'video/mp4' ? (
                    <video height={180} controls>
                      <source src={`${base}/public/uploads/${it.uploadId}`} type="video/mp4" />
                    </video>
                  ) : (
                    <CardMedia
                      component="img"
                      height="180"
                      image={`${base}/public/uploads/${it.uploadId}`}
                      alt={it.title || 'image'}
                    />
                  )}
                  <CardContent>
                    <Typography variant="subtitle1">{it.title || '(untitled)'}</Typography>
                    {it.caption && <Typography variant="body2">{it.caption}</Typography>}
                    {it.tags && <Typography variant="caption">Tags: {it.tags}</Typography>}
                  </CardContent>
                  <CardActions>
                    <Button onClick={() => downloadPublic(it.uploadId, it.title || it.id)}>Download</Button>
                  </CardActions>
                </Card>
              </Grid>
            ))}
            {items.length === 0 && (
              <Grid item xs={12}>
                <Typography>This album has no public items yet.</Typography>
              </Grid>
            )}
          </Grid>

          {totalPages > 1 && (
            <Pagination
              sx={{ mt: 2 }}
              count={totalPages}
              page={page + 1}
              onChange={(_, p) => loadItems(selectedAlbum.id, p - 1)}
            />
          )}
        </>
      )}
    </Container>
  );
}


import { useEffect, useState, useMemo, useCallback, useRef } from 'react';
import api from '../api/axios';
import {
  AppBar, Toolbar, Container, Typography, Grid, Card, CardMedia, CardContent, CardActions,
  Button, Pagination, Dialog, DialogTitle, DialogContent, DialogActions, IconButton,
  Chip, Stack, Switch, FormControlLabel, Skeleton
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import ArrowBackIosNewIcon from '@mui/icons-material/ArrowBackIosNew';
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import CollectionsIcon from '@mui/icons-material/Collections';

export default function Gallery() {
  const base = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

  // Albums state
  const [albums, setAlbums] = useState([]);
  const [selectedAlbum, setSelectedAlbum] = useState(null);

  // Items state
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(12);
  const [totalPages, setTotalPages] = useState(1);
  const [loadingItems, setLoadingItems] = useState(false);
  const [error, setError] = useState('');

  // Filters
  const [filter, setFilter] = useState('all'); // all | images | videos

  // Slideshow
  const [slideshowOpen, setSlideshowOpen] = useState(false);
  const [slideIndex, setSlideIndex] = useState(0);
  const [autoPlay, setAutoPlay] = useState(false);
  const autoPlayTimer = useRef(null);

  const filteredItems = useMemo(() => {
    const f = filter;
    if (f === 'images') return items.filter(it => (it.contentType || '').toLowerCase().startsWith('image/'));
    if (f === 'videos') return items.filter(it => (it.contentType || '').toLowerCase().startsWith('video/'));
    return items;
  }, [items, filter]);

  const imageItems = useMemo(
    () => items.filter(it => (it.contentType || '').toLowerCase().startsWith('image/')),
    [items]
  );

  const openSlideshow = useCallback((startIndex = 0) => {
    if (imageItems.length === 0) return;
    setSlideIndex(Math.max(0, Math.min(startIndex, imageItems.length - 1)));
    setSlideshowOpen(true);
  }, [imageItems]);

  const closeSlideshow = useCallback(() => {
    setSlideshowOpen(false);
    setAutoPlay(false);
    if (autoPlayTimer.current) { clearInterval(autoPlayTimer.current); autoPlayTimer.current = null; }
  }, []);

  const nextSlide = useCallback(() => {
    setSlideIndex(idx => (idx + 1) % imageItems.length);
  }, [imageItems.length]);
  const prevSlide = useCallback(() => {
    setSlideIndex(idx => (idx - 1 + imageItems.length) % imageItems.length);
  }, [imageItems.length]);

  // Keyboard navigation
  useEffect(() => {
    if (!slideshowOpen) return;
    const onKey = (e) => {
      if (e.key === 'ArrowRight') nextSlide();
      else if (e.key === 'ArrowLeft') prevSlide();
      else if (e.key === 'Escape') closeSlideshow();
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [slideshowOpen, nextSlide, prevSlide, closeSlideshow]);

  // Auto-play
  useEffect(() => {
    if (!slideshowOpen) return;
    if (autoPlay) {
      autoPlayTimer.current = setInterval(nextSlide, 3000);
      return () => { clearInterval(autoPlayTimer.current); autoPlayTimer.current = null; };
    } else if (autoPlayTimer.current) {
      clearInterval(autoPlayTimer.current);
      autoPlayTimer.current = null;
    }
  }, [autoPlay, slideshowOpen, nextSlide]);

  const loadAlbums = async () => {
    try {
      const { data } = await api.get('/gallery/albums');
      setAlbums((data || []).filter(a => a.visible));
    } catch (e) {
      setAlbums([]);
      setError('Failed to load albums.');
    }
  };

  const loadItems = async (albumId, pg = 0) => {
    try {
      setLoadingItems(true);
      const { data } = await api.get(`/gallery/albums/${albumId}/items`, { params: { page: pg, size } });
      setItems(data?.content || []);
      setTotalPages(data?.totalPages || 1);
      setPage(pg);
    } catch (e) {
      setItems([]);
      setTotalPages(1);
      setPage(0);
      setError('Failed to load album items.');
    } finally {
      setLoadingItems(false);
    }
  };

  useEffect(() => { loadAlbums(); }, []);
  useEffect(() => { if (selectedAlbum) loadItems(selectedAlbum.id, 0); }, [selectedAlbum, size]);

  const openAlbum = (album) => setSelectedAlbum(album);
  const backToAlbums = () => { setSelectedAlbum(null); setItems([]); setPage(0); setTotalPages(1); setError(''); closeSlideshow(); };

  const downloadPublic = async (uploadId, filename) => {
    try {
      const url = `${base}/public/uploads/${uploadId}`;
      const a = document.createElement('a');
      a.href = url;
      a.download = filename || `file_${uploadId}`;
      a.rel = 'noopener';
      a.style.display = 'none';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    } catch (e) {
      setError('Failed to download file.');
    }
  };

  return (
    <>
      {/* Modern top bar */}
      <AppBar position="sticky" color="default" elevation={0} sx={{ borderBottom: '1px solid #eee' }}>
        <Toolbar sx={{ gap: 2 }}>
          <CollectionsIcon color="primary" />
          <Typography variant="h6" sx={{ flexGrow: 1 }}>
            {selectedAlbum ? selectedAlbum.name : 'Gallery'}
          </Typography>

          {selectedAlbum ? (
            <>
              <Stack direction="row" spacing={1} alignItems="center">
                <Chip label="All" color={filter === 'all' ? 'primary' : 'default'} onClick={() => setFilter('all')} />
                <Chip label="Images" color={filter === 'images' ? 'primary' : 'default'} onClick={() => setFilter('images')} />
                <Chip label="Videos" color={filter === 'videos' ? 'primary' : 'default'} onClick={() => setFilter('videos')} />
              </Stack>
              <Button variant="outlined" onClick={() => openSlideshow(0)} disabled={imageItems.length === 0} startIcon={<PlayArrowIcon />}>
                Slideshow
              </Button>
              <Button onClick={backToAlbums}>Back</Button>
            </>
          ) : (
            <Typography variant="body2" color="text.secondary">Browse albums</Typography>
          )}
        </Toolbar>
      </AppBar>

      <Container sx={{ mt: 3 }}>
        {!selectedAlbum && (
          <>
            {error && <Typography variant="body2" color="error" sx={{ mb: 2 }}>{error}</Typography>}
            <Grid container spacing={2}>
              {albums.map(a => (
                <Grid item xs={12} sm={6} md={4} key={a.id}>
                  <Card sx={{
                    borderRadius: 2,
                    overflow: 'hidden',
                    transition: 'transform .2s ease, box-shadow .2s ease',
                    '&:hover': { transform: 'translateY(-2px)', boxShadow: 4, cursor: 'pointer' }
                  }} onClick={() => openAlbum(a)}>
                    {a.coverUploadId ? (
                      <CardMedia component="img" height="200"
                        image={`${base}/public/uploads/${a.coverUploadId}/poster`} alt={a.name} loading="lazy" />
                    ) : (
                      <Skeleton variant="rectangular" height={200} />
                    )}
                    <CardContent>
                      <Typography variant="h6">{a.name}</Typography>
                      {a.description && <Typography variant="body2" color="text.secondary">{a.description}</Typography>}
                    </CardContent>
                  </Card>
                </Grid>
              ))}
              {albums.length === 0 && (
                <Grid item xs={12}><Typography>No public albums available.</Typography></Grid>
              )}
            </Grid>
          </>
        )}

        {selectedAlbum && (
          <>
            {error && <Typography variant="body2" color="error" sx={{ mb: 2 }}>{error}</Typography>}

            {loadingItems ? (
              <Grid container spacing={2}>
                {Array.from({ length: size }).map((_, i) => (
                  <Grid item xs={12} sm={6} md={4} key={i}>
                    <Card>
                      <Skeleton variant="rectangular" height={180} />
                      <CardContent>
                        <Skeleton width="60%" />
                        <Skeleton width="80%" />
                      </CardContent>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            ) : (
              <Grid container spacing={2}>
                {filteredItems.map(it => (
                  <Grid item xs={12} sm={6} md={4} key={it.id}>
                    <Card sx={{
                      borderRadius: 2,
                      overflow: 'hidden',
                      transition: 'transform .2s ease, box-shadow .2s ease',
                      '&:hover': { transform: 'translateY(-2px)', boxShadow: 4 }
                    }}>
                      {(it.contentType || '').toLowerCase().startsWith('image/') ? (
                        <CardMedia
                          component="img" height="220"
                          image={`${base}/public/uploads/${it.uploadId}`} alt={it.title || 'image'} loading="lazy"
                          onClick={() => {
                            const startIdx = imageItems.findIndex(img => img.id === it.id);
                            openSlideshow(startIdx >= 0 ? startIdx : 0);
                          }}
                          sx={{ cursor: 'zoom-in', objectFit: 'cover' }}
                        />
                      ) : (
                        <CardContent sx={{ p: 0 }}>
                          <video
                            height={220} controls muted playsInline preload="metadata"
                            poster={`${base}/public/uploads/${it.uploadId}/poster`}
                            style={{ display: 'block', width: '100%', backgroundColor: '#000' }}
                          >
                            <source src={`${base}/public/uploads/${it.uploadId}`} type="video/mp4" />
                          </video>
                        </CardContent>
                      )}

                      <CardContent>
                        <Typography variant="subtitle1" noWrap>{it.title || '(untitled)'}</Typography>
                        {it.caption && <Typography variant="body2" color="text.secondary">{it.caption}</Typography>}
                        {it.tags && (
                          <Stack direction="row" spacing={1} sx={{ mt: 1, flexWrap: 'wrap' }}>
                            {it.tags.split(',').map(tag => (
                              <Chip key={tag} label={tag.trim()} size="small" variant="outlined" />
                            ))}
                          </Stack>
                        )}
                      </CardContent>

                      <CardActions sx={{ justifyContent: 'space-between' }}>
                        <Button onClick={() => downloadPublic(it.uploadId, it.title || String(it.id))}>Download</Button>
                        {(it.contentType || '').toLowerCase().startsWith('image/') && (
                          <Button onClick={() => {
                            const startIdx = imageItems.findIndex(img => img.id === it.id);
                            openSlideshow(startIdx >= 0 ? startIdx : 0);
                          }}>View</Button>
                        )}
                      </CardActions>
                    </Card>
                  </Grid>
                ))}

                {filteredItems.length === 0 && (
                  <Grid item xs={12}><Typography>This album has no public items yet.</Typography></Grid>
                )}
              </Grid>
            )}

            {totalPages > 1 && (
              <Pagination sx={{ mt: 3 }} count={totalPages} page={page + 1}
                onChange={(_, p) => loadItems(selectedAlbum.id, p - 1)} />
            )}
          </>
        )}
      </Container>

      {/* Slideshow */}
      <Dialog open={slideshowOpen} onClose={closeSlideshow} fullWidth maxWidth="lg">
        <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Typography variant="h6">{imageItems[slideIndex]?.title || '(untitled)'}</Typography>
          <Stack direction="row" alignItems="center" spacing={1}>
            <FormControlLabel
              control={<Switch checked={autoPlay} onChange={(e) => setAutoPlay(e.target.checked)} />}
              label="Auto-play"
            />
            <IconButton onClick={closeSlideshow}><CloseIcon /></IconButton>
          </Stack>
        </DialogTitle>
        <DialogContent sx={{ p: 0, backgroundColor: '#111' }}>
          {imageItems.length > 0 && (
            <CardMedia
              component="img"
              image={`${base}/public/uploads/${imageItems[slideIndex].uploadId}`}
              alt={imageItems[slideIndex]?.title || 'image'}
              loading="lazy"
              sx={{ width: '100%', maxHeight: '70vh', objectFit: 'contain' }}
            />
          )}
          <div style={{ padding: '12px 16px', color: '#ddd' }}>
            {imageItems[slideIndex]?.caption && <Typography variant="body2">{imageItems[slideIndex].caption}</Typography>}
            {imageItems[slideIndex]?.tags && <Typography variant="caption">Tags: {imageItems[slideIndex].tags}</Typography>}
          </div>
        </DialogContent>
        <DialogActions sx={{ justifyContent: 'space-between' }}>
          <IconButton onClick={prevSlide}><ArrowBackIosNewIcon /></IconButton>
          <Typography variant="caption" color="text.secondary">
            {imageItems.length > 0 ? `${slideIndex + 1} / ${imageItems.length}` : '0 / 0'}
          </Typography>
          <IconButton onClick={nextSlide}><ArrowForwardIosIcon /></IconButton>
        </DialogActions>
      </Dialog>
    </>
  );
}

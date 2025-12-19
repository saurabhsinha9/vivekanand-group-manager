
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../api/axios';
import {
  Container, Paper, Typography, Grid, TextField, Button,
  Card, CardMedia, CardContent, Dialog, DialogTitle,
  DialogContent, DialogActions
} from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { DndContext, closestCenter } from '@dnd-kit/core';
import { SortableContext, arrayMove, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import UploadPicker from '../components/UploadPicker';

function SortableItem({ item, onEdit, onDelete }) {
  const { attributes, listeners, setNodeRef, transform, transition } = useSortable({ id: item.id });
  const style = { transform: CSS.Transform.toString(transform), transition };
  return (
    <Grid item xs={12} sm={6} md={4} ref={setNodeRef} style={style}>
      <Card>
        <CardMedia component="img" height="180"
          image={`${import.meta.env.VITE_API_URL || 'http://localhost:8080/api'}/uploads/${item.uploadId}`} />
        <CardContent>
          <Typography variant="subtitle1">{item.title || '(untitled)'}</Typography>
          <Typography variant="body2">{item.caption}</Typography>
          <Typography variant="caption">Tags: {item.tags}</Typography>
          <Grid container spacing={1} sx={{ mt:1 }}>
            <Grid item><Button {...attributes} {...listeners} variant="outlined">Drag</Button></Grid>
            <Grid item><Button onClick={()=>onEdit(item)} variant="outlined">Edit</Button></Grid>
            <Grid item><Button color="error" onClick={()=>onDelete(item.id)}>Delete</Button></Grid>
          </Grid>
        </CardContent>
      </Card>
    </Grid>
  );
}

export default function AlbumManager(){
  const { albumId } = useParams();
  const { user } = useAuth();
  const [items,setItems]=useState([]);
  const [orderedIds,setOrderedIds]=useState([]);
  const [page,setPage]=useState(0);
  const [size,setSize]=useState(50);

  // new item form
  const [form,setForm]=useState({ title:'', caption:'', tags:'', position:0, visible:true });
  const [selectedUpload, setSelectedUpload] = useState(null); // { id, originalFilename, contentType, ... }
  const [pickerOpen, setPickerOpen] = useState(false);

  // edit dialog
  const [editOpen,setEditOpen] = useState(false);
  const [editItem,setEditItem] = useState(null);

  const load=async()=>{ 
    const {data}=await api.get(`/gallery/albums/${albumId}/items`, { params: { onlyVisible:false, page, size } });
    setItems(data.content);
    setOrderedIds(data.content.map(i => i.id));
  };
  useEffect(()=>{ load(); }, [albumId, page, size]);

  const addItem = async () => {
    if (!selectedUpload?.id) {
      alert('Please select an upload first');
      return;
    }
    const payload = {
      albumId: parseInt(albumId,10),
      uploadId: selectedUpload.id,
      title: form.title,
      caption: form.caption,
      tags: form.tags,
      position: form.position,
      visible: form.visible
    };
    await api.post('/gallery/admin/items', payload);
    setForm({ title:'', caption:'', tags:'', position:0, visible:true });
    setSelectedUpload(null);
    load();
  };

  const updateItem = async (id, payload) => { await api.put(`/gallery/admin/items/${id}`, payload); load(); };
  const deleteItem = async (id) => { await api.delete(`/gallery/admin/items/${id}`); load(); };

  const onEdit = (item) => { setEditItem(item); setEditOpen(true); };
  const onEditSave = async () => {
    const { id, title, caption, tags, visible } = editItem;
    await updateItem(id, { title, caption, tags, visible });
    setEditOpen(false); setEditItem(null);
  };

  // DnD reorder
  const onDragEnd = async (event) => {
    const { active, over } = event;
    if (!over || active.id === over.id) return;
    const oldIndex = orderedIds.indexOf(active.id);
    const newIndex = orderedIds.indexOf(over.id);
    const newOrder = arrayMove(orderedIds, oldIndex, newIndex);
    setOrderedIds(newOrder);
    await api.post(`/gallery/admin/albums/${albumId}/reorder`, newOrder);
    load();
  };

  if (user?.role !== 'ADMIN') return <Container sx={{mt:3}}><Typography>You need admin rights.</Typography></Container>;

  return (
    <Container sx={{ mt:3 }}>
      <Typography variant="h5">Album #{albumId}</Typography>

      {/* Upload Picker */}
      <UploadPicker
        open={pickerOpen}
        onClose={()=>setPickerOpen(false)}
        onSelect={(u)=>{ setSelectedUpload(u); setPickerOpen(false); }}
      />

      <Paper sx={{ p:2, mt:2 }}>
        <Typography>Add Item</Typography>
        <Grid container spacing={2}>
          {/* Select Upload instead of typing ID */}
          <Grid item xs={12} sm={6}>
            <Button variant="outlined" onClick={()=>setPickerOpen(true)}>
              {selectedUpload ? `Selected: ${selectedUpload.originalFilename}` : 'Select Upload'}
            </Button>
            {selectedUpload?.contentType?.startsWith('image/') && (
              <Card sx={{ mt:1 }}>
                <CardMedia component="img" height="140"
                  image={`${import.meta.env.VITE_API_URL || 'http://localhost:8080/api'}/uploads/${selectedUpload.id}`}
                />
              </Card>
            )}
          </Grid>

          <Grid item xs={12} sm={6}><TextField fullWidth label="Title" value={form.title} onChange={e=>setForm({...form, title:e.target.value})}/></Grid>
          <Grid item xs={12}><TextField fullWidth label="Caption" value={form.caption} onChange={e=>setForm({...form, caption:e.target.value})}/></Grid>
          <Grid item xs={12} sm={6}><TextField fullWidth label="Tags (comma-separated)" value={form.tags} onChange={e=>setForm({...form, tags:e.target.value})}/></Grid>
          <Grid item xs={12} sm={3}><TextField fullWidth label="Position (optional)" value={form.position} onChange={e=>setForm({...form, position:parseInt(e.target.value||'0',10)})}/></Grid>
          <Grid item xs={12}><Button variant="contained" onClick={addItem}>Add</Button></Grid>
        </Grid>
      </Paper>

      <DndContext collisionDetection={closestCenter} onDragEnd={onDragEnd}>
        <SortableContext items={orderedIds} strategy={verticalListSortingStrategy}>
          <Grid container spacing={2} sx={{ mt:2 }}>
            {items.map(it => (
              <SortableItem key={it.id} item={it} onEdit={onEdit} onDelete={deleteItem} />
            ))}
          </Grid>
        </SortableContext>
      </DndContext>

      {/* Edit dialog */}
      <Dialog open={editOpen} onClose={()=>setEditOpen(false)}>
        <DialogTitle>Edit Item</DialogTitle>
        {editItem && (
          <>
            <DialogContent>
              <TextField fullWidth label="Title" sx={{mt:1}} value={editItem.title || ''} onChange={e=>setEditItem({...editItem, title:e.target.value})}/>
              <TextField fullWidth label="Caption" sx={{mt:1}} value={editItem.caption || ''} onChange={e=>setEditItem({...editItem, caption:e.target.value})}/>
              <TextField fullWidth label="Tags" sx={{mt:1}} value={editItem.tags || ''} onChange={e=>setEditItem({...editItem, tags:e.target.value})}/>
            </DialogContent>
            <DialogActions>
              <Button onClick={()=>setEditOpen(false)}>Cancel</Button>
              <Button variant="contained" onClick={onEditSave}>Save</Button>
            </DialogActions>
          </>
        )}
      </Dialog>
    </Container>
  );
}

package com.vivekanand.manager.gallery;

import com.vivekanand.manager.gallery.dto.AlbumDto;
import com.vivekanand.manager.gallery.dto.GalleryItemDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gallery/admin")
@PreAuthorize("hasRole('ADMIN')")
public class GalleryAdminController {

    private final GalleryService svc;

    public GalleryAdminController(GalleryService s) {
        this.svc = s;
    }

    // Albums CRUD
    @PostMapping
    public Album createAlbum(@RequestBody AlbumDto dto) {
        return svc.createAlbum(dto);
    }

    @PutMapping("/{id}")
    public Album updateAlbum(@PathVariable Long id, @RequestBody AlbumDto dto) {
        return svc.updateAlbum(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteAlbum(@PathVariable Long id) {
        svc.deleteAlbum(id);
    }

    // Items CRUD
    @PostMapping("/items")
    public GalleryItem addItem(@RequestBody GalleryItemDto dto) {
        return svc.addItem(dto);
    }

    @PutMapping("/items/{id}")
    public GalleryItem updateItem(@PathVariable Long id, @RequestBody GalleryItemDto dto) {
        return svc.updateItem(id, dto);
    }

    @DeleteMapping("/items/{id}")
    public void softDeleteItem(@PathVariable Long id) {
        svc.softDeleteItem(id);
    }

    // Drag-and-drop reordering: send array of item IDs in new order
    @PostMapping("/albums/{albumId}/reorder")
    public java.util.List<GalleryItem> reorder(@PathVariable Long albumId, @RequestBody java.util.List<Long> orderedItemIds) {
        return svc.bulkReorder(albumId, orderedItemIds);
    }
}
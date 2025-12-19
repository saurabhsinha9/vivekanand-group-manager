package com.vivekanand.manager.gallery;

import com.vivekanand.manager.gallery.dto.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class GalleryService {
    private final AlbumRepository albumRepo;
    private final GalleryItemRepository itemRepo;

    public GalleryService(AlbumRepository a, GalleryItemRepository g) {
        this.albumRepo = a;
        this.itemRepo = g;
    }

    public Album createAlbum(AlbumDto dto) {
        Album a = new Album();
        a.setName(dto.name());
        a.setDescription(dto.description());
        a.setCoverUploadId(dto.coverUploadId());
        if (dto.visible() != null) a.setVisible(dto.visible());
        return albumRepo.save(a);
    }

    public Album updateAlbum(Long id, AlbumDto dto) {
        Album a = albumRepo.findById(id).orElseThrow();
        a.setName(dto.name());
        a.setDescription(dto.description());
        a.setCoverUploadId(dto.coverUploadId());
        if (dto.visible() != null) a.setVisible(dto.visible());
        a.setUpdatedAt(Instant.now());
        return albumRepo.save(a);
    }

    public void deleteAlbum(Long id) {
        albumRepo.deleteById(id);
    }

    public GalleryItem addItem(GalleryItemDto dto) {
        GalleryItem gi = new GalleryItem();
        gi.setAlbumId(dto.albumId());
        gi.setUploadId(dto.uploadId());
        gi.setTitle(dto.title());
        gi.setCaption(dto.caption());
        gi.setTags(dto.tags());
        gi.setPosition(dto.position() == null ? 0 : dto.position());
        gi.setVisible(dto.visible() == null ? true : dto.visible());
        return itemRepo.save(gi);
    }

    public GalleryItem updateItem(Long id, GalleryItemDto dto) {
        GalleryItem gi = itemRepo.findById(id).orElseThrow();
        gi.setTitle(dto.title());
        gi.setCaption(dto.caption());
        gi.setTags(dto.tags());
        gi.setPosition(dto.position() == null ? gi.getPosition() : dto.position());
        if (dto.visible() != null) gi.setVisible(dto.visible());
        gi.setUpdatedAt(Instant.now());
        return itemRepo.save(gi);
    }

    public void softDeleteItem(Long id) {
        var gi = itemRepo.findById(id).orElseThrow();
        gi.setDeletedAt(Instant.now());
        itemRepo.save(gi);
    }

    public Page<GalleryItem> listItems(Long albumId, boolean onlyVisible, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("position").ascending().and(Sort.by("createdAt").descending()));
        return onlyVisible ? itemRepo.findByAlbumIdAndVisibleTrueAndDeletedAtIsNull(albumId, pageable)
                : itemRepo.findByAlbumIdAndDeletedAtIsNull(albumId, pageable);
    }

    public List<GalleryItem> bulkReorder(Long albumId, List<Long> orderedItemIds) {
        // minimal reorder: update 'position' according to index
        var items = itemRepo.findByAlbumIdAndDeletedAtIsNull(albumId, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        var posMap = new java.util.HashMap<Long, Integer>();
        for (int i = 0; i < orderedItemIds.size(); i++) posMap.put(orderedItemIds.get(i), i);
        for (var gi : items) {
            if (posMap.containsKey(gi.getId())) {
                gi.setPosition(posMap.get(gi.getId()));
            }
        }
        return itemRepo.saveAll(items);
    }
}

package com.vivekanand.manager.gallery;

import com.vivekanand.manager.gallery.dto.*;
import com.vivekanand.manager.uploads.Upload;
import com.vivekanand.manager.uploads.UploadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class GalleryService {
    @Autowired
    private UploadRepository uploadRepo;
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

    public Page<AlbumItemDto> listItems(Long albumId, boolean onlyVisible, int page, int size) {
        var pageable = PageRequest.of(page, size,
                Sort.by("position").ascending().and(Sort.by("createdAt").descending()));
        Page<GalleryItem> pageItems = onlyVisible
                ? itemRepo.findByAlbumIdAndVisibleTrueAndDeletedAtIsNull(albumId, pageable)
                : itemRepo.findByAlbumIdAndDeletedAtIsNull(albumId, pageable);

        // Batch-load uploads to avoid N+1
        var uploadIds = pageItems.getContent().stream()
                .map(GalleryItem::getUploadId)
                .distinct()
                .toList();

        var uploadsById = uploadRepo.findAllById(uploadIds).stream()
                .collect(java.util.stream.Collectors.toMap(Upload::getId, java.util.function.Function.identity()));

        return pageItems.map(it -> {
            Upload up = uploadsById.get(it.getUploadId());
            String ct = (up != null) ? up.getContentType() : null;
            return new AlbumItemDto(
                    it.getId(),
                    it.getUploadId(),
                    it.getTitle(),
                    it.getCaption(),
                    it.getTags(),
                    it.getPosition(),
                    it.isVisible(),
                    ct
            );
        });
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

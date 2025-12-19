package com.vivekanand.manager.gallery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GalleryItemRepository extends JpaRepository<GalleryItem, Long> {
    Page<GalleryItem> findByAlbumIdAndDeletedAtIsNull(Long albumId, Pageable pageable);

    Page<GalleryItem> findByAlbumIdAndVisibleTrueAndDeletedAtIsNull(Long albumId, Pageable pageable);
}

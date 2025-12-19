package com.vivekanand.manager.gallery;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "gallery_items")
public class GalleryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long albumId;
    @Column(nullable = false)
    private Long uploadId;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String caption;
    private String tags; // e.g. "saraswati,2026,stage"
    private Integer position = 0;
    private boolean visible = true;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    private Instant deletedAt; // soft delete
}

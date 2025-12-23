package com.vivekanand.manager.gallery.dto;

public record AlbumItemDto(
        Long id,
        Long uploadId,
        String title,
        String caption,
        String tags,
        Integer position,
        Boolean visible,
        String contentType // <- from Upload.contentType
) {}

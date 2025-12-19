package com.vivekanand.manager.gallery.dto;

import jakarta.validation.constraints.NotNull;

public record GalleryItemDto(
        @NotNull Long albumId,
        @NotNull Long uploadId,
        String title,
        String caption,
        String tags,
        Integer position,
        Boolean visible
) {
}

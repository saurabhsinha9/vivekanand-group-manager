package com.vivekanand.manager.gallery.dto;

import jakarta.validation.constraints.NotBlank;

public record AlbumDto(
        @NotBlank String name,
        String description,
        Long coverUploadId,
        Boolean visible
) {
}

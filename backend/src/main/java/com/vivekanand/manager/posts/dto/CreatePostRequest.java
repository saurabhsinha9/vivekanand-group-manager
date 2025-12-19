package com.vivekanand.manager.posts.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePostRequest(
        Long authorUserId,
        @NotBlank String message,
        Boolean broadcast,
        String templateCode // optional: select template
) {}

package com.vivekanand.manager.posts;


import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaAttachmentRepository extends JpaRepository<MediaAttachment, Long> {
    void deleteByUploadId(Long uploadId);
}
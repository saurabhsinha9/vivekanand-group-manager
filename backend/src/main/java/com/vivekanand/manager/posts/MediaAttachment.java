
package com.vivekanand.manager.posts;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "media_attachments")
public class MediaAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long postId;
    private Long uploadId;
    private String type;
}

package com.vivekanand.manager.content;


import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "static_content_pages", uniqueConstraints = @UniqueConstraint(columnNames = {"slug"}))
@Data
public class StaticContentPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // e.g., "about", "mission", "contact"
    @Column(nullable = false, length = 64)
    private String slug;

    @Column(nullable = false, length = 256)
    private String title;

    // Published content
    @Lob
    private String publishedContent;

    // Draft content (editable)
    @Lob
    private String draftContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PUBLISHED;

    private Instant updatedAt;
    private Instant publishedAt;

    public enum Status { DRAFT, PUBLISHED }

}

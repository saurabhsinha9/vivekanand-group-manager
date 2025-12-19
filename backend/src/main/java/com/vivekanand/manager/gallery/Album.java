package com.vivekanand.manager.gallery;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "albums")
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String description;
    private Long coverUploadId;
    private boolean visible = true;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
}

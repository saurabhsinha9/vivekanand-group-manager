
package com.vivekanand.manager.uploads;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "uploads")
public class Upload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String originalFilename;
    private String contentType;
    private Long sizeBytes;
    private String storagePath;
    private Instant uploadedAt = Instant.now();
}


package com.vivekanand.manager.posts;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long authorUserId;
    @Column(columnDefinition = "TEXT")
    private String message;
    private Instant createdAt = Instant.now();
    private boolean broadcast = true;
}

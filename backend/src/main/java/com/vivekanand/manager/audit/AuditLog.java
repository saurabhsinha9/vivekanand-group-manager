
package com.vivekanand.manager.audit;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String action;
    private String entity;
    @Column(columnDefinition = "TEXT")
    private String details;
    private Instant timestamp = Instant.now();
}

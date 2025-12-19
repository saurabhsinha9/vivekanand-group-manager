
package com.vivekanand.manager.finance;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "financial_records")
public class FinancialRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long eventId;
    @Enumerated(EnumType.STRING)
    private FinancialRecordType type;
    @Enumerated(EnumType.STRING)
    private FinancialCategory category = FinancialCategory.MISC;
    private BigDecimal amount;
    private String description;
    private Instant timestamp = Instant.now();
    private Long uploadId;
}

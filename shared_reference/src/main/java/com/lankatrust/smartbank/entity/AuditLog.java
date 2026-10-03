package com.lankatrust.smartbank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Shared/collaborative function (Section 7). System auto-creates immutable
 * log entries; no manual create/update allowed. Administrator reads;
 * deletion requires Manager approval (enforced at the service layer).
 */
@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150)
    private String actorEmail;

    @Column(nullable = false, length = 100)
    private String action; // e.g. ACCOUNT_CREATED, TRANSFER_COMPLETED, LOAN_APPROVED

    @Column(length = 100)
    private String entityType;

    private Long entityId;

    @Column(length = 500)
    private String details;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}

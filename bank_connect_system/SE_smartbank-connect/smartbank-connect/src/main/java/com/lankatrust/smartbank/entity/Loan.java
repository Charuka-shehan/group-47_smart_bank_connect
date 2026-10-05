package com.lankatrust.smartbank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Member 4 (Farwin M. N. S.) - Loan Request & Approval.
 * Workflow: SUBMITTED -> UNDER_VERIFICATION (Officer) -> COMPLIANCE_CHECK
 * (Compliance Officer) -> APPROVED / REJECTED (Manager final approval).
 */
@Entity
@Table(name = "loans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String applicationNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(nullable = false, length = 50)
    private String loanType; // PERSONAL, HOME, VEHICLE, EDUCATION

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal requestedAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(length = 100)
    private String employmentStatus;

    @Column(length = 500)
    private String documentsSummary;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 30)
    private LoanStatus status = LoanStatus.SUBMITTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(length = 500)
    private String remarks;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}

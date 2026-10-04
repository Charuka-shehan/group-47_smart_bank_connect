package com.lankatrust.smartbank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Member 1 (Liyanage C. I. L.) - Account Management.
 * C: Officer submits new account (PENDING_APPROVAL) -> R: Staff view anytime
 * -> U: Officer submits updates/freeze -> D: Officer submits closure,
 * all subject to Manager approval (see {@link #approvedBy}).
 */
@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String accountNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(nullable = false, length = 30)
    private String accountType; // SAVINGS, CURRENT, FIXED_DEPOSIT

    @Builder.Default
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal initialDeposit = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 30)
    private AccountStatus status = AccountStatus.PENDING_APPROVAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(length = 100)
    private String occupation;

    @Column(precision = 18, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(length = 200)
    private String nomineeName;

    @Column(length = 100)
    private String nomineeRelationship;

    @Column(length = 400)
    private String signaturePath;

    @Column(length = 400)
    private String nicFrontPath;

    @Column(length = 400)
    private String nicBackPath;

    @Column(length = 400)
    private String proofOfAddressPath;

    @Column(length = 500)
    private String rejectionReason;

    /** Officer who submitted the create/update/close request. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by")
    private User submittedBy;

    /** Manager who approved the pending request; null while awaiting approval. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}

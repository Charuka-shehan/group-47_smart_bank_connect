package com.lankatrust.smartbank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Member 2 (Ayyash M. A. M.) - Fund Transfer, and
 * Member 3 (Sasna R. K. S.) - Transaction History.
 * Auto-created by the system on every transfer; searchable/filterable
 * by customers and staff (Section 6.3 of the proposal).
 */
@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String referenceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    /** Null for external/beneficiary transfers outside the bank. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id")
    private Account destinationAccount;

    @Column(length = 100)
    private String beneficiaryName;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type; // TRANSFER, DEPOSIT, WITHDRAWAL

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(length = 255)
    private String remarks;

    /** True once OTP step-up verification has been completed for high-value transfers. */
    @Builder.Default
    private boolean otpVerified = false;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime processedAt;
}

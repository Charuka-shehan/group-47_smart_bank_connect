package com.lankatrust.smartbank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Staging record for a Bank Officer's request to create a new staff account.
 * Holds the proposed staff details (password already BCrypt-hashed, never
 * stored in plaintext) until a Bank Manager / System Administrator approves
 * or rejects the request via the shared ApprovalRequest workflow.
 */
@Entity
@Table(name = "staff_creation_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffCreationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 150)
    private String email;

    /** Already BCrypt-hashed by the officer-facing controller — never stored in plaintext. */
    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(length = 30)
    private String nic;

    @Column(length = 50)
    private String employeeId;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING";

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

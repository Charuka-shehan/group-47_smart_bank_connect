package com.lankatrust.smartbank.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Member 5 (Senanayaka C. S. R.) - Authentication & Access Control.
 * Represents any system user: Customer, Officer, Manager, Administrator
 * or Compliance Officer. Role-based access is applied via the `role` field
 * and enforced in SecurityConfig.
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String fullName;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** BCrypt-hashed password. Null when the user registered via Google OAuth only. */
    @Column(length = 255)
    private String password;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 30, unique = true)
    private String nic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean googleLinked = false;

    /** Simulated OTP code sent for sensitive actions (login step-up, transfers). */
    @Column(length = 6)
    private String otpCode;

    private LocalDateTime otpExpiry;

    @Builder.Default
    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    private LocalDateTime lockedUntil;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastLogin;
}

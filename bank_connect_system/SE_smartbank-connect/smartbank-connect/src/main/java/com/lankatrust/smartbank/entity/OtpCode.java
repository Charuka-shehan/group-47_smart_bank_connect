package com.lankatrust.smartbank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_codes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long transferId;

    private String code;

    private String purpose;

    private LocalDateTime expiresAt;

    @Builder.Default
    private int attempts = 0;

    @Builder.Default
    private boolean consumed = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}


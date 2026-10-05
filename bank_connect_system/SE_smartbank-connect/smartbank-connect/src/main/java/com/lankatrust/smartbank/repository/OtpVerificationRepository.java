package com.lankatrust.smartbank.repository;

import com.lankatrust.smartbank.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findFirstByEmailOrderByCreatedAtDesc(String email);
    long countByEmailAndCreatedAtAfter(String email, LocalDateTime createdAt);
}

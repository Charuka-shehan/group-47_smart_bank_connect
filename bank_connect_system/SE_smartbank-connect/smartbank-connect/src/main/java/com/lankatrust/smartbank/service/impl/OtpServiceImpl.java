package com.lankatrust.smartbank.service.impl;

import com.lankatrust.smartbank.entity.*;
import com.lankatrust.smartbank.repository.OtpVerificationRepository;
import com.lankatrust.smartbank.repository.UserRepository;
import com.lankatrust.smartbank.service.NotificationService;
import com.lankatrust.smartbank.service.OtpService;
import com.lankatrust.smartbank.service.SettingsService;
import com.lankatrust.smartbank.util.EmailTemplates;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Implementation of OTP Service for centralized OTP management.
 * Handles generation, verification, and security for all sensitive operations.
 */
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    
    private final UserRepository userRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final NotificationService notificationService;
    private final SettingsService settingsService;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${security.otp.threshold:50000.00}")
    private BigDecimal otpThreshold;
    
    private static final SecureRandom RANDOM = new SecureRandom();
    
    @Override
    public boolean isOtpRequired(String operationType, BigDecimal amount, boolean isNewBeneficiary) {
        if (operationType == null) {
            return false;
        }

        // Operations that unconditionally require OTP verification
        String[] alwaysRequireOtp = {
            "PASSWORD_CHANGE",
            "EMAIL_CHANGE",
            "PHONE_CHANGE",
            "SECURITY_SETTINGS_CHANGE",
            "CARD_PAYMENT",
            "BENEFICIARY_ADD",
            "LOAN_APPLICATION",
            "LOGIN_STEPUP"
        };
        
        for (String op : alwaysRequireOtp) {
            if (op.equalsIgnoreCase(operationType)) {
                return true;
            }
        }
        
        // OTP required for transfers to new/external beneficiary
        if ("FUND_TRANSFER".equalsIgnoreCase(operationType) && isNewBeneficiary) {
            return true;
        }
        
        // OTP required for high-value transfers exceeding threshold
        if ("FUND_TRANSFER".equalsIgnoreCase(operationType) && amount != null) {
            return amount.compareTo(otpThreshold) > 0;
        }
        
        return false;
    }
    
    @Override
    public BigDecimal getOtpThreshold() {
        return otpThreshold;
    }
    
    @Override
    @Transactional
    public String generateAndSendOtp(String email, String operationType) {
        int maxResend = settingsService.otpMaxResend();
        long recent = otpVerificationRepository.countByEmailAndCreatedAtAfter(
                email, LocalDateTime.now().minusMinutes(15));
        
        if (recent >= maxResend) {
            throw new IllegalArgumentException(
                    "OTP resend limit reached. Please wait 15 minutes before requesting a new OTP.");
        }
        
        // Invalidate any previous OTP
        invalidateOtp(email);
        
        // Generate cryptographically secure 6-digit OTP
        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        String hashedOtp = passwordEncoder.encode(otp);
        
        // Create OTP verification record
        OtpVerification verification = OtpVerification.builder()
                .email(email)
                .otpHash(hashedOtp)
                .operationType(operationType)
                .expiresAt(LocalDateTime.now().plusMinutes(settingsService.otpExpiryMinutes()))
                .attempts(0)
                .verified(false)
                .createdAt(LocalDateTime.now())
                .build();
        otpVerificationRepository.save(verification);
        
        // Get recipient user
        User recipient = userRepository.findByEmail(email).orElse(null);
        if (recipient == null) {
            recipient = User.builder()
                    .email(email)
                    .fullName("Valued Customer")
                    .build();
        }
        
        // Send OTP exclusively via email
        String subject = "Your SmartBank Connect Verification Code";
        String operationLabel = getOperationLabel(operationType);
        String html = EmailTemplates.otp(recipient.getFullName(), otp, operationLabel, 
                settingsService.otpExpiryMinutes());
        
        // Log secure audit message and sandbox dev code
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
        logger.info("[OTP SECURITY] Verification OTP generated and dispatched via email for operation '{}' (expires in {} mins).",
                operationType, settingsService.otpExpiryMinutes());
        logger.info("[DEV/SANDBOX OTP] Verification code for {}: {}", email, otp);
        
        notificationService.sendHtml(recipient, subject, html,
                "Your verification code is: " + otp + "\nValid for " + 
                settingsService.otpExpiryMinutes() + " minutes.\nDo not share this code.");
        
        return otp;
    }
    
    @Override
    @Transactional
    public String resendOtp(String email, String operationType) {
        // Invalidate previous OTP
        OtpVerification previous = otpVerificationRepository
                .findFirstByEmailOrderByCreatedAtDesc(email)
                .orElse(null);
        
        if (previous != null && !previous.isVerified()) {
            previous.setExpiresAt(LocalDateTime.now()); // Invalidate immediately
            otpVerificationRepository.save(previous);
        }
        
        // Generate and send new OTP
        return generateAndSendOtp(email, operationType);
    }
    
    @Override
    @Transactional
    public boolean verifyOtp(String email, String otp) {
        OtpVerification verification = otpVerificationRepository
                .findFirstByEmailOrderByCreatedAtDesc(email)
                .orElse(null);
        
        if (verification == null) {
            throw new IllegalArgumentException("No OTP was requested for this email address.");
        }
        
        if (verification.isVerified()) {
            throw new IllegalArgumentException("This OTP has already been used. Please request a new OTP.");
        }
        
        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired. Please request a new OTP.");
        }
        
        int maxAttempts = settingsService.otpMaxAttempts();
        if (verification.getAttempts() >= maxAttempts) {
            verification.setExpiresAt(LocalDateTime.now()); // Invalidate
            otpVerificationRepository.save(verification);
            throw new IllegalArgumentException(
                    "Maximum OTP verification attempts exceeded. Please request a new OTP.");
        }
        
        // Increment attempt counter
        verification.setAttempts(verification.getAttempts() + 1);
        otpVerificationRepository.save(verification);
        
        // Verify OTP
        boolean matches = passwordEncoder.matches(otp, verification.getOtpHash());
        if (matches) {
            verification.setVerified(true);
            verification.setVerifiedAt(LocalDateTime.now());
            otpVerificationRepository.save(verification);
            return true;
        }
        
        return false;
    }
    
    @Override
    public int getOtpRemainingAttempts(String email) {
        OtpVerification verification = otpVerificationRepository
                .findFirstByEmailOrderByCreatedAtDesc(email)
                .orElse(null);
        
        if (verification == null) {
            return -1;
        }
        
        int maxAttempts = settingsService.otpMaxAttempts();
        int remaining = maxAttempts - verification.getAttempts();
        return Math.max(0, remaining);
    }
    
    @Override
    public boolean hasOtpExpired(String email) {
        OtpVerification verification = otpVerificationRepository
                .findFirstByEmailOrderByCreatedAtDesc(email)
                .orElse(null);
        
        if (verification == null) {
            return true;
        }
        
        return verification.getExpiresAt().isBefore(LocalDateTime.now());
    }
    
    @Override
    @Transactional
    public void invalidateOtp(String email) {
        otpVerificationRepository.findFirstByEmailOrderByCreatedAtDesc(email)
                .ifPresent(otp -> {
                    if (!otp.isVerified()) {
                        otp.setExpiresAt(LocalDateTime.now());
                        otpVerificationRepository.save(otp);
                    }
                });
    }
    
    @Override
    public int getOtpExpiryMinutes() {
        return settingsService.otpExpiryMinutes();
    }
    
    @Override
    public int getMaxOtpAttempts() {
        return settingsService.otpMaxAttempts();
    }
    
    @Override
    public int getMaxOtpResend() {
        return settingsService.otpMaxResend();
    }
    
    private String getOperationLabel(String operationType) {
        return switch (operationType.toUpperCase()) {
            case "FUND_TRANSFER" -> "Fund Transfer";
            case "CARD_PAYMENT" -> "Card Payment";
            case "PASSWORD_CHANGE" -> "Password Change";
            case "EMAIL_CHANGE" -> "Email Address Change";
            case "PHONE_CHANGE" -> "Phone Number Change";
            case "BENEFICIARY_ADD" -> "Add New Beneficiary";
            case "LOAN_APPLICATION" -> "Loan Application";
            case "SECURITY_SETTINGS_CHANGE" -> "Security Settings Change";
            default -> "Security Verification";
        };
    }
}


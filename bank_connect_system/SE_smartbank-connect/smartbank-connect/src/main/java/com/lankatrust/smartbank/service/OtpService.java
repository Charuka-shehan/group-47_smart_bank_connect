package com.lankatrust.smartbank.service;

import java.math.BigDecimal;

/**
 * Centralized OTP Service for sensitive operations.
 * Handles OTP generation, verification, and management for all high-risk transactions.
 */
public interface OtpService {

    /**
     * Determines if an operation requires OTP verification.
     *
     * @param operationType Type of operation (e.g., FUND_TRANSFER, BENEFICIARY_ADD, PAYMENT, PASSWORD_CHANGE, EMAIL_CHANGE, etc.)
     * @param amount Transaction amount (if applicable)
     * @param isNewBeneficiary Whether the beneficiary is newly added
     * @return true if OTP is required
     */
    boolean isOtpRequired(String operationType, BigDecimal amount, boolean isNewBeneficiary);

    /**
     * Get the OTP threshold amount for fund transfers.
     * Transfers above this amount require OTP.
     */
    BigDecimal getOtpThreshold();

    /**
     * Generate and send OTP for the given email.
     *
     * @param email Recipient email address (must be registered in the system)
     * @param operationType Type of operation requiring OTP
     * @return Generated OTP (only in development/sandbox mode for testing)
     */
    String generateAndSendOtp(String email, String operationType);

    /**
     * Resend OTP to the given email.
     * Respects rate limiting and resend attempt limits.
     *
     * @param email Recipient email address
     * @param operationType Type of operation requiring OTP
     * @return Generated OTP (only in development/sandbox mode for testing)
     */
    String resendOtp(String email, String operationType);

    /**
     * Verify OTP for the given email.
     *
     * @param email Email address of the user
     * @param otp Six-digit OTP code provided by user
     * @return true if OTP is valid and verified; false otherwise
     * @throws IllegalArgumentException if OTP is expired, already used, or max attempts exceeded
     */
    boolean verifyOtp(String email, String otp);

    /**
     * Check remaining OTP attempts before lockout.
     *
     * @param email Email address of the user
     * @return Number of remaining attempts (-1 if no OTP requested yet)
     */
    int getOtpRemainingAttempts(String email);

    /**
     * Check if OTP has expired for the given email.
     *
     * @param email Email address of the user
     * @return true if OTP has expired
     */
    boolean hasOtpExpired(String email);

    /**
     * Invalidate OTP for the given email (e.g., after successful verification or lockout).
     *
     * @param email Email address of the user
     */
    void invalidateOtp(String email);

    /**
     * Get OTP expiry time in minutes (from settings).
     */
    int getOtpExpiryMinutes();

    /**
     * Get maximum OTP verification attempts allowed.
     */
    int getMaxOtpAttempts();

    /**
     * Get maximum OTP resend attempts within time window.
     */
    int getMaxOtpResend();
}


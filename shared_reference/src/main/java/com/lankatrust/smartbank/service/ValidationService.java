package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.entity.Account;
import com.lankatrust.smartbank.entity.User;
import java.math.BigDecimal;

/**
 * Centralized Validation Service for SmartBank Connect.
 * Provides comprehensive validation for all business operations.
 */
public interface ValidationService {
    
    // ACCOUNT VALIDATION
    
    /**
     * Validate that account exists and belongs to the authenticated customer.
     */
    void validateAccountOwnership(Account account, User authenticatedUser);
    
    /**
     * Validate account status for transactions.
     * Ensures account is ACTIVE and not FROZEN or CLOSED.
     */
    void validateAccountStatus(Account account, String operationType);
    
    /**
     * Validate that account has sufficient balance for transaction.
     */
    void validateSufficientBalance(Account account, BigDecimal amount);
    
    /**
     * Validate transaction limit compliance.
     */
    void validateTransactionLimit(Account account, BigDecimal amount, String transactionType);
    
    // TRANSFER VALIDATION
    
    /**
     * Validate source and destination accounts for fund transfer.
     */
    void validateTransfer(Account source, Account destination, BigDecimal amount, User authenticatedUser);
    
    /**
     * Validate beneficiary account exists and is active.
     */
    void validateBeneficiary(Account beneficiary);
    
    /**
     * Validate that destination account is not the same as source.
     */
    void validateDestinationNotSource(Long sourceId, Long destinationId);
    
    /**
     * Validate beneficiary name format.
     */
    void validateBeneficiaryName(String beneficiaryName);
    
    // AMOUNT VALIDATION
    
    /**
     * Validate amount is positive and within acceptable range.
     */
    void validateAmount(BigDecimal amount);
    
    /**
     * Validate amount meets minimum threshold for account type.
     */
    void validateMinimumAmount(BigDecimal amount, String operationType);
    
    /**
     * Validate amount does not exceed daily/monthly transaction limits.
     */
    void validateAmountLimit(BigDecimal amount, String accountType, String operationType);
    
    // USER/PROFILE VALIDATION
    
    /**
     * Validate email address format.
     */
    void validateEmail(String email);
    
    /**
     * Validate phone number format.
     */
    void validatePhoneNumber(String phoneNumber);
    
    /**
     * Validate NIC (National Identity Card) format.
     */
    void validateNic(String nic);
    
    /**
     * Validate password meets security requirements.
     */
    void validatePassword(String password);
    
    /**
     * Validate passwords match.
     */
    void validatePasswordMatch(String password, String confirmPassword);
    
    // LOAN VALIDATION
    
    /**
     * Validate loan application amount.
     */
    void validateLoanAmount(BigDecimal amount);
    
    /**
     * Validate loan term in months.
     */
    void validateLoanTerm(Integer termMonths);
    
    /**
     * Validate loan purpose description.
     */
    void validateLoanPurpose(String purpose);
    
    /**
     * Validate applicant eligibility for loan.
     */
    void validateLoanApplicantEligibility(User applicant);
    
    // FILE VALIDATION
    
    /**
     * Validate uploaded file for security and compliance.
     */
    void validateUploadedFile(org.springframework.web.multipart.MultipartFile file, String fileType);
    
    /**
     * Validate file type is allowed.
     */
    void validateFileType(String filename, String[] allowedTypes);
    
    /**
     * Validate file size within limits.
     */
    void validateFileSize(long fileSize, long maxSizeBytes);
    
    // DUPLICATE/UNIQUENESS VALIDATION
    
    /**
     * Validate email is not already registered.
     */
    void validateEmailNotExists(String email);
    
    /**
     * Validate NIC is not already registered.
     */
    void validateNicNotExists(String nic);
    
    /**
     * Validate account number is unique.
     */
    void validateAccountNumberUnique(String accountNumber);
    
    /**
     * Validate email is not already registered (except for given user ID).
     */
    void validateEmailNotExistsExcept(String email, Long excludeUserId);

    // GENERAL / DATE / SETTINGS VALIDATION

    /**
     * Validate required field is non-null and not blank.
     */
    void validateRequired(String value, String fieldName);

    /**
     * Validate date is not in the future.
     */
    void validateDateNotFuture(java.time.LocalDate date, String fieldName);

    /**
     * Validate age meets minimum age requirement.
     */
    void validateAge(java.time.LocalDate dob, int minAge);

    /**
     * Validate date range (start <= end).
     */
    void validateDateRange(java.time.LocalDate start, java.time.LocalDate end);

    /**
     * Validate loan monthly income.
     */
    void validateLoanIncome(BigDecimal monthlyIncome);

    /**
     * Validate initial deposit meets minimum requirement for account type.
     */
    void validateInitialDeposit(String accountType, BigDecimal deposit);

    /**
     * Validate admin security parameters are within sensible operational bounds.
     */
    void validateAdminSettings(int otpExpiryMinutes, int otpMaxAttempts, int otpMaxResend, int sessionTimeoutMinutes, int maxFailedLogins);
}


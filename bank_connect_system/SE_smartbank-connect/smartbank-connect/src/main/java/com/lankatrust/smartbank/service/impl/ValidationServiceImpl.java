package com.lankatrust.smartbank.service.impl;

import com.lankatrust.smartbank.entity.*;
import com.lankatrust.smartbank.repository.*;
import com.lankatrust.smartbank.service.ValidationService;
import com.lankatrust.smartbank.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Implementation of comprehensive Validation Service.
 * Centralized validation for all business operations.
 */
@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    @Value("${security.file.maxsize:5242880}") // 5MB default
    private long maxFileSize;

    // Regex patterns for validation
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9\\s\\-\\+\\(\\)]{10,20}$");
    private static final Pattern NIC_PATTERN =
            Pattern.compile("^[0-9]{9}[VvXx]$|^[0-9]{12}$"); // Sri Lankan NIC format
    private static final Pattern BENEFICIARY_NAME_PATTERN =
            Pattern.compile("^[a-zA-Z\\s\\-']{3,100}$");

    // ACCOUNT VALIDATION

    @Override
    public void validateAccountOwnership(Account account, User authenticatedUser) {
        if (account == null) {
            throw new IllegalArgumentException("Account does not exist.");
        }
        if (authenticatedUser != null &&
                (authenticatedUser.getRole() == com.lankatrust.smartbank.entity.Role.BANK_MANAGER ||
                        authenticatedUser.getRole() == com.lankatrust.smartbank.entity.Role.SYSTEM_ADMINISTRATOR)) {
            return;
        }
        if (!account.getCustomer().getId().equals(authenticatedUser.getId())) {
            throw new IllegalArgumentException("You do not have permission to access this account.");
        }
    }

    @Override
    public void validateAccountStatus(Account account, String operationType) {
        if (account.getStatus() == null) {
            throw new IllegalStateException("Account status is invalid.");
        }

        switch (account.getStatus()) {
            case ACTIVE:
                // Account is active, proceed
                break;
            case PENDING_APPROVAL:
                throw new IllegalStateException("Account is not yet active. Please wait for manager approval.");
            case FROZEN:
                throw new IllegalStateException("Account is frozen. Transactions cannot be performed.");
            case CLOSED:
                throw new IllegalStateException("Account is closed. Transactions cannot be performed.");
            case REJECTED:
                throw new IllegalStateException("Account has been rejected. Please contact support.");
            default:
                throw new IllegalStateException("Account status is invalid: " + account.getStatus());
        }
    }

    @Override
    public void validateSufficientBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException(
                    "Insufficient balance. Available: Rs. " + account.getBalance() +
                            ", Required: Rs. " + amount);
        }
    }

    @Override
    public void validateTransactionLimit(Account account, BigDecimal amount, String transactionType) {
        // Define limits per account type
        BigDecimal dailyLimit, monthlyLimit;

        switch (account.getAccountType()) {
            case "SAVINGS":
                dailyLimit = new BigDecimal("500000.00");
                monthlyLimit = new BigDecimal("5000000.00");
                break;
            case "CURRENT":
                dailyLimit = new BigDecimal("2000000.00");
                monthlyLimit = new BigDecimal("20000000.00");
                break;
            case "FIXED_DEPOSIT":
                dailyLimit = new BigDecimal("100000.00");
                monthlyLimit = new BigDecimal("1000000.00");
                break;
            default:
                dailyLimit = new BigDecimal("100000.00");
                monthlyLimit = new BigDecimal("1000000.00");
        }

        if (amount.compareTo(dailyLimit) > 0) {
            throw new IllegalStateException(
                    "Transaction exceeds daily limit of Rs. " + dailyLimit);
        }

        if (amount.compareTo(monthlyLimit) > 0) {
            throw new IllegalStateException(
                    "Transaction exceeds monthly limit of Rs. " + monthlyLimit);
        }
    }

    // TRANSFER VALIDATION

    @Override
    public void validateTransfer(Account source, Account destination, BigDecimal amount, User authenticatedUser) {
        validateAccountOwnership(source, authenticatedUser);
        validateAccountStatus(source, "TRANSFER");
        validateSufficientBalance(source, amount);
        validateTransactionLimit(source, amount, "TRANSFER");
        validateAmount(amount);

        if (destination != null) {
            validateBeneficiary(destination);
            validateDestinationNotSource(source.getId(), destination.getId());
        }
    }

    @Override
    public void validateBeneficiary(Account beneficiary) {
        if (beneficiary == null) {
            throw new IllegalArgumentException("Beneficiary account does not exist.");
        }
        if (beneficiary.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Beneficiary account is not active.");
        }
    }

    @Override
    public void validateDestinationNotSource(Long sourceId, Long destinationId) {
        if (sourceId.equals(destinationId)) {
            throw new IllegalArgumentException("Source and destination accounts must be different.");
        }
    }

    @Override
    public void validateBeneficiaryName(String beneficiaryName) {
        if (beneficiaryName == null || beneficiaryName.isBlank()) {
            throw new IllegalArgumentException("Beneficiary name is required.");
        }
        if (!BENEFICIARY_NAME_PATTERN.matcher(beneficiaryName.trim()).matches()) {
            throw new IllegalArgumentException(
                    "Beneficiary name must contain only letters, spaces, hyphens and apostrophes (3-100 characters).");
        }
    }

    // AMOUNT VALIDATION

    @Override
    public void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than Rs. 0.");
        }
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("Amount can have maximum 2 decimal places.");
        }
        if (amount.compareTo(new BigDecimal("999999999.99")) > 0) {
            throw new IllegalArgumentException("Amount exceeds maximum limit of Rs. 999,999,999.99");
        }
    }

    @Override
    public void validateMinimumAmount(BigDecimal amount, String operationType) {
        BigDecimal minimum = new BigDecimal("0.01");

        if ("TRANSFER".equalsIgnoreCase(operationType)) {
            minimum = new BigDecimal("10.00");
        } else if ("LOAN_APPLICATION".equalsIgnoreCase(operationType)) {
            minimum = new BigDecimal("10000.00");
        }

        if (amount.compareTo(minimum) < 0) {
            throw new IllegalArgumentException(
                    "Minimum " + operationType + " amount is Rs. " + minimum);
        }
    }

    @Override
    public void validateAmountLimit(BigDecimal amount, String accountType, String operationType) {
        // Delegated to validateTransactionLimit
        // This method can be used for specific operation types if needed
    }

    // USER/PROFILE VALIDATION

    @Override
    public void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email address is required.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        if (email.length() > 150) {
            throw new IllegalArgumentException("Email address is too long (max 150 characters).");
        }
    }

    @Override
    public void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number is required.");
        }
        if (!PHONE_PATTERN.matcher(phoneNumber.trim()).matches()) {
            throw new IllegalArgumentException(
                    "Please enter a valid phone number (10-20 digits, spaces, dashes, or parentheses).");
        }
    }

    @Override
    public void validateNic(String nic) {
        if (nic == null || nic.isBlank()) {
            throw new IllegalArgumentException("NIC is required.");
        }
        if (!NIC_PATTERN.matcher(nic.trim().toUpperCase()).matches()) {
            throw new IllegalArgumentException(
                    "Invalid NIC format. Please enter a valid Sri Lankan NIC (9 digits + V, or 12 digits).");
        }
    }

    @Override
    public void validatePassword(String password) {
        if (!PasswordValidator.isValid(password)) {
            throw new IllegalArgumentException(PasswordValidator.getRequirements());
        }
    }

    @Override
    public void validatePasswordMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            throw new IllegalArgumentException("Passwords are required.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
    }

    // LOAN VALIDATION

    @Override
    public void validateLoanAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Loan amount is required.");
        }
        BigDecimal minimum = new BigDecimal("10000.00");
        BigDecimal maximum = new BigDecimal("50000000.00");

        if (amount.compareTo(minimum) < 0) {
            throw new IllegalArgumentException("Minimum loan amount is Rs. " + minimum);
        }
        if (amount.compareTo(maximum) > 0) {
            throw new IllegalArgumentException("Maximum loan amount is Rs. " + maximum);
        }
    }

    @Override
    public void validateLoanTerm(Integer termMonths) {
        if (termMonths == null) {
            throw new IllegalArgumentException("Loan term is required.");
        }
        if (termMonths < 3) {
            throw new IllegalArgumentException("Minimum loan term is 3 months.");
        }
        if (termMonths > 360) {
            throw new IllegalArgumentException("Maximum loan term is 30 years (360 months).");
        }
    }

    @Override
    public void validateLoanPurpose(String purpose) {
        if (purpose == null || purpose.isBlank()) {
            throw new IllegalArgumentException("Loan purpose is required.");
        }
        if (purpose.length() < 10) {
            throw new IllegalArgumentException("Loan purpose must be at least 10 characters.");
        }
        if (purpose.length() > 500) {
            throw new IllegalArgumentException("Loan purpose must not exceed 500 characters.");
        }
    }

    @Override
    public void validateLoanApplicantEligibility(User applicant) {
        if (applicant == null) {
            throw new IllegalArgumentException("Applicant not found.");
        }
        if (!applicant.isEnabled()) {
            throw new IllegalStateException("Your account is disabled. Please contact support.");
        }
        if (!(applicant instanceof Customer)) {
            throw new IllegalStateException("Only customers can apply for loans.");
        }
    }

    // FILE VALIDATION

    @Override
    public void validateUploadedFile(MultipartFile file, String fileType) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required.");
        }

        String[] allowedTypes = getAllowedFileTypes(fileType);
        validateFileType(file.getOriginalFilename(), allowedTypes);
        validateFileSize(file.getSize(), maxFileSize);

        // Additional virus/malware scanning can be added here
        // This is a placeholder for security scanning
    }

    @Override
    public void validateFileType(String filename, String[] allowedTypes) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename is invalid.");
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        boolean isAllowed = Arrays.stream(allowedTypes)
                .anyMatch(type -> type.equalsIgnoreCase(extension));

        if (!isAllowed) {
            throw new IllegalArgumentException(
                    "File type not allowed. Allowed types: " + String.join(", ", allowedTypes));
        }
    }

    @Override
    public void validateFileSize(long fileSize, long maxSizeBytes) {
        if (fileSize > maxSizeBytes) {
            throw new IllegalArgumentException(
                    "File size exceeds maximum allowed size of " + (maxSizeBytes / 1024 / 1024) + "MB.");
        }
    }

    // DUPLICATE/UNIQUENESS VALIDATION

    @Override
    public void validateEmailNotExists(String email) {
        validateEmail(email);
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }
    }

    @Override
    public void validateNicNotExists(String nic) {
        validateNic(nic);
        if (customerRepository.existsByNic(nic)) {
            throw new IllegalArgumentException("An account with this NIC already exists.");
        }
    }

    @Override
    public void validateAccountNumberUnique(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Account number is required.");
        }
        if (accountRepository.findByAccountNumber(accountNumber).isPresent()) {
            throw new IllegalArgumentException("This account number already exists.");
        }
    }

    @Override
    public void validateEmailNotExistsExcept(String email, Long excludeUserId) {
        validateEmail(email);
        boolean exists = userRepository.findByEmail(email)
                .map(user -> !user.getId().equals(excludeUserId))
                .orElse(false);

        if (exists) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }
    }

    // GENERAL / DATE / SETTINGS VALIDATION

    @Override
    public void validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }

    @Override
    public void validateDateNotFuture(java.time.LocalDate date, String fieldName) {
        if (date == null) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        if (date.isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException(fieldName + " cannot be in the future.");
        }
    }

    @Override
    public void validateAge(java.time.LocalDate dob, int minAge) {
        validateDateNotFuture(dob, "Date of birth");
        java.time.Period period = java.time.Period.between(dob, java.time.LocalDate.now());
        if (period.getYears() < minAge) {
            throw new IllegalArgumentException("Customer must be at least " + minAge + " years old.");
        }
    }

    @Override
    public void validateDateRange(java.time.LocalDate start, java.time.LocalDate end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }
    }

    @Override
    public void validateLoanIncome(BigDecimal monthlyIncome) {
        if (monthlyIncome == null || monthlyIncome.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Monthly income must be a positive amount.");
        }
    }

    @Override
    public void validateInitialDeposit(String accountType, BigDecimal deposit) {
        if (deposit == null || deposit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial deposit cannot be negative.");
        }
        BigDecimal min = switch (accountType != null ? accountType.toUpperCase() : "") {
            case "SAVINGS" -> new BigDecimal("1000.00");
            case "CURRENT" -> new BigDecimal("5000.00");
            case "FIXED_DEPOSIT" -> new BigDecimal("50000.00");
            default -> BigDecimal.ZERO;
        };
        if (deposit.compareTo(min) < 0) {
            throw new IllegalArgumentException("Minimum initial deposit for " + accountType + " is LKR " + min);
        }
    }

    @Override
    public void validateAdminSettings(int otpExpiryMinutes, int otpMaxAttempts, int otpMaxResend,
                                      int sessionTimeoutMinutes, int maxFailedLogins) {
        if (otpExpiryMinutes < 1 || otpExpiryMinutes > 60) {
            throw new IllegalArgumentException("OTP expiry must be between 1 and 60 minutes.");
        }
        if (otpMaxAttempts < 1 || otpMaxAttempts > 10) {
            throw new IllegalArgumentException("OTP max attempts must be between 1 and 10.");
        }
        if (otpMaxResend < 1 || otpMaxResend > 10) {
            throw new IllegalArgumentException("OTP max resends must be between 1 and 10.");
        }
        if (sessionTimeoutMinutes < 5 || sessionTimeoutMinutes > 1440) {
            throw new IllegalArgumentException("Session timeout must be between 5 and 1440 minutes.");
        }
        if (maxFailedLogins < 1 || maxFailedLogins > 20) {
            throw new IllegalArgumentException("Max failed logins must be between 1 and 20.");
        }
    }

    // HELPER METHODS

    private String[] getAllowedFileTypes(String fileType) {
        return switch (fileType.toUpperCase()) {
            case "DOCUMENT" -> new String[]{"pdf", "doc", "docx", "txt"};
            case "IMAGE" -> new String[]{"jpg", "jpeg", "png", "gif"};
            case "PROFILE_PHOTO" -> new String[]{"jpg", "jpeg", "png"};
            case "NIC_SCAN" -> new String[]{"pdf", "jpg", "jpeg", "png"};
            case "SIGNATURE" -> new String[]{"pdf", "jpg", "jpeg", "png"};
            default -> new String[]{"pdf", "doc", "docx", "txt", "jpg", "jpeg", "png"};
        };
    }
}


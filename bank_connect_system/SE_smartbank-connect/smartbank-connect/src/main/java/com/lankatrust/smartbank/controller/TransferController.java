package com.lankatrust.smartbank.controller;

import com.lankatrust.smartbank.entity.Account;
import com.lankatrust.smartbank.entity.User;
import com.lankatrust.smartbank.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/transfer")
@PreAuthorize("hasAnyRole('CUSTOMER','BANK_MANAGER')")
public class TransferController {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final UserService userService;
    private final BeneficiaryService beneficiaryService;
    private final ValidationService validationService;
    private final OtpService otpService;

    @GetMapping
    public String transferForm(Authentication authentication, Model model) {
        try {
            User customer = userService.getByEmail(authentication.getName());
            model.addAttribute("accounts", getAccountsForUser(customer));
            model.addAttribute("beneficiaries", beneficiaryService.getForUser(customer.getId()));
            model.addAttribute("otpThreshold", otpService.getOtpThreshold());
            return "transfer/form";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            return "transfer/form";
        }
    }

    @PostMapping
    public String initiate(@RequestParam Long sourceAccountId,
                            @RequestParam(required = false) String destinationAccountId,
                            @RequestParam(required = false) String beneficiaryName,
                            @RequestParam BigDecimal amount,
                            @RequestParam(required = false) String remarks,
                            Authentication authentication,
                            Model model) {
        try {
            // Validate inputs
            if (sourceAccountId == null) {
                throw new IllegalArgumentException("Source account is required.");
            }
            validationService.validateAmount(amount);
            if (beneficiaryName != null && !beneficiaryName.isBlank()) {
                validationService.validateBeneficiaryName(beneficiaryName);
            }
            
            // Get source account
            Account sourceAccount = accountService.getById(sourceAccountId);
            User customer = userService.getByEmail(authentication.getName());
            
            // Validate account ownership and status
            validationService.validateAccountOwnership(sourceAccount, customer);
            validationService.validateAccountStatus(sourceAccount, "TRANSFER");
            validationService.validateSufficientBalance(sourceAccount, amount);
            validationService.validateTransactionLimit(sourceAccount, amount, "TRANSFER");
            
            // Get destination account if specified
            Account destAccount = null;
            if (destinationAccountId != null && !destinationAccountId.isBlank()) {
                String destInput = destinationAccountId.trim();
                destAccount = accountService.getAll().stream()
                        .filter(a -> destInput.equalsIgnoreCase(a.getAccountNumber()))
                        .findFirst()
                        .orElse(null);
                if (destAccount == null) {
                    try {
                        Long destId = Long.parseLong(destInput);
                        destAccount = accountService.getById(destId);
                    } catch (Exception ex) {
                        throw new IllegalArgumentException("Destination account not found: " + destInput);
                    }
                }
                validationService.validateDestinationNotSource(sourceAccountId, destAccount.getId());
                validationService.validateBeneficiary(destAccount);
            }
            
            // Initiate transfer
            var txn = transactionService.initiateTransfer(sourceAccountId, destAccount != null ? destAccount.getId() : null,
                    beneficiaryName, amount, remarks);
            
            // Check if OTP is required
            boolean otpRequired = otpService.isOtpRequired("FUND_TRANSFER", amount, destAccount == null);
            if (otpRequired) {
                // Generate and send OTP
                otpService.generateAndSendOtp(customer.getEmail(), "FUND_TRANSFER");
                model.addAttribute("transactionId", txn.getId());
                model.addAttribute("otpExpiryMinutes", otpService.getOtpExpiryMinutes());
                return "transfer/otp";
            }
            
            // Transfer completed without OTP
            model.addAttribute("transaction", txn);
            return "transfer/success";
            
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("errorField", "general");
            return returnTransferForm(authentication, model);
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            return returnTransferForm(authentication, model);
        }
    }

    @PostMapping("/otp")
    public String verifyOtp(@RequestParam Long transactionId,
                             @RequestParam String otp,
                             Authentication authentication,
                             Model model) {
        try {
            // Validate OTP input
            if (otp == null || otp.isBlank()) {
                throw new IllegalArgumentException("OTP is required.");
            }
            if (!otp.matches("\\d{6}")) {
                throw new IllegalArgumentException("OTP must be a 6-digit code.");
            }
            
            User customer = userService.getByEmail(authentication.getName());
            
            // Complete transfer with verified OTP (verifyTransferOtp validates OTP via OtpService)
            var txn = transactionService.verifyTransferOtp(transactionId, customer.getEmail(), otp);
            model.addAttribute("transaction", txn);
            return "transfer/success";
            
        } catch (IllegalArgumentException | IllegalStateException ex) {
            try {
                User customer = userService.getByEmail(authentication.getName());
                int remaining = otpService.getOtpRemainingAttempts(customer.getEmail());
                if (remaining > 0) {
                    model.addAttribute("error", ex.getMessage() + " (" + remaining + " attempt(s) remaining).");
                } else {
                    model.addAttribute("error", ex.getMessage());
                }
            } catch (Exception ignored) {
                model.addAttribute("error", ex.getMessage());
            }
            model.addAttribute("transactionId", transactionId);
            model.addAttribute("otpExpiryMinutes", otpService.getOtpExpiryMinutes());
            return "transfer/otp";
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(TransferController.class).error("Transfer OTP error: ", ex);
            model.addAttribute("error", "An error occurred during verification. Please try again.");
            model.addAttribute("transactionId", transactionId);
            model.addAttribute("otpExpiryMinutes", otpService.getOtpExpiryMinutes());
            return "transfer/otp";
        }
    }
    
    @PostMapping("/otp/resend")
    public String resendOtp(@RequestParam Long transactionId,
                            Authentication authentication,
                            Model model) {
        try {
            User customer = userService.getByEmail(authentication.getName());
            otpService.resendOtp(customer.getEmail(), "FUND_TRANSFER");
            model.addAttribute("success", "New OTP has been sent to your registered email address.");
            model.addAttribute("transactionId", transactionId);
            model.addAttribute("otpExpiryMinutes", otpService.getOtpExpiryMinutes());
            return "transfer/otp";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("transactionId", transactionId);
            model.addAttribute("otpExpiryMinutes", otpService.getOtpExpiryMinutes());
            return "transfer/otp";
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(TransferController.class).error("Transfer OTP resend error: ", ex);
            model.addAttribute("error", "Unable to resend OTP at this time. Please try again.");
            model.addAttribute("transactionId", transactionId);
            model.addAttribute("otpExpiryMinutes", otpService.getOtpExpiryMinutes());
            return "transfer/otp";
        }
    }
    
    private String returnTransferForm(Authentication authentication, Model model) {
        try {
            User customer = userService.getByEmail(authentication.getName());
            model.addAttribute("accounts", getAccountsForUser(customer));
            model.addAttribute("beneficiaries", beneficiaryService.getForUser(customer.getId()));
            model.addAttribute("otpThreshold", otpService.getOtpThreshold());
            return "transfer/form";
        } catch (Exception ex) {
            if (!model.containsAttribute("error")) {
                model.addAttribute("error", "An error occurred. Please try again.");
            }
            return "transfer/form";
        }
    }

    private java.util.List<Account> getAccountsForUser(User user) {
        java.util.List<Account> accounts = accountService.getForCustomer(user.getId());
        if (accounts.isEmpty() || user.getRole() == com.lankatrust.smartbank.entity.Role.BANK_MANAGER || user.getRole() == com.lankatrust.smartbank.entity.Role.SYSTEM_ADMINISTRATOR) {
            accounts = accountService.getAll();
        }
        return accounts;
    }
}


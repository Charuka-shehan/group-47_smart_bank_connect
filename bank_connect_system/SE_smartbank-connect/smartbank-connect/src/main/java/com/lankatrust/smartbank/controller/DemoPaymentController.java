package com.lankatrust.smartbank.controller;

import com.lankatrust.smartbank.entity.*;
import com.lankatrust.smartbank.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/demo/payment")
@PreAuthorize("hasAnyRole('CUSTOMER','BANK_MANAGER')")
public class DemoPaymentController {

    private final AccountService accountService;
    private final AtmCardService atmCardService;
    private final UserService userService;
    private final TransactionService transactionService;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final ValidationService validationService;
    private final OtpService otpService;

    @GetMapping
    public String paymentForm(Authentication authentication, Model model) {
        try {
            User customer = userService.getByEmail(authentication.getName());
            model.addAttribute("accounts", getAccountsForUser(customer));
            model.addAttribute("isDemo", true);
            model.addAttribute("demoLabel", "Demo / Simulated Card Payment");
            return "demo/payment";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            return "demo/payment";
        }
    }

    @PostMapping
    public String processPayment(@RequestParam Long accountId,
                                 @RequestParam String cardNumber,
                                 @RequestParam String cvv,
                                 @RequestParam String merchantName,
                                 @RequestParam BigDecimal amount,
                                 Authentication authentication,
                                 Model model) {
        try {
            // Validate inputs
            validationService.validateAmount(amount);
            if (merchantName == null || merchantName.isBlank()) {
                throw new IllegalArgumentException("Merchant name is required.");
            }
            if (cardNumber == null || cardNumber.isBlank()) {
                throw new IllegalArgumentException("Card number is required.");
            }
            if (cvv == null || cvv.isBlank()) {
                throw new IllegalArgumentException("CVV is required.");
            }

            // Get and validate account
            Account account = accountService.getById(accountId);
            User customer = userService.getByEmail(authentication.getName());
            validationService.validateAccountOwnership(account, customer);
            validationService.validateAccountStatus(account, "PAYMENT");
            validationService.validateSufficientBalance(account, amount);
            validationService.validateTransactionLimit(account, amount, "PAYMENT");

            // Get and validate card
            AtmCard card = atmCardService.findByCardNumber(cardNumber.replaceAll("\\s", ""))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid card number."));

            if (!card.getAccount().getId().equals(account.getId())) {
                throw new IllegalArgumentException("Card does not belong to selected account.");
            }
            if (!card.getStatus().equals("ACTIVE")) {
                throw new IllegalArgumentException("Card is blocked or expired.");
            }
            if (card.getExpiryDate().isBefore(LocalDate.now())) {
                throw new IllegalStateException("Card has expired.");
            }
            if (!passwordEncoder.matches(cvv, card.getCvvHash())) {
                throw new IllegalArgumentException("Invalid CVV.");
            }

            // Check if OTP is required for this payment
            boolean otpRequired = otpService.isOtpRequired("CARD_PAYMENT", amount, false);
            if (otpRequired) {
                // Generate and send OTP, then redirect to OTP verification
                otpService.generateAndSendOtp(customer.getEmail(), "CARD_PAYMENT");
                model.addAttribute("accountId", accountId);
                model.addAttribute("cardNumber", cardNumber);
                model.addAttribute("merchantName", merchantName);
                model.addAttribute("amount", amount);
                model.addAttribute("otpExpiryMinutes", otpService.getOtpExpiryMinutes());
                model.addAttribute("isDemo", true);
                model.addAttribute("demoLabel", "Demo / Simulated Card Payment");
                return "demo/payment-otp";
            }

            // Process payment directly (low-value, no OTP required)
            Transaction txn = transactionService.recordDemoPayment(accountId, merchantName, amount);

            notificationService.send(account.getCustomer(), "Demo Payment Receipt",
                    "You paid LKR " + amount + " to " + merchantName + " (Demo). Ref: " + txn.getReferenceNumber(),
                    NotificationChannel.EMAIL);

            model.addAttribute("transaction", txn);
            model.addAttribute("merchantName", merchantName);
            return "demo/payment-success";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            try {
                User customer = userService.getByEmail(authentication.getName());
                model.addAttribute("accounts", getAccountsForUser(customer));
            } catch (Exception ignored) {}
            return "demo/payment";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            try {
                User customer = userService.getByEmail(authentication.getName());
                model.addAttribute("accounts", getAccountsForUser(customer));
            } catch (Exception ignored) {}
            return "demo/payment";
        }
    }

    @PostMapping("/otp")
    public String verifyPaymentOtp(@RequestParam Long accountId,
                                   @RequestParam String cardNumber,
                                   @RequestParam String merchantName,
                                   @RequestParam BigDecimal amount,
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

            // Verify OTP
            boolean verified = otpService.verifyOtp(customer.getEmail(), otp);
            if (!verified) {
                int remaining = otpService.getOtpRemainingAttempts(customer.getEmail());
                if (remaining > 0) {
                    model.addAttribute("error", "Invalid OTP. " + remaining + " attempt(s) remaining.");
                } else {
                    model.addAttribute("error", "Maximum OTP attempts exceeded. Please request a new OTP.");
                }
                model.addAttribute("accountId", accountId);
                model.addAttribute("cardNumber", cardNumber);
                model.addAttribute("merchantName", merchantName);
                model.addAttribute("amount", amount);
                model.addAttribute("otpExpiryMinutes", otpService.getOtpExpiryMinutes());
                model.addAttribute("isDemo", true);
                model.addAttribute("demoLabel", "Demo / Simulated Card Payment");
                return "demo/payment-otp";
            }

            // Process payment with verified OTP
            Transaction txn = transactionService.recordDemoPayment(accountId, merchantName, amount);
            Account account = accountService.getById(accountId);

            notificationService.send(account.getCustomer(), "Demo Payment Receipt",
                    "You paid LKR " + amount + " to " + merchantName + " (Demo). Ref: " + txn.getReferenceNumber(),
                    NotificationChannel.EMAIL);

            model.addAttribute("transaction", txn);
            model.addAttribute("merchantName", merchantName);
            model.addAttribute("demoLabel", "Demo / Simulated Card Payment");
            return "demo/payment-success";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("accountId", accountId);
            model.addAttribute("cardNumber", cardNumber);
            model.addAttribute("merchantName", merchantName);
            model.addAttribute("amount", amount);
            model.addAttribute("otpExpiryMinutes", otpService.getOtpExpiryMinutes());
            model.addAttribute("isDemo", true);
            model.addAttribute("demoLabel", "Demo / Simulated Card Payment");
            return "demo/payment-otp";
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(DemoPaymentController.class).error("Payment OTP error: ", ex);
            model.addAttribute("error", "An unexpected error occurred during verification. Please try again.");
            model.addAttribute("accountId", accountId);
            model.addAttribute("cardNumber", cardNumber);
            model.addAttribute("merchantName", merchantName);
            model.addAttribute("amount", amount);
            model.addAttribute("isDemo", true);
            model.addAttribute("demoLabel", "Demo / Simulated Card Payment");
            return "demo/payment-otp";
        }
    }

    @PostMapping("/otp/resend")
    public String resendPaymentOtp(@RequestParam Long accountId,
                                   @RequestParam String cardNumber,
                                   @RequestParam String merchantName,
                                   @RequestParam BigDecimal amount,
                                   Authentication authentication,
                                   Model model) {
        try {
            User customer = userService.getByEmail(authentication.getName());
            otpService.resendOtp(customer.getEmail(), "CARD_PAYMENT");
            model.addAttribute("success", "New OTP has been sent to your registered email address.");
            model.addAttribute("accountId", accountId);
            model.addAttribute("cardNumber", cardNumber);
            model.addAttribute("merchantName", merchantName);
            model.addAttribute("amount", amount);
            model.addAttribute("otpExpiryMinutes", otpService.getOtpExpiryMinutes());
            model.addAttribute("isDemo", true);
            model.addAttribute("demoLabel", "Demo / Simulated Card Payment");
            return "demo/payment-otp";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("accountId", accountId);
            model.addAttribute("cardNumber", cardNumber);
            model.addAttribute("merchantName", merchantName);
            model.addAttribute("amount", amount);
            model.addAttribute("isDemo", true);
            model.addAttribute("demoLabel", "Demo / Simulated Card Payment");
            return "demo/payment-otp";
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(DemoPaymentController.class).error("Payment OTP resend error: ", ex);
            model.addAttribute("error", "Unable to resend OTP at this time. Please try again.");
            model.addAttribute("accountId", accountId);
            model.addAttribute("cardNumber", cardNumber);
            model.addAttribute("merchantName", merchantName);
            model.addAttribute("amount", amount);
            model.addAttribute("isDemo", true);
            model.addAttribute("demoLabel", "Demo / Simulated Card Payment");
            return "demo/payment-otp";
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

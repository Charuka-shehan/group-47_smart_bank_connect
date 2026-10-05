package com.lankatrust.smartbank.service.impl;

import com.lankatrust.smartbank.entity.*;
import com.lankatrust.smartbank.repository.AccountRepository;
import com.lankatrust.smartbank.repository.ApprovalRequestRepository;
import com.lankatrust.smartbank.repository.TransactionCorrectionRepository;
import com.lankatrust.smartbank.repository.TransactionRepository;
import com.lankatrust.smartbank.service.AuditLogService;
import com.lankatrust.smartbank.service.NotificationService;
import com.lankatrust.smartbank.service.TransactionService;
import com.lankatrust.smartbank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Member 2 (Ayyash M. A. M.) - Fund Transfer, and
 * Member 3 (Sasna R. K. S.) - Transaction History.
 * Implements: customer selects accounts -> validate balance & beneficiary
 * -> check transaction limits -> OTP step-up for high-value transfers
 * -> funds moved -> notification sent (Section 6.2, Process Flow).
 */
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final TransactionCorrectionRepository transactionCorrectionRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final com.lankatrust.smartbank.service.OtpService otpService;

    private static final SecureRandom RANDOM = new SecureRandom();

    private String generateReference() {
        return "TXN" + System.currentTimeMillis() + RANDOM.nextInt(1000);
    }

    @Override
    @Transactional
    public Transaction initiateTransfer(Long sourceAccountId, Long destinationAccountId, String beneficiaryName,
                                         BigDecimal amount, String remarks) {
        Account source = accountRepository.findById(sourceAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));

        if (source.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Source account is not active.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive.");
        }
        if (source.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance.");
        }

        Account destination = null;
        if (destinationAccountId != null) {
            destination = accountRepository.findById(destinationAccountId)
                    .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));
        }

        boolean requiresOtp = otpService.isOtpRequired("FUND_TRANSFER", amount, destinationAccountId == null);

        Transaction txn = Transaction.builder()
                .referenceNumber(generateReference())
                .sourceAccount(source)
                .destinationAccount(destination)
                .beneficiaryName(beneficiaryName)
                .amount(amount)
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .remarks(remarks)
                .otpVerified(!requiresOtp)
                .build();
        Transaction saved = transactionRepository.save(txn);

        if (requiresOtp) {
            // Do not send OTP here; the controller is responsible for initiating OTP workflows.
            auditLogService.log(source.getCustomer().getEmail(), "TRANSFER_OTP_REQUIRED", "Transaction",
                    saved.getId(), "High-value transfer of Rs. " + amount + " requires OTP verification");
        } else {
            saved = completeTransfer(saved);
        }
        return saved;
    }

    @Override
    @Transactional
    public Transaction verifyTransferOtp(Long transactionId, String customerEmail, String otp) {
        Transaction txn = getById(transactionId);
        if (txn.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction is not pending OTP verification.");
        }
        boolean valid = otpService.verifyOtp(customerEmail, otp);
        if (!valid) {
            throw new IllegalStateException("Invalid or expired OTP.");
        }
        txn.setOtpVerified(true);
        return completeTransfer(txn);
    }

    private Transaction completeTransfer(Transaction txn) {
        Account source = txn.getSourceAccount();
        if (source.getBalance().compareTo(txn.getAmount()) < 0) {
            txn.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(txn);
            throw new IllegalStateException("Insufficient balance at settlement time.");
        }

        source.setBalance(source.getBalance().subtract(txn.getAmount()));
        accountRepository.save(source);

        if (txn.getDestinationAccount() != null) {
            Account destination = txn.getDestinationAccount();
            destination.setBalance(destination.getBalance().add(txn.getAmount()));
            accountRepository.save(destination);
        }

        txn.setStatus(TransactionStatus.COMPLETED);
        txn.setProcessedAt(LocalDateTime.now());
        Transaction saved = transactionRepository.save(txn);

        auditLogService.log(source.getCustomer().getEmail(), "TRANSFER_COMPLETED", "Transaction",
                saved.getId(), "Rs. " + saved.getAmount() + " transferred from " + source.getAccountNumber());

        notificationService.send(source.getCustomer(), "Transfer Successful",
                "Rs. " + saved.getAmount() + " sent from " + source.getAccountNumber()
                        + " (Ref: " + saved.getReferenceNumber() + ")",
                NotificationChannel.SMS);

        if (saved.getDestinationAccount() != null) {
            notificationService.send(saved.getDestinationAccount().getCustomer(), "Funds Received",
                    "Rs. " + saved.getAmount() + " credited to " + saved.getDestinationAccount().getAccountNumber(),
                    NotificationChannel.SMS);
        }
        return saved;
    }

    @Override
    @Transactional
    public Transaction cancelPendingTransfer(Long transactionId, String officerEmail) {
        Transaction txn = getById(transactionId);
        if (txn.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Only pending transfers can be cancelled.");
        }
        txn.setStatus(TransactionStatus.CANCELLED);
        Transaction saved = transactionRepository.save(txn);
        auditLogService.log(officerEmail, "TRANSFER_CANCELLED", "Transaction", saved.getId(),
                "Pending transfer " + saved.getReferenceNumber() + " cancelled by officer");
        return saved;
    }

    @Override
    public List<Transaction> historyForAccount(Long accountId) {
        return transactionRepository.findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(
                accountId, accountId);
    }

    @Override
    public List<Transaction> filterHistory(Long accountId, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByAccountIdAndCreatedAtBetween(accountId, start, end);
    }

    @Override
    public Transaction getById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));
    }

    @Override
    public List<Transaction> getRecentTransactions(int limit) {
        return transactionRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(Transaction::getCreatedAt).reversed())
                .limit(limit)
                .toList();
    }

    @Override
    @Transactional
    public Transaction recordDemoPayment(Long accountId, String merchantName, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance.");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction txn = Transaction.builder()
                .referenceNumber(generateReference())
                .sourceAccount(account)
                .beneficiaryName(merchantName)
                .amount(amount)
                .type(TransactionType.PAYMENT)
                .status(TransactionStatus.COMPLETED)
                .remarks("Demo card payment")
                .otpVerified(true)
                .processedAt(LocalDateTime.now())
                .build();
        Transaction saved = transactionRepository.save(txn);

        auditLogService.log(account.getCustomer().getEmail(), "DEMO_PAYMENT_COMPLETED", "Transaction",
                saved.getId(), "LKR " + amount + " paid to " + merchantName);
        notificationService.send(account.getCustomer(), "Demo Payment Successful",
                "You paid LKR " + amount + " to " + merchantName + " (Demo). Ref: " + saved.getReferenceNumber(),
                NotificationChannel.EMAIL);
        return saved;
    }

    @Override
    public List<Transaction> getPendingTransfers() {
        return transactionRepository.findByStatusOrderByCreatedAtDesc(TransactionStatus.PENDING);
    }

    @Override
    public List<Transaction> getAll() {
        return transactionRepository.findAll();
    }

    @Override
    public List<Transaction> findBetween(LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
    }

    @Override
    public BigDecimal todayWithdrawalTotal(Long accountId) {
        LocalDateTime start = LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.MAX);
        return transactionRepository.findBySourceAccountIdAndTypeAndCreatedAtBetween(
                        accountId, TransactionType.WITHDRAWAL, start, end)
                .stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional
    public TransactionCorrection requestCorrection(Long transactionId, String reason, String proposedRemarks,
                                                   BigDecimal proposedAmount, String officerEmail) {
        Transaction txn = getById(transactionId);
        User officer = userService.getByEmail(officerEmail);
        TransactionCorrection correction = TransactionCorrection.builder()
                .transaction(txn)
                .requestedBy(officer)
                .reason(reason)
                .proposedRemarks(proposedRemarks)
                .proposedAmount(proposedAmount)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        TransactionCorrection saved = transactionCorrectionRepository.save(correction);

        ApprovalRequest request = ApprovalRequest.builder()
                .requestType("TXN_CORRECTION")
                .targetId(saved.getId())
                .targetReference(txn.getReferenceNumber())
                .requester(officer)
                .status("PENDING")
                .remarks(reason)
                .requestedAt(LocalDateTime.now())
                .build();
        approvalRequestRepository.save(request);

        auditLogService.log(officerEmail, "TXN_CORRECTION_REQUESTED", "TransactionCorrection", saved.getId(), reason);
        return saved;
    }

    @Override
    @Transactional
    public TransactionCorrection approveCorrection(Long correctionId, String managerEmail) {
        TransactionCorrection correction = transactionCorrectionRepository.findById(correctionId)
                .orElseThrow(() -> new IllegalArgumentException("Correction request not found"));
        if (!"PENDING".equals(correction.getStatus())) {
            throw new IllegalStateException("Correction is not pending.");
        }
        Transaction txn = correction.getTransaction();
        if (correction.getProposedRemarks() != null && !correction.getProposedRemarks().isBlank()) {
            txn.setRemarks(correction.getProposedRemarks());
        }
        if (correction.getProposedAmount() != null
                && correction.getProposedAmount().compareTo(txn.getAmount()) != 0
                && txn.getStatus() == TransactionStatus.COMPLETED) {
            BigDecimal delta = correction.getProposedAmount().subtract(txn.getAmount());
            Account source = txn.getSourceAccount();
            source.setBalance(source.getBalance().subtract(delta));
            accountRepository.save(source);
            txn.setAmount(correction.getProposedAmount());
            txn.setRemarks((txn.getRemarks() != null ? txn.getRemarks() + " | " : "")
                    + "Corrected by manager " + managerEmail);
        }
        transactionRepository.save(txn);
        correction.setStatus("APPROVED");
        correction.setApprovedBy(userService.getByEmail(managerEmail));
        correction.setProcessedAt(LocalDateTime.now());
        TransactionCorrection saved = transactionCorrectionRepository.save(correction);
        auditLogService.log(managerEmail, "TXN_CORRECTION_APPROVED", "TransactionCorrection", saved.getId(),
                txn.getReferenceNumber());
        return saved;
    }

    @Override
    @Transactional
    public TransactionCorrection rejectCorrection(Long correctionId, String managerEmail, String remarks) {
        TransactionCorrection correction = transactionCorrectionRepository.findById(correctionId)
                .orElseThrow(() -> new IllegalArgumentException("Correction request not found"));
        correction.setStatus("REJECTED");
        correction.setApprovedBy(userService.getByEmail(managerEmail));
        correction.setProcessedAt(LocalDateTime.now());
        correction.setReason(correction.getReason() + " | Rejected: " + remarks);
        TransactionCorrection saved = transactionCorrectionRepository.save(correction);
        auditLogService.log(managerEmail, "TXN_CORRECTION_REJECTED", "TransactionCorrection", saved.getId(), remarks);
        return saved;
    }

    @Override
    public List<TransactionCorrection> getCorrections() {
        return transactionCorrectionRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional
    public Transaction updatePendingTransfer(Long transactionId, BigDecimal amount, String remarks,
                                             Long destinationAccountId, String officerEmail) {
        Transaction txn = getById(transactionId);
        if (txn.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Only pending transfers can be updated. Current status: " + txn.getStatus());
        }
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            if (txn.getSourceAccount().getBalance().compareTo(amount) < 0) {
                throw new IllegalStateException("Insufficient balance in source account for updated amount.");
            }
            txn.setAmount(amount);
        }
        if (remarks != null && !remarks.isBlank()) {
            txn.setRemarks(remarks.trim());
        }
        if (destinationAccountId != null) {
            Account dest = accountRepository.findById(destinationAccountId)
                    .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));
            txn.setDestinationAccount(dest);
        }
        Transaction saved = transactionRepository.save(txn);
        auditLogService.log(officerEmail, "TRANSFER_UPDATED", "Transaction", saved.getId(),
                "Pending transfer " + saved.getReferenceNumber() + " updated by officer");
        return saved;
    }

    @Override
    @Transactional
    public Transaction approveAndExecuteTransfer(Long transactionId, String managerEmail) {
        Transaction txn = getById(transactionId);
        if (txn.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction is not pending execution. Current status: " + txn.getStatus());
        }
        Transaction completed = completeTransfer(txn);
        auditLogService.log(managerEmail, "TRANSFER_APPROVED", "Transaction", completed.getId(),
                "Pending transfer " + completed.getReferenceNumber() + " approved and executed by manager");
        return completed;
    }

    @Override
    @Transactional
    public ApprovalRequest requestTransactionDeletion(Long transactionId, String reason, String officerEmail) {
        Transaction txn = getById(transactionId);
        User officer = userService.getByEmail(officerEmail);
        ApprovalRequest request = ApprovalRequest.builder()
                .requestType("TXN_DELETION")
                .targetId(txn.getId())
                .targetReference(txn.getReferenceNumber())
                .requester(officer)
                .status("PENDING")
                .remarks(reason != null && !reason.isBlank() ? reason : "Erroneous transaction deletion requested by officer")
                .requestedAt(LocalDateTime.now())
                .build();
        ApprovalRequest saved = approvalRequestRepository.save(request);
        auditLogService.log(officerEmail, "TXN_DELETION_REQUESTED", "Transaction", txn.getId(), reason);
        return saved;
    }

    @Override
    @Transactional
    public Transaction approveTransactionDeletion(Long transactionId, String managerEmail) {
        Transaction txn = getById(transactionId);
        if (txn.getStatus() == TransactionStatus.CANCELLED) {
            throw new IllegalStateException("Transaction is already cancelled.");
        }
        if (txn.getStatus() == TransactionStatus.COMPLETED) {
            Account source = txn.getSourceAccount();
            if (source != null && txn.getAmount() != null) {
                source.setBalance(source.getBalance().add(txn.getAmount()));
                accountRepository.save(source);
            }
            if (txn.getDestinationAccount() != null && txn.getAmount() != null) {
                Account dest = txn.getDestinationAccount();
                dest.setBalance(dest.getBalance().subtract(txn.getAmount()));
                accountRepository.save(dest);
            }
        }
        txn.setStatus(TransactionStatus.CANCELLED);
        txn.setRemarks((txn.getRemarks() != null ? txn.getRemarks() + " | " : "")
                + "Cancelled/Voided on manager approval (" + managerEmail + ")");
        Transaction saved = transactionRepository.save(txn);
        auditLogService.log(managerEmail, "TXN_DELETED", "Transaction", saved.getId(),
                "Transaction " + saved.getReferenceNumber() + " marked CANCELLED/reversed upon manager approval");
        return saved;
    }
}

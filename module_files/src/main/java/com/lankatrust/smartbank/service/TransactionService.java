package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {
    BigDecimal OTP_THRESHOLD = new BigDecimal("25000");

    Transaction initiateTransfer(Long sourceAccountId, Long destinationAccountId, String beneficiaryName,
                                  BigDecimal amount, String remarks);
    Transaction verifyTransferOtp(Long transactionId, String customerEmail, String otp);
    Transaction cancelPendingTransfer(Long transactionId, String officerEmail);
    List<Transaction> historyForAccount(Long accountId);
    List<Transaction> filterHistory(Long accountId, LocalDateTime start, LocalDateTime end);
    Transaction getById(Long id);

    List<Transaction> getRecentTransactions(int limit);

    Transaction recordDemoPayment(Long accountId, String merchantName, BigDecimal amount);

    List<Transaction> getPendingTransfers();
    List<Transaction> getAll();
    List<Transaction> findBetween(LocalDateTime start, LocalDateTime end);
    BigDecimal todayWithdrawalTotal(Long accountId);

    com.lankatrust.smartbank.entity.TransactionCorrection requestCorrection(Long transactionId, String reason,
                                                                            String proposedRemarks, BigDecimal proposedAmount,
                                                                            String officerEmail);
    com.lankatrust.smartbank.entity.TransactionCorrection approveCorrection(Long correctionId, String managerEmail);
    com.lankatrust.smartbank.entity.TransactionCorrection rejectCorrection(Long correctionId, String managerEmail, String remarks);
    List<com.lankatrust.smartbank.entity.TransactionCorrection> getCorrections();

    Transaction updatePendingTransfer(Long transactionId, BigDecimal amount, String remarks,
                                      Long destinationAccountId, String officerEmail);
    Transaction approveAndExecuteTransfer(Long transactionId, String managerEmail);
    com.lankatrust.smartbank.entity.ApprovalRequest requestTransactionDeletion(Long transactionId, String reason, String officerEmail);
    Transaction approveTransactionDeletion(Long transactionId, String managerEmail);
}

package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.entity.*;
import com.lankatrust.smartbank.repository.AccountRepository;
import com.lankatrust.smartbank.repository.TransactionRepository;
import com.lankatrust.smartbank.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundTransferWorkflowTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private AuditLogService auditLogService;
    @Mock private NotificationService notificationService;
    @Mock private UserService userService;
    @Mock private OtpService otpService;
    @Mock private ValidationService validationService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Customer customer;
    private Account sourceAccount;
    private Account destAccount;

    @BeforeEach
    void setUp() {
        customer = Customer.builder().id(1L).email("alice@bank.lk").build();
        sourceAccount = Account.builder()
                .id(100L)
                .accountNumber("LTB100")
                .balance(new BigDecimal("100000.00"))
                .status(AccountStatus.ACTIVE)
                .customer(customer)
                .build();
        destAccount = Account.builder()
                .id(200L)
                .accountNumber("LTB200")
                .balance(new BigDecimal("10000.00"))
                .status(AccountStatus.ACTIVE)
                .customer(customer)
                .build();
    }

    @Test
    void sensitiveTransferDoesNotDeductBalanceUntilOtpVerified() {
        when(accountRepository.findById(100L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(200L)).thenReturn(Optional.of(destAccount));
        when(otpService.isOtpRequired("FUND_TRANSFER", new BigDecimal("75000.00"), false)).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction t = i.getArgument(0);
            t.setId(555L);
            return t;
        });

        // 1. Initiate transfer
        Transaction pendingTxn = transactionService.initiateTransfer(
                100L, 200L, null, new BigDecimal("75000.00"), "High value transfer");

        // Assert pending state and ZERO balance deduction
        assertEquals(TransactionStatus.PENDING, pendingTxn.getStatus());
        assertFalse(pendingTxn.isOtpVerified());
        assertEquals(new BigDecimal("100000.00"), sourceAccount.getBalance()); // Source untouched!
        assertEquals(new BigDecimal("10000.00"), destAccount.getBalance());    // Dest untouched!

        // 2. Now verify OTP with correct code
        when(transactionRepository.findById(555L)).thenReturn(Optional.of(pendingTxn));
        when(otpService.verifyOtp("alice@bank.lk", "123456")).thenReturn(true);
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        Transaction completedTxn = transactionService.verifyTransferOtp(555L, "alice@bank.lk", "123456");

        // Assert completed state and correct deduction
        assertEquals(TransactionStatus.COMPLETED, completedTxn.getStatus());
        assertTrue(completedTxn.isOtpVerified());
        assertEquals(new BigDecimal("25000.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("85000.00"), destAccount.getBalance());
        verify(notificationService, atLeastOnce()).send(any(User.class), anyString(), anyString(), any(NotificationChannel.class));
    }

    @Test
    void invalidOtpDoesNotDeductBalance() {
        when(accountRepository.findById(100L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(200L)).thenReturn(Optional.of(destAccount));
        when(otpService.isOtpRequired("FUND_TRANSFER", new BigDecimal("75000.00"), false)).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction t = i.getArgument(0);
            t.setId(556L);
            return t;
        });

        Transaction pendingTxn = transactionService.initiateTransfer(
                100L, 200L, null, new BigDecimal("75000.00"), "High value transfer");

        when(transactionRepository.findById(556L)).thenReturn(Optional.of(pendingTxn));
        when(otpService.verifyOtp("alice@bank.lk", "000000")).thenReturn(false);

        // Assert exception thrown on invalid OTP
        assertThrows(IllegalStateException.class, () ->
                transactionService.verifyTransferOtp(556L, "alice@bank.lk", "000000"));

        // Balance remains completely untouched
        assertEquals(new BigDecimal("100000.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("10000.00"), destAccount.getBalance());
    }
}

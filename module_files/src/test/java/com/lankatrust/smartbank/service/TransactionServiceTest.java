package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.entity.*;
import com.lankatrust.smartbank.repository.AccountRepository;
import com.lankatrust.smartbank.repository.TransactionRepository;
import com.lankatrust.smartbank.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private AuditLogService auditLogService;
    @Mock private NotificationService notificationService;
    @Mock private UserService userService;
    @Mock private OtpService otpService;
    @Mock private ValidationService validationService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void lowValueTransferCompletesImmediately() {
        Customer customer = Customer.builder().id(1L).email("customer@lankatrust.lk").build();
        Account source = Account.builder().id(1L).balance(new BigDecimal("100000")).status(AccountStatus.ACTIVE).customer(customer).build();
        Account dest = Account.builder().id(2L).balance(BigDecimal.ZERO).status(AccountStatus.ACTIVE).customer(customer).build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(dest));
        when(otpService.isOtpRequired(anyString(), any(BigDecimal.class), anyBoolean())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        Transaction txn = transactionService.initiateTransfer(1L, 2L, null, new BigDecimal("5000"), "Test");

        assertEquals(TransactionStatus.COMPLETED, txn.getStatus());
        assertTrue(txn.isOtpVerified());
        assertEquals(new BigDecimal("95000"), source.getBalance());
        assertEquals(new BigDecimal("5000"), dest.getBalance());
    }

    @Test
    void highValueTransferRequiresOtp() {
        Customer customer = Customer.builder().id(1L).email("customer@lankatrust.lk").build();
        Account source = Account.builder().id(1L).balance(new BigDecimal("100000")).status(AccountStatus.ACTIVE).customer(customer).build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));
        when(otpService.isOtpRequired(anyString(), any(BigDecimal.class), anyBoolean())).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction txn = transactionService.initiateTransfer(1L, null, "External", new BigDecimal("50000"), "High value");

        assertEquals(TransactionStatus.PENDING, txn.getStatus());
        assertFalse(txn.isOtpVerified());
        assertEquals(new BigDecimal("100000"), source.getBalance()); // balance NOT deducted
    }

    @Test
    void transferFailsWithInsufficientBalance() {
        Customer customer = Customer.builder().id(1L).email("customer@lankatrust.lk").build();
        Account source = Account.builder().id(1L).balance(new BigDecimal("1000")).status(AccountStatus.ACTIVE).customer(customer).build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                transactionService.initiateTransfer(1L, null, null, new BigDecimal("5000"), "Test"));
        assertTrue(ex.getMessage().contains("Insufficient balance"));
    }
}

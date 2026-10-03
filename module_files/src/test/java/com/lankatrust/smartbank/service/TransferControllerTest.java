package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.controller.TransferController;
import com.lankatrust.smartbank.entity.Account;
import com.lankatrust.smartbank.entity.AccountStatus;
import com.lankatrust.smartbank.entity.Customer;
import com.lankatrust.smartbank.entity.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferControllerTest {

    @Mock private AccountService accountService;
    @Mock private TransactionService transactionService;
    @Mock private UserService userService;
    @Mock private BeneficiaryService beneficiaryService;
    @Mock private OtpService otpService;
    @Mock private ValidationService validationService;
    @Mock private Authentication authentication;

    @InjectMocks
    private TransferController transferController;

    private Customer customer;
    private Account sourceAccount;
    private Account destAccount;

    @BeforeEach
    void setUp() {
        customer = Customer.builder().id(1L).email("alice@bank.lk").build();
        sourceAccount = Account.builder()
                .id(100L)
                .accountNumber("LTB000000100")
                .balance(new BigDecimal("50000.00"))
                .status(AccountStatus.ACTIVE)
                .customer(customer)
                .build();
        destAccount = Account.builder()
                .id(200L)
                .accountNumber("LTB000000200")
                .balance(new BigDecimal("10000.00"))
                .status(AccountStatus.ACTIVE)
                .customer(customer)
                .build();
    }

    @Test
    void transferResolvesDestinationByAccountNumber() {
        when(authentication.getName()).thenReturn("alice@bank.lk");
        when(userService.getByEmail("alice@bank.lk")).thenReturn(customer);
        when(accountService.getById(100L)).thenReturn(sourceAccount);
        when(accountService.getAll()).thenReturn(List.of(sourceAccount, destAccount));

        Transaction txn = Transaction.builder().id(999L).amount(new BigDecimal("5000.00")).build();
        when(transactionService.initiateTransfer(eq(100L), eq(200L), eq("Bob"), eq(new BigDecimal("5000.00")), any()))
                .thenReturn(txn);
        when(otpService.isOtpRequired(eq("FUND_TRANSFER"), eq(new BigDecimal("5000.00")), eq(false)))
                .thenReturn(false);

        Model model = new ExtendedModelMap();
        String view = transferController.initiate(100L, "LTB000000200", "Bob",
                new BigDecimal("5000.00"), "Payment", authentication, model);

        assertEquals("transfer/success", view);
        assertEquals(txn, model.getAttribute("transaction"));
        verify(validationService).validateDestinationNotSource(100L, 200L);
    }

    @Test
    void transferResolvesDestinationByNumericId() {
        when(authentication.getName()).thenReturn("alice@bank.lk");
        when(userService.getByEmail("alice@bank.lk")).thenReturn(customer);
        when(accountService.getById(100L)).thenReturn(sourceAccount);
        when(accountService.getAll()).thenReturn(List.of(sourceAccount));
        when(accountService.getById(200L)).thenReturn(destAccount);

        Transaction txn = Transaction.builder().id(999L).amount(new BigDecimal("5000.00")).build();
        when(transactionService.initiateTransfer(eq(100L), eq(200L), eq("Bob"), eq(new BigDecimal("5000.00")), any()))
                .thenReturn(txn);
        when(otpService.isOtpRequired(eq("FUND_TRANSFER"), eq(new BigDecimal("5000.00")), eq(false)))
                .thenReturn(false);

        Model model = new ExtendedModelMap();
        String view = transferController.initiate(100L, "200", "Bob",
                new BigDecimal("5000.00"), "Payment", authentication, model);

        assertEquals("transfer/success", view);
        assertEquals(txn, model.getAttribute("transaction"));
    }

    @Test
    void transferFailsWhenDestinationNotFound() {
        when(authentication.getName()).thenReturn("alice@bank.lk");
        when(userService.getByEmail("alice@bank.lk")).thenReturn(customer);
        when(accountService.getById(100L)).thenReturn(sourceAccount);
        when(accountService.getAll()).thenReturn(List.of(sourceAccount));
        when(accountService.getForCustomer(1L)).thenReturn(List.of(sourceAccount));

        Model model = new ExtendedModelMap();
        String view = transferController.initiate(100L, "NON_EXISTENT_ACC", "Bob",
                new BigDecimal("5000.00"), "Payment", authentication, model);

        assertEquals("transfer/form", view);
        assertTrue(model.containsAttribute("error"));
        assertTrue(model.getAttribute("error").toString().contains("Destination account not found"));
    }
}

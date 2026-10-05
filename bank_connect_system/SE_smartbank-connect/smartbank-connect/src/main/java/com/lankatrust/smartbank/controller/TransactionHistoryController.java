package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.controller.TransactionHistoryController;
import com.lankatrust.smartbank.entity.Account;
import com.lankatrust.smartbank.entity.AccountStatus;
import com.lankatrust.smartbank.entity.Customer;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionHistoryControllerTest {

    @Mock private TransactionService transactionService;
    @Mock private AccountService accountService;
    @Mock private UserService userService;
    @Mock private Authentication authentication;

    @InjectMocks
    private TransactionHistoryController controller;

    private Customer customer;
    private Account account;

    @BeforeEach
    void setUp() {
        customer = Customer.builder().id(1L).email("alice@bank.lk").build();
        account = Account.builder()
                .id(100L)
                .accountNumber("LTB000000100")
                .balance(new BigDecimal("50000.00"))
                .status(AccountStatus.ACTIVE)
                .customer(customer)
                .build();
    }

    @Test
    void historyHandlesInvalidDateFormatGracefully() {
        when(authentication.getName()).thenReturn("alice@bank.lk");
        when(userService.getByEmail("alice@bank.lk")).thenReturn(customer);
        when(accountService.getForCustomer(1L)).thenReturn(List.of(account));

        Model model = new ExtendedModelMap();
        String view = controller.history(100L, "invalid-date", "2026-09-15", authentication, model);

        assertEquals("history/list", view);
        assertTrue(model.containsAttribute("error"));
        assertEquals("Invalid date format. Please use YYYY-MM-DD.", model.getAttribute("error"));
        assertEquals(Collections.emptyList(), model.getAttribute("transactions"));
    }

    @Test
    void historyHandlesStartDateAfterEndDateGracefully() {
        when(authentication.getName()).thenReturn("alice@bank.lk");
        when(userService.getByEmail("alice@bank.lk")).thenReturn(customer);
        when(accountService.getForCustomer(1L)).thenReturn(List.of(account));

        Model model = new ExtendedModelMap();
        String view = controller.history(100L, "2026-09-20", "2026-09-10", authentication, model);

        assertEquals("history/list", view);
        assertTrue(model.containsAttribute("error"));
        assertEquals("Start date cannot be after end date.", model.getAttribute("error"));
        assertEquals(Collections.emptyList(), model.getAttribute("transactions"));
    }

    @Test
    void historyFetchesFilteredTransactionsForValidDates() {
        when(authentication.getName()).thenReturn("alice@bank.lk");
        when(userService.getByEmail("alice@bank.lk")).thenReturn(customer);
        when(accountService.getForCustomer(1L)).thenReturn(List.of(account));
        when(transactionService.filterHistory(eq(100L), any(), any())).thenReturn(Collections.emptyList());

        Model model = new ExtendedModelMap();
        String view = controller.history(100L, "2026-09-01", "2026-09-15", authentication, model);

        assertEquals("history/list", view);
        assertFalse(model.containsAttribute("error"));
        verify(transactionService).filterHistory(eq(100L), any(), any());
    }
}

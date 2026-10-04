package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.entity.*;
import com.lankatrust.smartbank.repository.*;
import com.lankatrust.smartbank.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private BranchRepository branchRepository;
    @Mock private ApprovalRequestRepository approvalRequestRepository;
    @Mock private AtmCardRepository atmCardRepository;
    @Mock private AuditLogService auditLogService;
    @Mock private NotificationService notificationService;
    @Mock private FileStorageService fileStorageService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ValidationService validationService;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void approveAccountGeneratesAtmCardAndActivates() {
        User manager = User.builder().id(1L).email("manager@lankatrust.lk").role(Role.BANK_MANAGER).build();
        Customer customer = Customer.builder().id(2L).email("customer@lankatrust.lk").kycStatus("PENDING").build();
        Account account = Account.builder().id(10L).accountNumber("LTB123456789").customer(customer)
                .status(AccountStatus.PENDING_APPROVAL).initialDeposit(new BigDecimal("10000")).build();

        when(accountRepository.findById(10L)).thenReturn(Optional.of(account));
        when(userRepository.findByEmail("manager@lankatrust.lk")).thenReturn(Optional.of(manager));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));
        when(atmCardRepository.save(any(AtmCard.class))).thenAnswer(i -> i.getArgument(0));

        Account approved = accountService.approveAccount(10L, "manager@lankatrust.lk");

        assertEquals(AccountStatus.ACTIVE, approved.getStatus());
        assertEquals(new BigDecimal("10000"), approved.getBalance());
        assertEquals("APPROVED", customer.getKycStatus());
        verify(atmCardRepository, times(1)).save(any(AtmCard.class));
        verify(notificationService, times(1)).send(any(User.class), anyString(), anyString(), any(NotificationChannel.class));
    }

    @Test
    void openAccountRejectsDuplicateNic() {
        User officer = User.builder().id(3L).email("officer@lankatrust.lk").role(Role.BANK_OFFICER).build();
        Branch branch = Branch.builder().id(1).code("CMB-MAIN").name("Colombo Main").build();

        when(userRepository.findByEmail("officer@lankatrust.lk")).thenReturn(Optional.of(officer));
        when(branchRepository.findById(1)).thenReturn(Optional.of(branch));
        doThrow(new IllegalArgumentException("A customer with this NIC already exists."))
                .when(validationService).validateNicNotExists("200212345678");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                accountService.openAccount("CUST1", "Test", "test@mail.lk", "0771234567", "200212345678",
                        LocalDate.of(2000, 1, 1), "Colombo", "Engineer", new BigDecimal("100000"),
                        "SAVINGS", new BigDecimal("5000"), 1, null, null, java.util.Collections.emptyMap(),
                        "officer@lankatrust.lk"));

        assertTrue(ex.getMessage().contains("customer with this NIC already exists"));
    }

    @Test
    void freezeAccountRejectsAlreadyFrozenAccount() {
        Account account = Account.builder().id(10L).status(AccountStatus.FROZEN).build();
        when(accountRepository.findById(10L)).thenReturn(Optional.of(account));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                accountService.freezeAccount(10L, "manager@lankatrust.lk"));

        assertTrue(ex.getMessage().contains("already frozen"));
    }

    @Test
    void unfreezeAccountRejectsActiveAccount() {
        Account account = Account.builder().id(10L).status(AccountStatus.ACTIVE).build();
        when(accountRepository.findById(10L)).thenReturn(Optional.of(account));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                accountService.unfreezeAccount(10L, "manager@lankatrust.lk"));

        assertTrue(ex.getMessage().contains("already active"));
    }
}

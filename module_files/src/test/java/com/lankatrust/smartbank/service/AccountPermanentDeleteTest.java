package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.controller.AccountController;
import com.lankatrust.smartbank.entity.*;
import com.lankatrust.smartbank.repository.*;
import com.lankatrust.smartbank.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountPermanentDeleteTest {

    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private BranchRepository branchRepository;
    @Mock private ApprovalRequestRepository approvalRequestRepository;
    @Mock private AtmCardRepository atmCardRepository;
    @Mock private StatementRequestRepository statementRequestRepository;
    @Mock private TransactionCorrectionRepository transactionCorrectionRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private BeneficiaryRepository beneficiaryRepository;
    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private AuditLogService auditLogService;
    @Mock private NotificationService notificationService;
    @Mock private FileStorageService fileStorageService;
    @Mock private ValidationService validationService;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        Customer customer = Customer.builder().id(50L).email("cust@lankatrust.lk").fullName("Test Customer").build();
        testAccount = Account.builder()
                .id(100L)
                .accountNumber("LTB100200300")
                .status(AccountStatus.CLOSED)
                .balance(BigDecimal.ZERO)
                .customer(customer)
                .build();
    }

    @Test
    void deleteAccountPermanentlyFailsIfAccountNotFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                accountService.deleteAccountPermanently(999L, "manager@lankatrust.lk"));

        assertTrue(ex.getMessage().contains("Account not found"));
    }

    @Test
    void deleteAccountPermanentlyCascadesAllDependentsAndDeletesAccount() {
        when(accountRepository.findById(100L)).thenReturn(Optional.of(testAccount));

        // Mock ATM card
        AtmCard card = AtmCard.builder().id(10L).account(testAccount).cardNumber("4532XXXXXXXX1234").build();
        when(atmCardRepository.findByAccountId(100L)).thenReturn(Optional.of(card));

        // Mock Statement Requests
        StatementRequest statement = StatementRequest.builder().id(20L).account(testAccount).build();
        when(statementRequestRepository.findByAccountId(100L)).thenReturn(List.of(statement));

        // Mock Transactions and corrections
        Transaction txn = Transaction.builder().id(30L).sourceAccount(testAccount).build();
        when(transactionRepository.findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(100L, 100L))
                .thenReturn(List.of(txn));
        TransactionCorrection corr = TransactionCorrection.builder().id(40L).transaction(txn).build();
        when(transactionCorrectionRepository.findByTransactionId(30L)).thenReturn(List.of(corr));

        // Mock Approval Requests
        ApprovalRequest approval = ApprovalRequest.builder().id(60L).targetId(100L).requestType("ACCOUNT_FREEZE").build();
        when(approvalRequestRepository.findByTargetId(100L)).thenReturn(List.of(approval));

        // Mock Beneficiaries
        Beneficiary beneficiary = Beneficiary.builder().id(70L).beneficiaryAccountNumber("LTB100200300").build();
        when(beneficiaryRepository.findByBeneficiaryAccountNumber("LTB100200300")).thenReturn(List.of(beneficiary));

        // Execute permanent delete
        accountService.deleteAccountPermanently(100L, "manager@lankatrust.lk");

        // Verify cascading deletion order and repository executions
        verify(atmCardRepository, times(1)).delete(card);
        verify(statementRequestRepository, times(1)).deleteAll(List.of(statement));
        verify(transactionCorrectionRepository, times(1)).deleteAll(List.of(corr));
        verify(transactionRepository, times(1)).deleteAll(List.of(txn));
        verify(jdbcTemplate, times(1)).update(eq("DELETE FROM transfers WHERE from_account_id = ?"), eq(100L));
        verify(approvalRequestRepository, times(1)).deleteAll(List.of(approval));
        verify(beneficiaryRepository, times(1)).deleteAll(List.of(beneficiary));
        verify(accountRepository, times(1)).delete(testAccount);

        // Verify audit log created
        verify(auditLogService, times(1)).log(
                eq("manager@lankatrust.lk"),
                eq("ACCOUNT_PERMANENTLY_DELETED"),
                eq("Account"),
                eq(100L),
                contains("LTB100200300")
        );
    }

    @Test
    void accountControllerEndpointsTriggerPermanentDelete() {
        AccountService mockAccountService = mock(AccountService.class);
        UserService mockUserService = mock(UserService.class);
        BranchService mockBranchService = mock(BranchService.class);
        ValidationService mockValidationService = mock(ValidationService.class);

        AccountController controller = new AccountController(
                mockAccountService,
                mockUserService,
                mockBranchService,
                mockValidationService
        );

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("manager@lankatrust.lk");

        // Test POST form redirect
        RedirectAttributes ra = new RedirectAttributesModelMap();
        String redirect = controller.deleteAccount(100L, auth, ra);
        assertEquals("redirect:/accounts/manage", redirect);
        verify(mockAccountService, times(1)).deleteAccountPermanently(100L, "manager@lankatrust.lk");
        assertTrue(ra.getFlashAttributes().containsKey("success"));

        // Test DELETE API endpoint
        ResponseEntity<?> response = controller.deleteAccountApi(100L, auth);
        assertEquals(200, response.getStatusCode().value());
        verify(mockAccountService, times(2)).deleteAccountPermanently(100L, "manager@lankatrust.lk");
    }
}

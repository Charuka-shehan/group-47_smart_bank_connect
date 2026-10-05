package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.entity.*;
import com.lankatrust.smartbank.repository.LoanRepository;
import com.lankatrust.smartbank.repository.UserRepository;
import com.lankatrust.smartbank.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanWorkflowTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private LoanServiceImpl loanService;

    private User customerUser;
    private User officerUser;
    private User managerUser;
    private Loan loan;

    @BeforeEach
    void setUp() {
        customerUser = User.builder()
                .id(10L)
                .email("customer@lankatrust.lk")
                .fullName("Sunil Silva")
                .role(Role.CUSTOMER)
                .build();

        officerUser = User.builder()
                .id(11L)
                .email("officer@lankatrust.lk")
                .fullName("Dilshan Perera")
                .role(Role.BANK_OFFICER)
                .build();

        managerUser = User.builder()
                .id(13L)
                .email("manager@lankatrust.lk")
                .fullName("Senior Manager")
                .role(Role.BANK_MANAGER)
                .build();

        loan = Loan.builder()
                .id(50L)
                .customer(customerUser)
                .requestedAmount(new BigDecimal("500000.00"))
                .loanType("PERSONAL")
                .monthlyIncome(new BigDecimal("120000.00"))
                .employmentStatus("PERMANENT")
                .status(LoanStatus.SUBMITTED)
                .applicationNumber("LN12345678")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Loan transition: SUBMITTED -> UNDER_VERIFICATION succeeds")
    void transitionSubmittedToUnderVerification() {
        when(loanRepository.findById(50L)).thenReturn(Optional.of(loan));
        when(userRepository.findByEmail("officer@lankatrust.lk")).thenReturn(Optional.of(officerUser));
        when(loanRepository.save(any(Loan.class))).thenAnswer(i -> i.getArgument(0));

        Loan updated = loanService.verify(50L, "officer@lankatrust.lk", "Document check passed");

        assertEquals(LoanStatus.UNDER_VERIFICATION, updated.getStatus());
        verify(loanRepository).save(loan);
    }

    @Test
    @DisplayName("Loan transition: UNDER_VERIFICATION -> COMPLIANCE_CHECK succeeds")
    void transitionUnderVerificationToComplianceCheck() {
        loan.setStatus(LoanStatus.UNDER_VERIFICATION);
        when(loanRepository.findById(50L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(i -> i.getArgument(0));

        Loan updated = loanService.complianceCheck(50L, "compliance@lankatrust.lk", "Compliance check clear");

        assertEquals(LoanStatus.COMPLIANCE_CHECK, updated.getStatus());
    }

    @Test
    @DisplayName("Illegal transition: SUBMITTED directly to APPROVED throws IllegalStateException")
    void illegalDirectApprovalThrowsException() {
        when(loanRepository.findById(50L)).thenReturn(Optional.of(loan));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                loanService.approve(50L, "manager@lankatrust.lk"));

        assertTrue(ex.getMessage().contains("Cannot approve loan application directly without prior review"));
        verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Illegal transition: UNDER_VERIFICATION directly to APPROVED throws IllegalStateException (must complete COMPLIANCE_CHECK)")
    void illegalApprovalFromUnderVerificationThrowsException() {
        loan.setStatus(LoanStatus.UNDER_VERIFICATION);
        when(loanRepository.findById(50L)).thenReturn(Optional.of(loan));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                loanService.approve(50L, "manager@lankatrust.lk"));

        assertTrue(ex.getMessage().contains("Cannot approve loan application directly without prior review"));
        verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Dual Authorization: Manager cannot approve their own loan application")
    void managerCannotApproveOwnLoanApplication() {
        loan.setStatus(LoanStatus.COMPLIANCE_CHECK);
        loan.setCustomer(managerUser); // manager applied for the loan
        when(loanRepository.findById(50L)).thenReturn(Optional.of(loan));
        when(userRepository.findByEmail("manager@lankatrust.lk")).thenReturn(Optional.of(managerUser));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                loanService.approve(50L, "manager@lankatrust.lk"));

        assertTrue(ex.getMessage().contains("Requesters cannot approve their own requests"));
        verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Customer cancellation: Owner can cancel SUBMITTED application")
    void customerCanCancelSubmittedLoan() {
        when(loanRepository.findById(50L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(i -> i.getArgument(0));

        Loan cancelled = loanService.cancel(50L, 10L);

        assertEquals(LoanStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    @DisplayName("Customer cancellation: Non-owner cannot cancel application (IDOR prevention)")
    void nonOwnerCannotCancelLoan() {
        when(loanRepository.findById(50L)).thenReturn(Optional.of(loan));

        assertThrows(AccessDeniedException.class, () ->
                loanService.cancel(50L, 99L));

        verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Customer cancellation: Cannot cancel already APPROVED application")
    void cannotCancelApprovedLoan() {
        loan.setStatus(LoanStatus.APPROVED);
        when(loanRepository.findById(50L)).thenReturn(Optional.of(loan));

        assertThrows(IllegalStateException.class, () ->
                loanService.cancel(50L, 10L));

        verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Customer update: Owner can update SUBMITTED application")
    void customerCanUpdateSubmittedLoan() {
        when(loanRepository.findById(50L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(i -> i.getArgument(0));

        BigDecimal newAmount = new BigDecimal("750000.00");
        BigDecimal newIncome = new BigDecimal("150000.00");
        Loan updated = loanService.updateApplication(50L, 10L, "VEHICLE", newAmount, newIncome, "Self-Employed", "Vehicle quotation");

        assertEquals("VEHICLE", updated.getLoanType());
        assertEquals(newAmount, updated.getRequestedAmount());
        assertEquals(newIncome, updated.getMonthlyIncome());
        assertEquals("Self-Employed", updated.getEmploymentStatus());
        verify(loanRepository).save(loan);
    }

    @Test
    @DisplayName("Customer update: Non-owner cannot update application (IDOR prevention)")
    void nonOwnerCannotUpdateLoan() {
        when(loanRepository.findById(50L)).thenReturn(Optional.of(loan));

        assertThrows(AccessDeniedException.class, () ->
                loanService.updateApplication(50L, 99L, "HOME", new BigDecimal("1000000.00"), new BigDecimal("200000.00"), "Salaried", "Notes"));

        verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Customer update: Cannot update application once verification has started")
    void cannotUpdateUnderVerificationLoan() {
        loan.setStatus(LoanStatus.UNDER_VERIFICATION);
        when(loanRepository.findById(50L)).thenReturn(Optional.of(loan));

        assertThrows(IllegalStateException.class, () ->
                loanService.updateApplication(50L, 10L, "HOME", new BigDecimal("600000.00"), new BigDecimal("120000.00"), "Salaried", "Notes"));

        verify(loanRepository, never()).save(any());
    }
}

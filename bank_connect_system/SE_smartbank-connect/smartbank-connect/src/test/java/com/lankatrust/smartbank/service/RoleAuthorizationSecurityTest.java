package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.entity.*;
import com.lankatrust.smartbank.repository.ApprovalRequestRepository;
import com.lankatrust.smartbank.repository.BeneficiaryRepository;
import com.lankatrust.smartbank.repository.UserRepository;
import com.lankatrust.smartbank.service.impl.ApprovalServiceImpl;
import com.lankatrust.smartbank.service.impl.BeneficiaryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleAuthorizationSecurityTest {

    @Mock private ApprovalRequestRepository approvalRequestRepository;
    @Mock private UserService userService;
    @Mock private AuditLogService auditLogService;
    @Mock private AccountService accountService;
    @Mock private TransactionService transactionService;

    @InjectMocks
    private ApprovalServiceImpl approvalService;

    @Mock private BeneficiaryRepository beneficiaryRepository;
    @Mock private UserRepository userRepository;

    @Test
    void approvalServiceDisallowsRequesterFromApprovingOwnRequest() {
        User requester = User.builder()
                .id(1L)
                .email("officer@lankatrust.lk")
                .role(Role.BANK_OFFICER)
                .build();

        ApprovalRequest request = ApprovalRequest.builder()
                .id(10L)
                .requestType("ACCOUNT_OPEN")
                .targetId(50L)
                .requester(requester)
                .status("PENDING")
                .build();

        when(approvalRequestRepository.findById(10L)).thenReturn(Optional.of(request));

        // The requester officer tries to approve their own request as manager
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                approvalService.approve(10L, "officer@lankatrust.lk", "Self approval attempt"));

        assertTrue(ex.getMessage().contains("Requesters cannot approve their own requests"));
        verify(approvalRequestRepository, never()).save(any());
    }

    @Test
    void approvalServiceAllowsDifferentManagerToApprove() {
        User requester = User.builder()
                .id(1L)
                .email("officer@lankatrust.lk")
                .role(Role.BANK_OFFICER)
                .build();

        User manager = User.builder()
                .id(2L)
                .email("manager@lankatrust.lk")
                .role(Role.BANK_MANAGER)
                .build();

        ApprovalRequest request = ApprovalRequest.builder()
                .id(10L)
                .requestType("ACCOUNT_OPEN")
                .targetId(50L)
                .requester(requester)
                .status("PENDING")
                .build();

        when(approvalRequestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(userService.getByEmail("manager@lankatrust.lk")).thenReturn(manager);
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenAnswer(i -> i.getArgument(0));

        ApprovalRequest approved = approvalService.approve(10L, "manager@lankatrust.lk", "Approved by manager");

        assertEquals("APPROVED", approved.getStatus());
        assertEquals(manager, approved.getApprovedBy());
    }

    @Test
    void beneficiaryServicePreventsIdorDeletion() {
        BeneficiaryServiceImpl beneficiaryService = new BeneficiaryServiceImpl(beneficiaryRepository, userRepository);

        User owner = User.builder().id(10L).email("owner@bank.lk").build();
        Beneficiary beneficiary = Beneficiary.builder()
                .id(99L)
                .user(owner)
                .beneficiaryName("Supplier")
                .beneficiaryAccountNumber("123456789")
                .beneficiaryBank("LankaTrust Bank")
                .build();

        when(beneficiaryRepository.findById(99L)).thenReturn(Optional.of(beneficiary));

        // Attacker with userId 88 tries to delete beneficiary owned by userId 10
        assertThrows(AccessDeniedException.class, () ->
                beneficiaryService.deleteBeneficiary(99L, 88L));

        verify(beneficiaryRepository, never()).delete(any());

        // Legitimate owner deletes
        assertDoesNotThrow(() -> beneficiaryService.deleteBeneficiary(99L, 10L));
        verify(beneficiaryRepository, times(1)).delete(beneficiary);
    }

    @Test
    void beneficiaryServicePreventsIdorUpdate() {
        BeneficiaryServiceImpl beneficiaryService = new BeneficiaryServiceImpl(beneficiaryRepository, userRepository);

        User owner = User.builder().id(10L).email("owner@bank.lk").build();
        Beneficiary beneficiary = Beneficiary.builder()
                .id(99L)
                .user(owner)
                .beneficiaryName("Supplier")
                .beneficiaryAccountNumber("123456789")
                .beneficiaryBank("LankaTrust Bank")
                .build();

        when(beneficiaryRepository.findById(99L)).thenReturn(Optional.of(beneficiary));

        // Attacker with userId 88 tries to update beneficiary owned by userId 10
        assertThrows(AccessDeniedException.class, () ->
                beneficiaryService.updateBeneficiary(99L, 88L, "New Nickname", "Commercial Bank"));

        verify(beneficiaryRepository, never()).save(any());

        // Legitimate owner updates
        when(beneficiaryRepository.save(any(Beneficiary.class))).thenAnswer(i -> i.getArgument(0));
        assertDoesNotThrow(() -> beneficiaryService.updateBeneficiary(99L, 10L, "New Nickname", "Commercial Bank"));
        verify(beneficiaryRepository, times(1)).save(beneficiary);
    }
}

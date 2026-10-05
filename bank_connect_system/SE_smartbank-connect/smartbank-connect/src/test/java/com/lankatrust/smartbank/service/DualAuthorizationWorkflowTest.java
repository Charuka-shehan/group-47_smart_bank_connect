package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.entity.*;
import com.lankatrust.smartbank.repository.ApprovalRequestRepository;
import com.lankatrust.smartbank.repository.CustomerDocumentRepository;
import com.lankatrust.smartbank.service.impl.ApprovalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DualAuthorizationWorkflowTest {

    @Mock
    private ApprovalRequestRepository approvalRequestRepository;

    @Mock
    private UserService userService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ApprovalServiceImpl approvalService;

    private User requesterManager;
    private User differentManager;
    private User staffOfficer;

    @BeforeEach
    void setUp() {
        requesterManager = User.builder()
                .id(1L)
                .email("nadeesha@lankatrust.lk")
                .fullName("Nadeesha Perera")
                .role(Role.BANK_MANAGER)
                .build();

        differentManager = User.builder()
                .id(2L)
                .email("manager2@lankatrust.lk")
                .fullName("Kamal Gunaratne")
                .role(Role.BANK_MANAGER)
                .build();

        staffOfficer = User.builder()
                .id(3L)
                .email("officer@lankatrust.lk")
                .fullName("Dilshan Perera")
                .role(Role.BANK_OFFICER)
                .build();
    }

    @Test
    @DisplayName("TEST 1: Requester cannot approve own request (Dual Authorization guard)")
    void test1_requesterCannotApproveOwnRequest() {
        ApprovalRequest request = ApprovalRequest.builder()
                .id(100L)
                .requestType("TEMPLATE_DELETE")
                .targetReference("ACCOUNT_ACTIVATED")
                .requester(requesterManager)
                .status("PENDING")
                .requestedAt(LocalDateTime.now())
                .build();

        when(approvalRequestRepository.findById(100L)).thenReturn(Optional.of(request));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                approvalService.approve(100L, "nadeesha@lankatrust.lk", "Trying to self-approve"));

        assertTrue(ex.getMessage().contains("Requesters cannot approve their own requests"));
        assertTrue(ex.getMessage().contains("A different authorized Manager is required"));
        verify(approvalRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST 2: Different manager approves -> success, executed, notification sent")
    void test2_differentManagerApprovesSuccessfully() {
        ApprovalRequest request = ApprovalRequest.builder()
                .id(100L)
                .requestType("ACCOUNT_ACTIVATION")
                .targetReference("ACC-1001")
                .requester(requesterManager)
                .status("PENDING")
                .requestedAt(LocalDateTime.now())
                .build();

        when(approvalRequestRepository.findById(100L)).thenReturn(Optional.of(request));
        when(userService.getByEmail("manager2@lankatrust.lk")).thenReturn(differentManager);
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenAnswer(i -> i.getArgument(0));

        ApprovalRequest approved = approvalService.approve(100L, "manager2@lankatrust.lk", "Verified and approved");

        assertEquals("APPROVED", approved.getStatus());
        assertEquals(differentManager, approved.getApprovedBy());
        assertEquals("EXECUTED", approved.getExecutionStatus());
        assertNotNull(approved.getApprovedAt());
        assertNotNull(approved.getExecutedAt());
        verify(approvalRequestRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("TEST 3: Staff requests, Manager approves -> success")
    void test3_staffRequestsManagerApproves() {
        ApprovalRequest request = ApprovalRequest.builder()
                .id(101L)
                .requestType("ACCOUNT_ACTIVATION")
                .targetReference("ACC-1002")
                .requester(staffOfficer)
                .status("PENDING")
                .requestedAt(LocalDateTime.now())
                .build();

        when(approvalRequestRepository.findById(101L)).thenReturn(Optional.of(request));
        when(userService.getByEmail("manager2@lankatrust.lk")).thenReturn(differentManager);
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenAnswer(i -> i.getArgument(0));

        ApprovalRequest approved = approvalService.approve(101L, "manager2@lankatrust.lk", "Officer request approved");

        assertEquals("APPROVED", approved.getStatus());
        assertEquals(differentManager, approved.getApprovedBy());
        assertEquals("EXECUTED", approved.getExecutionStatus());
    }

    @Test
    @DisplayName("TEST 7: Attempt to approve already processed request -> throws IllegalStateException")
    void test7_cannotApproveAlreadyProcessedRequest() {
        ApprovalRequest request = ApprovalRequest.builder()
                .id(102L)
                .requestType("ACCOUNT_ACTIVATION")
                .targetReference("ACC-1003")
                .requester(staffOfficer)
                .status("APPROVED")
                .build();

        when(approvalRequestRepository.findById(102L)).thenReturn(Optional.of(request));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                approvalService.approve(102L, "manager2@lankatrust.lk", "Second approval attempt"));

        assertTrue(ex.getMessage().contains("already been processed"));
    }

    @Test
    @DisplayName("TEST 8: Nonexistent request approval -> throws IllegalArgumentException")
    void test8_nonexistentRequestThrowsException() {
        when(approvalRequestRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                approvalService.approve(999L, "manager2@lankatrust.lk", "Approving ghost request"));

        assertTrue(ex.getMessage().contains("Approval request not found"));
    }

    @Test
    @DisplayName("TEST 10: Rejection requires reason and sets REJECTED status")
    void test10_rejectionRequiresReasonAndRecordsMetadata() {
        ApprovalRequest request = ApprovalRequest.builder()
                .id(103L)
                .requestType("ACCOUNT_ACTIVATION")
                .targetReference("ACC-1004")
                .requester(staffOfficer)
                .status("PENDING")
                .build();

        when(approvalRequestRepository.findById(103L)).thenReturn(Optional.of(request));

        // Attempt rejection without reason -> fails
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                approvalService.reject(103L, "manager2@lankatrust.lk", "  "));

        assertTrue(ex.getMessage().contains("Rejection reason is required"));

        // Rejection with reason -> succeeds
        when(userService.getByEmail("manager2@lankatrust.lk")).thenReturn(differentManager);
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenAnswer(i -> i.getArgument(0));

        ApprovalRequest rejected = approvalService.reject(103L, "manager2@lankatrust.lk", "Invalid documentation provided");

        assertEquals("REJECTED", rejected.getStatus());
        assertEquals("REJECTED", rejected.getExecutionStatus());
        assertEquals("Invalid documentation provided", rejected.getRejectionReason());
        assertNotNull(rejected.getRejectedAt());
        assertEquals(differentManager, rejected.getApprovedBy());
    }
}

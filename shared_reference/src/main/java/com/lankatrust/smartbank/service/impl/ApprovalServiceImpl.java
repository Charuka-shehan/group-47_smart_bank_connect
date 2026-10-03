package com.lankatrust.smartbank.service.impl;

import com.lankatrust.smartbank.entity.ApprovalRequest;
import com.lankatrust.smartbank.entity.PublishedReport;
import com.lankatrust.smartbank.entity.StaffCreationRequest;
import com.lankatrust.smartbank.entity.User;
import com.lankatrust.smartbank.repository.ApprovalRequestRepository;
import com.lankatrust.smartbank.repository.PublishedReportRepository;
import com.lankatrust.smartbank.repository.StaffCreationRequestRepository;
import com.lankatrust.smartbank.service.AccountService;
import com.lankatrust.smartbank.service.ApprovalService;
import com.lankatrust.smartbank.service.AuditLogService;
import com.lankatrust.smartbank.service.NotificationService;
import com.lankatrust.smartbank.service.TransactionService;
import com.lankatrust.smartbank.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lankatrust.smartbank.repository.AuditLogRepository;
import com.lankatrust.smartbank.repository.NotificationTemplateRepository;

@Service
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final UserService userService;
    private final AuditLogService auditLogService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final PublishedReportRepository publishedReportRepository;
    private final StaffCreationRequestRepository staffCreationRequestRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;

    public ApprovalServiceImpl(ApprovalRequestRepository approvalRequestRepository,
                               UserService userService,
                               AuditLogService auditLogService,
                               @Lazy AccountService accountService,
                               @Lazy TransactionService transactionService,
                               PublishedReportRepository publishedReportRepository,
                               StaffCreationRequestRepository staffCreationRequestRepository,
                               NotificationTemplateRepository notificationTemplateRepository,
                               AuditLogRepository auditLogRepository,
                               @Lazy NotificationService notificationService) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.userService = userService;
        this.auditLogService = auditLogService;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.publishedReportRepository = publishedReportRepository;
        this.staffCreationRequestRepository = staffCreationRequestRepository;
        this.notificationTemplateRepository = notificationTemplateRepository;
        this.auditLogRepository = auditLogRepository;
        this.notificationService = notificationService;
    }

    @Override
    public List<ApprovalRequest> getPending() {
        return approvalRequestRepository.findByStatusOrderByRequestedAtDesc("PENDING");
    }

    @Override
    public List<ApprovalRequest> getAll() {
        return approvalRequestRepository.findAll();
    }

    @Override
    public ApprovalRequest getById(Long id) {
        return approvalRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Approval request not found"));
    }

    @Override
    @Transactional
    public ApprovalRequest create(String requestType, Long targetId, String targetReference, String requesterEmail, String remarks) {
        User requester = userService.getByEmail(requesterEmail);
        ApprovalRequest request = ApprovalRequest.builder()
                .requestType(requestType)
                .targetId(targetId)
                .targetReference(targetReference)
                .requester(requester)
                .status("PENDING")
                .executionStatus("PENDING")
                .requestedData(remarks)
                .remarks(remarks)
                .requestedAt(LocalDateTime.now())
                .build();
        ApprovalRequest saved = approvalRequestRepository.save(request);
        auditLogService.log(requesterEmail, "APPROVAL_REQUESTED", "ApprovalRequest", saved.getId(), requestType + " - " + targetReference);
        return saved;
    }

    @Override
    @Transactional
    public ApprovalRequest approve(Long requestId, String managerEmail, String remarks) {
        ApprovalRequest request = getById(requestId);
        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("This request has already been processed.");
        }
        if (managerEmail != null && request.getRequester() != null && request.getRequester().getEmail() != null
                && request.getRequester().getEmail().trim().equalsIgnoreCase(managerEmail.trim())) {
            throw new IllegalStateException("This request cannot be approved by its requester. A different authorized Manager is required. (Requesters cannot approve their own requests. Dual authorization is required.)");
        }
        User approver = userService.getByEmail(managerEmail);
        if (request.getRequester() != null && approver != null && approver.getId() != null
                && request.getRequester().getId() != null
                && request.getRequester().getId().equals(approver.getId())) {
            throw new IllegalStateException("This request cannot be approved by its requester. A different authorized Manager is required. (Requesters cannot approve their own requests. Dual authorization is required.)");
        }
        if (approver == null) {
            throw new IllegalArgumentException("Approver not found: " + managerEmail);
        }
        execute(request, managerEmail, true, remarks);
        return finalize(requestId, managerEmail, "APPROVED", remarks);
    }

    @Override
    @Transactional
    public ApprovalRequest reject(Long requestId, String managerEmail, String remarks) {
        ApprovalRequest request = getById(requestId);
        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("This request has already been processed.");
        }
        if (remarks == null || remarks.trim().isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required.");
        }
        execute(request, managerEmail, false, remarks);
        return finalize(requestId, managerEmail, "REJECTED", remarks);
    }

    private ApprovalRequest finalize(Long requestId, String managerEmail, String status, String remarks) {
        ApprovalRequest request = getById(requestId);
        if ("PENDING".equals(request.getStatus())) {
            User approver = userService.getByEmail(managerEmail);
            request.setStatus(status);
            request.setApprovedBy(approver);
            if ("APPROVED".equals(status)) {
                request.setApprovedAt(LocalDateTime.now());
                request.setExecutionStatus("EXECUTED");
                request.setExecutedAt(LocalDateTime.now());
            } else {
                request.setRejectedAt(LocalDateTime.now());
                request.setRejectionReason(remarks);
                request.setExecutionStatus("REJECTED");
            }
            request = approvalRequestRepository.save(request);

            if (request.getRequester() != null && notificationService != null) {
                try {
                    com.lankatrust.smartbank.entity.NotificationChannel channel = com.lankatrust.smartbank.entity.NotificationChannel.IN_APP;
                    String message = "Your " + request.getRequestType() + " request (" + request.getTargetReference() + ") has been "
                            + status.toLowerCase() + " by " + (approver != null ? approver.getFullName() : managerEmail)
                            + (remarks != null && !remarks.isBlank() ? ". Remarks: " + remarks : ".");
                    notificationService.send(request.getRequester(), "Approval Request " + status, message, channel);
                } catch (Exception notifEx) {
                    org.slf4j.LoggerFactory.getLogger(ApprovalServiceImpl.class).warn("Could not notify requester: {}", notifEx.getMessage());
                }
            }
        }
        auditLogService.log(managerEmail, "APPROVAL_" + status, "ApprovalRequest", request.getId(), request.getRequestType() + " - " + request.getTargetReference());
        return request;
    }

    /** Unpacks "key=value|key2=value2" remarks (same convention used to stage proposed field changes for gated updates). */
    private Map<String, String> parsePipeDelimited(String packed) {
        Map<String, String> result = new HashMap<>();
        if (packed == null || packed.isBlank()) {
            return result;
        }
        for (String part : packed.split("\\|")) {
            int idx = part.indexOf('=');
            if (idx > 0) {
                result.put(part.substring(0, idx), part.substring(idx + 1));
            }
        }
        return result;
    }

    private void execute(ApprovalRequest request, String managerEmail, boolean approved, String remarks) {
        switch (request.getRequestType()) {
            case "ACCOUNT_OPEN" -> {
                if (approved) {
                    accountService.approveAccount(request.getTargetId(), managerEmail);
                } else {
                    accountService.rejectAccount(request.getTargetId(), managerEmail,
                            remarks != null ? remarks : "Rejected by manager");
                }
            }
            case "ACCOUNT_UPDATE" -> {
                if (approved) {
                    Map<String, String> proposed = parsePipeDelimited(request.getRemarks());
                    Integer branchId = proposed.get("branchId") != null && !proposed.get("branchId").isBlank()
                            ? Integer.parseInt(proposed.get("branchId")) : null;
                    java.math.BigDecimal monthlyIncome = proposed.get("monthlyIncome") != null && !proposed.get("monthlyIncome").isBlank()
                            ? new java.math.BigDecimal(proposed.get("monthlyIncome")) : null;
                    accountService.applyAccountUpdate(
                            request.getTargetId(),
                            proposed.get("accountType"),
                            branchId,
                            proposed.get("nomineeName"),
                            proposed.get("nomineeRelationship"),
                            proposed.get("occupation"),
                            monthlyIncome
                    );
                }
            }
            case "ACCOUNT_FREEZE" -> {
                if (approved) {
                    accountService.freezeAccount(request.getTargetId(), managerEmail);
                }
            }
            case "ACCOUNT_UNFREEZE" -> {
                if (approved) {
                    accountService.unfreezeAccount(request.getTargetId(), managerEmail);
                }
            }
            case "ACCOUNT_CLOSE" -> {
                if (approved) {
                    accountService.approveClosure(request.getTargetId(), managerEmail);
                }
            }
            case "TRANSFER_CANCEL" -> {
                if (approved) {
                    transactionService.cancelPendingTransfer(request.getTargetId(), managerEmail);
                }
            }
            case "TRANSFER_APPROVE" -> {
                if (approved) {
                    transactionService.approveAndExecuteTransfer(request.getTargetId(), managerEmail);
                } else {
                    transactionService.cancelPendingTransfer(request.getTargetId(), managerEmail);
                }
            }
            case "TXN_CORRECTION" -> {
                if (approved) {
                    transactionService.approveCorrection(request.getTargetId(), managerEmail);
                } else {
                    transactionService.rejectCorrection(request.getTargetId(), managerEmail, remarks);
                }
            }
            case "TXN_DELETION" -> {
                if (approved) {
                    transactionService.approveTransactionDeletion(request.getTargetId(), managerEmail);
                }
            }
            case "REPORT_PUBLISH" -> {
                PublishedReport report = publishedReportRepository.findById(request.getTargetId())
                        .orElseThrow(() -> new IllegalArgumentException("Report not found"));
                report.setStatus(approved ? "PUBLISHED" : "REJECTED");
                publishedReportRepository.save(report);
            }
            case "REPORT_ARCHIVE" -> {
                if (approved) {
                    PublishedReport report = publishedReportRepository.findById(request.getTargetId())
                            .orElseThrow(() -> new IllegalArgumentException("Report not found"));
                    report.setStatus("ARCHIVED");
                    publishedReportRepository.save(report);
                }
            }
            case "CUSTOMER_RECORD_UPDATE" -> {
                if (approved) {
                    Map<String, String> proposed = parsePipeDelimited(request.getRemarks());
                    User target = userService.getById(request.getTargetId());
                    java.time.LocalDate existingDob = null;
                    String existingPhoto = null;
                    if (target instanceof com.lankatrust.smartbank.entity.Customer c) {
                        existingDob = c.getDob();
                        existingPhoto = c.getProfilePhotoPath();
                    }
                    userService.updateProfile(request.getTargetId(),
                            proposed.getOrDefault("fullName", target.getFullName()),
                            proposed.getOrDefault("phone", target.getPhoneNumber()),
                            proposed.get("address"),
                            existingDob,
                            existingPhoto);
                }
            }
            case "CUSTOMER_RECORD_DELETION" -> {
                if (approved) {
                    userService.setEnabled(request.getTargetId(), false, managerEmail);
                }
            }
            case "STAFF_CREATE" -> {
                if (approved) {
                    StaffCreationRequest pending = staffCreationRequestRepository.findById(request.getTargetId())
                            .orElseThrow(() -> new IllegalArgumentException("Staff creation request not found"));
                    userService.createStaffUserFromHash(pending.getFullName(), pending.getEmail(), pending.getPasswordHash(),
                            pending.getRole(), pending.getNic(), pending.getEmployeeId(), pending.getPhone(),
                            pending.getBranch(), managerEmail);
                    pending.setStatus("APPROVED");
                    staffCreationRequestRepository.save(pending);
                } else {
                    StaffCreationRequest pending = staffCreationRequestRepository.findById(request.getTargetId())
                            .orElseThrow(() -> new IllegalArgumentException("Staff creation request not found"));
                    pending.setStatus("REJECTED");
                    staffCreationRequestRepository.save(pending);
                }
            }
            case "STAFF_ROLE_UPDATE" -> {
                if (approved) {
                    Map<String, String> proposed = parsePipeDelimited(request.getRemarks());
                    String roleName = proposed.get("role");
                    if (roleName != null) {
                        userService.updateRole(request.getTargetId(),
                                com.lankatrust.smartbank.entity.Role.valueOf(roleName), managerEmail);
                    }
                }
            }
            case "STAFF_DELETE" -> {
                if (approved) {
                    userService.setEnabled(request.getTargetId(), false, managerEmail);
                }
            }
            case "TEMPLATE_CREATE" -> {
                if (approved) {
                    Map<String, String> proposed = parsePipeDelimited(request.getRemarks());
                    com.lankatrust.smartbank.entity.NotificationChannel ch = com.lankatrust.smartbank.entity.NotificationChannel.IN_APP;
                    if (proposed.get("channel") != null) {
                        try {
                            ch = com.lankatrust.smartbank.entity.NotificationChannel.valueOf(proposed.get("channel"));
                        } catch (Exception ignored) {}
                    }
                    com.lankatrust.smartbank.entity.NotificationTemplate template = com.lankatrust.smartbank.entity.NotificationTemplate.builder()
                            .templateKey(proposed.getOrDefault("templateKey", "TPL_" + System.currentTimeMillis()))
                            .name(proposed.getOrDefault("name", "Notification Template"))
                            .subject(proposed.get("subject"))
                            .body(proposed.getOrDefault("body", ""))
                            .channel(ch)
                            .updatedBy(userService.getByEmail(managerEmail))
                            .updatedAt(LocalDateTime.now())
                            .build();
                    notificationTemplateRepository.save(template);
                }
            }
            case "TEMPLATE_UPDATE" -> {
                if (approved) {
                    Map<String, String> proposed = parsePipeDelimited(request.getRemarks());
                    notificationTemplateRepository.findById(request.getTargetId()).ifPresent(template -> {
                        if (proposed.containsKey("subject")) template.setSubject(proposed.get("subject"));
                        if (proposed.containsKey("body")) template.setBody(proposed.get("body"));
                        template.setUpdatedBy(userService.getByEmail(managerEmail));
                        template.setUpdatedAt(LocalDateTime.now());
                        notificationTemplateRepository.save(template);
                    });
                }
            }
            case "TEMPLATE_DELETE" -> {
                if (approved) {
                    notificationTemplateRepository.deleteById(request.getTargetId());
                }
            }
            case "REPORT_DELETE" -> {
                if (approved) {
                    publishedReportRepository.deleteById(request.getTargetId());
                }
            }
            case "AUDIT_LOG_DELETION" -> {
                if (approved) {
                    auditLogRepository.deleteById(request.getTargetId());
                }
            }
            default -> {
            }
        }
    }
}

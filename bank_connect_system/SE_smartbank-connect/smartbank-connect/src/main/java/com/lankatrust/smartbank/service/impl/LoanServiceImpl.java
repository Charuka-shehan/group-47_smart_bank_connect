package com.lankatrust.smartbank.service.impl;

import com.lankatrust.smartbank.entity.*;
import com.lankatrust.smartbank.repository.LoanRepository;
import com.lankatrust.smartbank.repository.UserRepository;
import com.lankatrust.smartbank.service.AuditLogService;
import com.lankatrust.smartbank.service.LoanService;
import com.lankatrust.smartbank.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Member 4 (Farwin M. N. S.) - Loan Request & Approval.
 * Workflow (Section 6.4): Customer submits application & documents ->
 * system checks completeness -> Officer verifies -> Compliance Officer
 * checks identity/income/credit history -> Manager final approval ->
 * customer notified at each stage.
 */
@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final com.lankatrust.smartbank.service.ValidationService validationService;

    private static final SecureRandom RANDOM = new SecureRandom();

    private String generateApplicationNumber() {
        return "LN" + System.currentTimeMillis() + RANDOM.nextInt(1000);
    }

    @Override
    @Transactional
    public Loan submitApplication(Long customerId, String loanType, BigDecimal amount, BigDecimal monthlyIncome,
                                   String employmentStatus, String documentsSummary) {
        if (loanType == null || loanType.isBlank()) {
            throw new IllegalArgumentException("Loan type is required.");
        }
        validationService.validateLoanAmount(amount);
        validationService.validateLoanIncome(monthlyIncome);
        if (employmentStatus == null || employmentStatus.isBlank()) {
            throw new IllegalArgumentException("Employment status is required.");
        }
        if (documentsSummary == null || documentsSummary.isBlank()) {
            throw new IllegalArgumentException("Documents summary is required.");
        }

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        validationService.validateLoanApplicantEligibility(customer);

        Loan loan = Loan.builder()
                .applicationNumber(generateApplicationNumber())
                .customer(customer)
                .loanType(loanType.trim())
                .requestedAmount(amount)
                .monthlyIncome(monthlyIncome)
                .employmentStatus(employmentStatus.trim())
                .documentsSummary(documentsSummary.trim())
                .status(LoanStatus.SUBMITTED)
                .createdAt(LocalDateTime.now())
                .build();
        Loan saved = loanRepository.save(loan);

        auditLogService.log(customer.getEmail(), "LOAN_SUBMITTED", "Loan", saved.getId(),
                loanType + " loan application for Rs. " + amount);
        notificationService.send(customer, "Loan Application Submitted",
                "Your application " + saved.getApplicationNumber() + " has been received and is under review.",
                NotificationChannel.EMAIL);
        return saved;
    }

    @Override
    @Transactional
    public Loan verify(Long loanId, String officerEmail, String remarks) {
        Loan loan = getById(loanId);
        if (loan.getStatus() != LoanStatus.SUBMITTED) {
            throw new IllegalStateException("Loan application cannot be verified from status: " + loan.getStatus());
        }
        loan.setStatus(LoanStatus.UNDER_VERIFICATION);
        loan.setVerifiedBy(userRepository.findByEmail(officerEmail).orElse(null));
        loan.setRemarks(remarks);
        loan.setUpdatedAt(LocalDateTime.now());
        Loan saved = loanRepository.save(loan);

        auditLogService.log(officerEmail, "LOAN_VERIFIED", "Loan", saved.getId(), remarks);
        notificationService.send(saved.getCustomer(), "Loan Application Update",
                "Application " + saved.getApplicationNumber() + " is now under document verification.",
                NotificationChannel.IN_APP);
        return saved;
    }

    @Override
    @Transactional
    public Loan complianceCheck(Long loanId, String complianceOfficerEmail, String remarks) {
        Loan loan = getById(loanId);
        if (loan.getStatus() != LoanStatus.UNDER_VERIFICATION) {
            throw new IllegalStateException("Compliance check requires loan to be UNDER_VERIFICATION, current status is: " + loan.getStatus());
        }
        loan.setStatus(LoanStatus.COMPLIANCE_CHECK);
        loan.setRemarks(remarks);
        loan.setUpdatedAt(LocalDateTime.now());
        Loan saved = loanRepository.save(loan);

        auditLogService.log(complianceOfficerEmail, "LOAN_COMPLIANCE_CHECKED", "Loan", saved.getId(), remarks);
        notificationService.send(saved.getCustomer(), "Loan Application Update",
                "Application " + saved.getApplicationNumber() + " has passed compliance review and awaits final approval.",
                NotificationChannel.IN_APP);
        return saved;
    }

    @Override
    @Transactional
    public Loan approve(Long loanId, String managerEmail) {
        Loan loan = getById(loanId);
        if (loan.getStatus() == LoanStatus.APPROVED) {
            throw new IllegalStateException("This loan application has already been approved.");
        }
        if (loan.getStatus() == LoanStatus.REJECTED || loan.getStatus() == LoanStatus.CANCELLED) {
            throw new IllegalStateException("Cannot approve loan application with status: " + loan.getStatus());
        }
        if (loan.getStatus() != LoanStatus.COMPLIANCE_CHECK) {
            throw new IllegalStateException("Cannot approve loan application directly without prior review. Current status: " + loan.getStatus());
        }

        User manager = userRepository.findByEmail(managerEmail).orElse(null);
        if (loan.getCustomer() != null && manager != null && loan.getCustomer().getId().equals(manager.getId())) {
            throw new IllegalStateException("This request cannot be approved by its requester. A different authorized Manager is required. (Requesters cannot approve their own requests. Dual authorization is required.)");
        }

        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovedBy(manager);
        loan.setUpdatedAt(LocalDateTime.now());
        Loan saved = loanRepository.save(loan);

        auditLogService.log(managerEmail, "LOAN_APPROVED", "Loan", saved.getId(),
                "Approved Rs. " + saved.getRequestedAmount());
        notificationService.send(saved.getCustomer(), "Loan Approved",
                "Congratulations! Your loan application " + saved.getApplicationNumber() + " has been approved.",
                NotificationChannel.SMS);
        return saved;
    }

    @Override
    @Transactional
    public Loan reject(Long loanId, String reviewerEmail, String reason) {
        Loan loan = getById(loanId);
        if (loan.getStatus() == LoanStatus.APPROVED || loan.getStatus() == LoanStatus.REJECTED || loan.getStatus() == LoanStatus.CANCELLED) {
            throw new IllegalStateException("Loan application has already been finalized with status: " + loan.getStatus());
        }
        if (reason == null || reason.trim().isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required.");
        }

        loan.setStatus(LoanStatus.REJECTED);
        loan.setRemarks(reason);
        loan.setUpdatedAt(LocalDateTime.now());
        Loan saved = loanRepository.save(loan);

        auditLogService.log(reviewerEmail, "LOAN_REJECTED", "Loan", saved.getId(), reason);
        notificationService.send(saved.getCustomer(), "Loan Application Update",
                "Application " + saved.getApplicationNumber() + " was not approved. Reason: " + reason,
                NotificationChannel.EMAIL);
        return saved;
    }

    @Override
    @Transactional
    public Loan cancel(Long loanId, Long customerId) {
        Loan loan = getById(loanId);
        if (!loan.getCustomer().getId().equals(customerId)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: You do not have permission to cancel this loan application.");
        }
        if (loan.getStatus() != LoanStatus.SUBMITTED) {
            throw new IllegalStateException("Loan application cannot be cancelled after review has started. Current status: " + loan.getStatus());
        }
        loan.setStatus(LoanStatus.CANCELLED);
        loan.setUpdatedAt(LocalDateTime.now());
        Loan saved = loanRepository.save(loan);
        auditLogService.log(loan.getCustomer().getEmail(), "LOAN_CANCELLED", "Loan", saved.getId(), "Application cancelled by applicant");
        return saved;
    }

    @Override
    @Transactional
    public Loan updateApplication(Long loanId, Long customerId, String loanType, BigDecimal amount,
                                  BigDecimal monthlyIncome, String employmentStatus, String documentsSummary) {
        Loan loan = getById(loanId);
        if (!loan.getCustomer().getId().equals(customerId)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: You do not have permission to update this loan application.");
        }
        if (loan.getStatus() != LoanStatus.SUBMITTED) {
            throw new IllegalStateException("Loan application can only be edited while in SUBMITTED status before review begins. Current status: " + loan.getStatus());
        }
        validationService.validateLoanAmount(amount);
        validationService.validateLoanIncome(monthlyIncome);
        if (loanType != null && !loanType.isBlank()) {
            loan.setLoanType(loanType.trim().toUpperCase());
        }
        loan.setRequestedAmount(amount);
        loan.setMonthlyIncome(monthlyIncome);
        if (employmentStatus != null && !employmentStatus.isBlank()) {
            loan.setEmploymentStatus(employmentStatus.trim());
        }
        if (documentsSummary != null) {
            loan.setDocumentsSummary(documentsSummary.trim());
        }
        loan.setUpdatedAt(LocalDateTime.now());
        Loan saved = loanRepository.save(loan);
        auditLogService.log(loan.getCustomer().getEmail(), "LOAN_APPLICATION_UPDATED", "Loan", saved.getId(),
                "Updated requested amount to Rs. " + amount);
        return saved;
    }

    @Override
    public List<Loan> getForCustomer(Long customerId) {
        return loanRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Loan> getAll() {
        return loanRepository.findAll();
    }

    @Override
    public Loan getById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Loan application not found: " + id));
    }
}

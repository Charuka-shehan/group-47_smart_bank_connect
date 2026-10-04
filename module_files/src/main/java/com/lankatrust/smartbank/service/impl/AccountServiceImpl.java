package com.lankatrust.smartbank.service.impl;

import com.lankatrust.smartbank.entity.*;
import com.lankatrust.smartbank.repository.*;
import com.lankatrust.smartbank.service.AccountService;
import com.lankatrust.smartbank.service.AuditLogService;
import com.lankatrust.smartbank.service.FileStorageService;
import com.lankatrust.smartbank.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final AtmCardRepository atmCardRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionCorrectionRepository transactionCorrectionRepository;
    private final StatementRequestRepository statementRequestRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;
    private final com.lankatrust.smartbank.service.ValidationService validationService;

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final BigDecimal MIN_SAVINGS_DEPOSIT = new BigDecimal("1000.00");
    private static final BigDecimal MIN_CURRENT_DEPOSIT = new BigDecimal("5000.00");
    private static final BigDecimal MIN_FIXED_DEPOSIT = new BigDecimal("50000.00");

    private String generateAccountNumber() {
        String number;
        do {
            number = "LTB" + String.format("%09d", RANDOM.nextInt(1_000_000_000));
        } while (accountRepository.findByAccountNumber(number).isPresent());
        return number;
    }

    private String generateCardNumber() {
        String number;
        do {
            number = "4" + String.format("%015d", RANDOM.nextLong(1_000_000_000_000_000L));
        } while (atmCardRepository.findByCardNumber(number).isPresent());
        return number;
    }

    @Override
    @Transactional
    public Account submitNewAccount(Long customerId, String accountType, String submittedByEmail) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        User officer = userRepository.findByEmail(submittedByEmail)
                .orElseThrow(() -> new IllegalArgumentException("Officer not found"));

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .customer(customer)
                .accountType(accountType)
                .status(AccountStatus.PENDING_APPROVAL)
                .submittedBy(officer)
                .createdAt(LocalDateTime.now())
                .build();
        Account saved = accountRepository.save(account);

        createApprovalRequest(saved, "ACCOUNT_OPEN", officer);

        auditLogService.log(submittedByEmail, "ACCOUNT_SUBMITTED", "Account", saved.getId(),
                "New " + accountType + " account submitted for customer #" + customerId);
        return saved;
    }

    @Override
    @Transactional
    public Account openAccount(String customerId, String fullName, String email, String phone, String nic,
                               LocalDate dob, String address, String occupation, BigDecimal monthlyIncome,
                               String accountType, BigDecimal initialDeposit, Integer branchId,
                               String nomineeName, String nomineeRelationship,
                               Map<String, MultipartFile> documents,
                               String submittedByEmail) {
        User officer = userRepository.findByEmail(submittedByEmail)
                .orElseThrow(() -> new IllegalArgumentException("Officer not found"));
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found"));

        validationService.validateRequired(customerId, "Customer ID");
        validationService.validateRequired(fullName, "Full name");
        validationService.validateEmail(email);
        validationService.validateEmailNotExists(email);
        validationService.validatePhoneNumber(phone);
        validationService.validateNic(nic);
        validationService.validateNicNotExists(nic);
        validationService.validateAge(dob, 18);
        validationService.validateRequired(address, "Address");
        validationService.validateRequired(occupation, "Occupation");
        validationService.validateLoanIncome(monthlyIncome);
        validationService.validateInitialDeposit(accountType, initialDeposit);

        Customer customer = Customer.builder()
                .fullName(fullName)
                .email(email)
                .phoneNumber(phone)
                .nic(nic)
                .dob(dob)
                .address(address)
                .customerId(customerId)
                .kycStatus("PENDING")
                .branch(branch)
                .role(Role.CUSTOMER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
        customer = customerRepository.save(customer);

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .customer(customer)
                .accountType(accountType)
                .balance(BigDecimal.ZERO)
                .initialDeposit(initialDeposit != null ? initialDeposit : BigDecimal.ZERO)
                .status(AccountStatus.PENDING_APPROVAL)
                .branch(branch)
                .occupation(occupation)
                .monthlyIncome(monthlyIncome)
                .nomineeName(nomineeName)
                .nomineeRelationship(nomineeRelationship)
                .signaturePath(storeDocument(documents.get("signature"), "signatures"))
                .nicFrontPath(storeDocument(documents.get("nicFront"), "nic"))
                .nicBackPath(storeDocument(documents.get("nicBack"), "nic"))
                .proofOfAddressPath(storeDocument(documents.get("proofOfAddress"), "address"))
                .submittedBy(officer)
                .createdAt(LocalDateTime.now())
                .build();
        Account saved = accountRepository.save(account);

        createApprovalRequest(saved, "ACCOUNT_OPEN", officer);

        auditLogService.log(submittedByEmail, "ACCOUNT_OPEN_SUBMITTED", "Account", saved.getId(),
                "Account opening request submitted for " + fullName);
        notificationService.send(officer, "Account Opening Submitted",
                "Account " + saved.getAccountNumber() + " submitted for manager approval.",
                NotificationChannel.IN_APP);
        return saved;
    }

    private String storeDocument(MultipartFile file, String documentType) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return fileStorageService.store(file, documentType);
    }

    private void validateMinimumDeposit(String accountType, BigDecimal initialDeposit) {
        BigDecimal required = switch (accountType.toUpperCase()) {
            case "SAVINGS" -> MIN_SAVINGS_DEPOSIT;
            case "CURRENT" -> MIN_CURRENT_DEPOSIT;
            case "FIXED_DEPOSIT" -> MIN_FIXED_DEPOSIT;
            default -> BigDecimal.ZERO;
        };
        if (initialDeposit == null || initialDeposit.compareTo(required) < 0) {
            throw new IllegalArgumentException(
                    "Minimum initial deposit for " + accountType + " is LKR " + required);
        }
    }

    private void createApprovalRequest(Account account, String requestType, User requester) {
        ApprovalRequest request = ApprovalRequest.builder()
                .requestType(requestType)
                .targetId(account.getId())
                .targetReference(account.getAccountNumber())
                .requester(requester)
                .status("PENDING")
                .requestedAt(LocalDateTime.now())
                .build();
        approvalRequestRepository.save(request);
    }

    @Override
    @Transactional
    public Account approveAccount(Long accountId, String approverEmail) {
        Account account = getById(accountId);
        if (account.getStatus() != AccountStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("This account is no longer pending approval.");
        }
        User manager = userRepository.findByEmail(approverEmail)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found"));

        if (account.getSubmittedBy() != null && manager.getId() != null
                && account.getSubmittedBy().getId().equals(manager.getId())) {
            throw new IllegalStateException("This request cannot be approved by its requester. A different authorized Manager is required. (Requesters cannot approve their own requests. Dual authorization is required.)");
        }

        account.setStatus(AccountStatus.ACTIVE);
        account.setApprovedBy(manager);
        account.setBalance(account.getInitialDeposit());
        account.setUpdatedAt(LocalDateTime.now());
        Account saved = accountRepository.save(account);

        ApprovalRequest request = approvalRequestRepository.findByRequestTypeAndStatusOrderByRequestedAtDesc(
                "ACCOUNT_OPEN", "PENDING").stream()
                .filter(r -> r.getTargetId().equals(saved.getId()))
                .findFirst()
                .orElse(null);
        if (request != null) {
            request.setStatus("APPROVED");
            request.setApprovedBy(manager);
            request.setApprovedAt(LocalDateTime.now());
            request.setExecutionStatus("EXECUTED");
            request.setExecutedAt(LocalDateTime.now());
            approvalRequestRepository.save(request);
        }

        generateAtmCard(saved);

        if (saved.getCustomer() != null) {
            customerRepository.findById(saved.getCustomer().getId()).ifPresent(customer -> {
                customer.setKycStatus("APPROVED");
                customerRepository.save(customer);
            });
        }

        auditLogService.log(approverEmail, "ACCOUNT_APPROVED", "Account", saved.getId(),
                "Account " + saved.getAccountNumber() + " activated");
        notificationService.send(saved.getCustomer(), "Account Activated",
                "Your " + saved.getAccountType() + " account " + saved.getAccountNumber() + " is now active.",
                NotificationChannel.EMAIL);
        return saved;
    }

    @Override
    @Transactional
    public Account rejectAccount(Long accountId, String approverEmail, String reason) {
        Account account = getById(accountId);
        if (account.getStatus() != AccountStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("This account is no longer pending approval.");
        }
        User manager = userRepository.findByEmail(approverEmail)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found"));

        account.setStatus(AccountStatus.REJECTED);
        account.setRejectionReason(reason);
        account.setUpdatedAt(LocalDateTime.now());
        Account saved = accountRepository.save(account);

        ApprovalRequest request = approvalRequestRepository.findByRequestTypeAndStatusOrderByRequestedAtDesc(
                "ACCOUNT_OPEN", "PENDING").stream()
                .filter(r -> r.getTargetId().equals(saved.getId()))
                .findFirst()
                .orElse(null);
        if (request != null) {
            request.setStatus("REJECTED");
            request.setApprovedBy(manager);
            request.setRejectedAt(LocalDateTime.now());
            request.setRejectionReason(reason);
            request.setExecutionStatus("REJECTED");
            approvalRequestRepository.save(request);
        }

        auditLogService.log(approverEmail, "ACCOUNT_REJECTED", "Account", saved.getId(), reason);
        notificationService.send(saved.getCustomer(), "Account Opening Rejected",
                "Your account opening request was rejected. Reason: " + reason,
                NotificationChannel.EMAIL);
        return saved;
    }

    private void generateAtmCard(Account account) {
        String cvv = String.format("%03d", RANDOM.nextInt(1000));
        AtmCard card = AtmCard.builder()
                .account(account)
                .cardNumber(generateCardNumber())
                .cvvHash(passwordEncoder.encode(cvv))
                .expiryDate(LocalDate.now().plusYears(5).withDayOfMonth(1))
                .status("ACTIVE")
                .issuedAt(LocalDateTime.now())
                .build();
        atmCardRepository.save(card);
    }

    @Override
    @Transactional
    public Account requestFreeze(Long accountId, String officerEmail) {
        Account account = getById(accountId);
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Only active accounts can be frozen.");
        }
        User officer = userRepository.findByEmail(officerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Officer not found"));
        createApprovalRequest(account, "ACCOUNT_FREEZE", officer);
        auditLogService.log(officerEmail, "ACCOUNT_FREEZE_REQUESTED", "Account", account.getId(),
                account.getAccountNumber());
        notificationService.send(officer, "Freeze Request Submitted",
                "Freeze request for " + account.getAccountNumber() + " awaits manager approval.",
                NotificationChannel.IN_APP);
        return account;
    }

    @Override
    @Transactional
    public Account requestUnfreeze(Long accountId, String officerEmail) {
        Account account = getById(accountId);
        if (account.getStatus() != AccountStatus.FROZEN) {
            throw new IllegalStateException("Only frozen accounts can be unfrozen.");
        }
        User officer = userRepository.findByEmail(officerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Officer not found"));
        createApprovalRequest(account, "ACCOUNT_UNFREEZE", officer);
        auditLogService.log(officerEmail, "ACCOUNT_UNFREEZE_REQUESTED", "Account", account.getId(),
                account.getAccountNumber());
        return account;
    }

    @Override
    @Transactional
    public Account freezeAccount(Long accountId, String officerEmail) {
        Account account = getById(accountId);
        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new IllegalStateException("Account is already frozen.");
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Only active accounts can be frozen.");
        }
        account.setStatus(AccountStatus.FROZEN);
        account.setUpdatedAt(LocalDateTime.now());
        Account saved = accountRepository.save(account);

        auditLogService.log(officerEmail, "ACCOUNT_FROZEN", "Account", saved.getId(),
                "Account " + saved.getAccountNumber() + " frozen");
        notificationService.send(saved.getCustomer(), "Account Frozen",
                "Your account " + saved.getAccountNumber() + " has been temporarily frozen. Contact support for details.",
                NotificationChannel.SMS);
        return saved;
    }

    @Override
    @Transactional
    public Account unfreezeAccount(Long accountId, String officerEmail) {
        Account account = getById(accountId);
        if (account.getStatus() == AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is already active.");
        }
        if (account.getStatus() != AccountStatus.FROZEN) {
            throw new IllegalStateException("Only frozen accounts can be unfrozen.");
        }
        account.setStatus(AccountStatus.ACTIVE);
        account.setUpdatedAt(LocalDateTime.now());
        Account saved = accountRepository.save(account);

        auditLogService.log(officerEmail, "ACCOUNT_UNFROZEN", "Account", saved.getId(),
                "Account " + saved.getAccountNumber() + " reactivated");
        return saved;
    }

    @Override
    @Transactional
    public Account requestClosure(Long accountId, String officerEmail) {
        Account account = getById(accountId);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalStateException("Account is already closed.");
        }
        User officer = userRepository.findByEmail(officerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Officer not found"));
        createApprovalRequest(account, "ACCOUNT_CLOSE", officer);
        auditLogService.log(officerEmail, "ACCOUNT_CLOSURE_REQUESTED", "Account", account.getId(),
                "Closure requested for " + account.getAccountNumber() + ", pending Manager approval");
        notificationService.send(officer, "Closure Request Submitted",
                "Closure request for " + account.getAccountNumber() + " awaits manager approval.",
                NotificationChannel.IN_APP);
        return account;
    }

    @Override
    @Transactional
    public Account approveClosure(Long accountId, String approverEmail) {
        Account account = getById(accountId);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalStateException("Account is already closed.");
        }
        User manager = userRepository.findByEmail(approverEmail)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found"));

        account.setStatus(AccountStatus.CLOSED);
        account.setApprovedBy(manager);
        account.setUpdatedAt(LocalDateTime.now());
        Account saved = accountRepository.save(account);

        auditLogService.log(approverEmail, "ACCOUNT_CLOSED", "Account", saved.getId(),
                "Account " + saved.getAccountNumber() + " closed");
        notificationService.send(saved.getCustomer(), "Account Closed",
                "Your account " + saved.getAccountNumber() + " has been closed as requested.",
                NotificationChannel.EMAIL);
        return saved;
    }

    @Override
    public List<Account> getAll() {
        return accountRepository.findAll();
    }

    @Override
    public List<Account> getForCustomer(Long customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    @Override
    public Account getById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
    }

    @Override
    public Account createForCustomer(Long customerId, String accountType) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .customer(customer)
                .accountType(accountType)
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        Account saved = accountRepository.save(account);
        auditLogService.log("system", "ACCOUNT_CREATED", "Account", saved.getId(), "Account created for customer " + customerId);
        notificationService.send(saved.getCustomer(), "Account Created", "Your account " + saved.getAccountNumber() + " has been created.", NotificationChannel.EMAIL);
        return saved;
    }

    @Override
    @Transactional
    public ApprovalRequest requestAccountUpdate(Long accountId, String accountType, Integer branchId,
                                                String nomineeName, String nomineeRelationship,
                                                String occupation, BigDecimal monthlyIncome, String officerEmail) {
        Account account = getById(accountId);
        User officer = userRepository.findByEmail(officerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Officer not found"));

        StringBuilder proposed = new StringBuilder();
        if (accountType != null && !accountType.isBlank()) proposed.append("accountType=").append(accountType.trim()).append("|");
        if (branchId != null) proposed.append("branchId=").append(branchId).append("|");
        if (nomineeName != null && !nomineeName.isBlank()) proposed.append("nomineeName=").append(nomineeName.trim()).append("|");
        if (nomineeRelationship != null && !nomineeRelationship.isBlank()) proposed.append("nomineeRelationship=").append(nomineeRelationship.trim()).append("|");
        if (occupation != null && !occupation.isBlank()) proposed.append("occupation=").append(occupation.trim()).append("|");
        if (monthlyIncome != null) proposed.append("monthlyIncome=").append(monthlyIncome).append("|");

        ApprovalRequest request = ApprovalRequest.builder()
                .requestType("ACCOUNT_UPDATE")
                .targetId(account.getId())
                .targetReference(account.getAccountNumber())
                .requester(officer)
                .status("PENDING")
                .remarks(proposed.toString())
                .requestedAt(LocalDateTime.now())
                .build();
        ApprovalRequest saved = approvalRequestRepository.save(request);
        auditLogService.log(officerEmail, "ACCOUNT_UPDATE_REQUESTED", "Account", account.getId(),
                "Update requested for " + account.getAccountNumber() + ", pending Manager approval");
        return saved;
    }

    @Override
    @Transactional
    public Account applyAccountUpdate(Long accountId, String accountType, Integer branchId,
                                      String nomineeName, String nomineeRelationship,
                                      String occupation, BigDecimal monthlyIncome) {
        Account account = getById(accountId);
        if (accountType != null && !accountType.isBlank()) {
            account.setAccountType(accountType.trim());
        }
        if (branchId != null) {
            branchRepository.findById(branchId).ifPresent(account::setBranch);
        }
        if (nomineeName != null) {
            account.setNomineeName(nomineeName.trim());
        }
        if (nomineeRelationship != null) {
            account.setNomineeRelationship(nomineeRelationship.trim());
        }
        if (occupation != null) {
            account.setOccupation(occupation.trim());
        }
        if (monthlyIncome != null) {
            account.setMonthlyIncome(monthlyIncome);
        }
        account.setUpdatedAt(LocalDateTime.now());
        Account saved = accountRepository.save(account);
        auditLogService.log("system", "ACCOUNT_UPDATED", "Account", saved.getId(),
                "Account " + saved.getAccountNumber() + " updated after manager approval");
        return saved;
    }

    @Override
    @Transactional
    public void deleteAccountPermanently(Long accountId, String actorEmail) {
        Account account = getById(accountId);
        String accountNumber = account.getAccountNumber();

        // 1. Remove associated ATM cards
        atmCardRepository.findByAccountId(accountId).ifPresent(atmCardRepository::delete);

        // 2. Remove associated statement requests
        List<StatementRequest> stmtReqs = statementRequestRepository.findByAccountId(accountId);
        if (stmtReqs != null && !stmtReqs.isEmpty()) {
            statementRequestRepository.deleteAll(stmtReqs);
        }

        // 3. Remove transaction corrections for transactions involving this account
        List<Transaction> transactions = transactionRepository
                .findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(accountId, accountId);
        for (Transaction t : transactions) {
            List<TransactionCorrection> corrections = transactionCorrectionRepository.findByTransactionId(t.getId());
            if (corrections != null && !corrections.isEmpty()) {
                transactionCorrectionRepository.deleteAll(corrections);
            }
        }

        // 4. Remove transactions
        if (transactions != null && !transactions.isEmpty()) {
            transactionRepository.deleteAll(transactions);
        }

        // 5. Clean up any rows in legacy transfers table if existing
        try {
            jdbcTemplate.update("DELETE FROM transfers WHERE from_account_id = ?", accountId);
        } catch (Exception ignored) {
        }

        // 6. Remove approval requests for this account
        List<ApprovalRequest> approvals = approvalRequestRepository.findByTargetId(accountId);
        if (approvals != null && !approvals.isEmpty()) {
            List<ApprovalRequest> accountApprovals = approvals.stream()
                    .filter(req -> req.getRequestType() != null && req.getRequestType().startsWith("ACCOUNT_"))
                    .toList();
            approvalRequestRepository.deleteAll(accountApprovals);
        }

        // 7. Remove any saved beneficiaries referencing this account number
        List<Beneficiary> beneficiaries = beneficiaryRepository.findByBeneficiaryAccountNumber(accountNumber);
        if (beneficiaries != null && !beneficiaries.isEmpty()) {
            beneficiaryRepository.deleteAll(beneficiaries);
        }

        // 8. Delete the account itself from the database
        accountRepository.delete(account);

        // 9. Audit log entry for permanent account deletion
        auditLogService.log(actorEmail != null ? actorEmail : "SYSTEM",
                "ACCOUNT_PERMANENTLY_DELETED", "Account", accountId,
                "Account " + accountNumber + " was permanently deleted from the database along with all dependent records.");
    }
}

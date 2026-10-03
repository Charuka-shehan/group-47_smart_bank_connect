package com.lankatrust.smartbank.config;

import com.lankatrust.smartbank.entity.*;
import com.lankatrust.smartbank.repository.AccountRepository;
import com.lankatrust.smartbank.repository.AtmCardRepository;
import com.lankatrust.smartbank.repository.BranchRepository;
import com.lankatrust.smartbank.repository.CustomerRepository;
import com.lankatrust.smartbank.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AtmCardRepository atmCardRepository;
    private final BranchRepository branchRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    private static final SecureRandom RANDOM = new SecureRandom();

    public DatabaseSeeder(UserRepository userRepository,
                          AccountRepository accountRepository,
                          AtmCardRepository atmCardRepository,
                          BranchRepository branchRepository,
                          CustomerRepository customerRepository,
                          PasswordEncoder passwordEncoder,
                          JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.atmCardRepository = atmCardRepository;
        this.branchRepository = branchRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @jakarta.annotation.PostConstruct
    public void onInit() {
        migrateSchemaIfNecessary();
    }

    @Override
    public void run(String... args) throws Exception {
        migrateSchemaIfNecessary();
        Branch branch = branchRepository.findByCode("CMB-MAIN")
                .orElseGet(() -> branchRepository.save(Branch.builder()
                        .code("CMB-MAIN")
                        .name("Colombo Main")
                        .address("No. 100, Galle Road, Colombo 03")
                        .build()));

        if (userRepository.count() == 0) {
            User manager = saveUser(User.builder()
                    .fullName("Nadeesha Perera")
                    .email("rscharukashehan@gmail.com")
                    .password(passwordEncoder.encode("Manager@123"))
                    .phoneNumber("0773117384")
                    .nic("850000001V")
                    .role(Role.BANK_MANAGER)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build(), 3);

            User admin = saveUser(User.builder()
                    .fullName("System Admin")
                    .email("rscharukashehanuni@gmail.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .phoneNumber("0773117385")
                    .nic("850000002V")
                    .role(Role.SYSTEM_ADMINISTRATOR)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build(), 4);

            User compliance = saveUser(User.builder()
                    .fullName("Compliance Officer")
                    .email("paramihimaya500@gmail.com")
                    .password(passwordEncoder.encode("Compliance@123"))
                    .phoneNumber("0773117386")
                    .nic("850000003V")
                    .role(Role.COMPLIANCE_OFFICER)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build(), 5);

            User relations = saveUser(User.builder()
                    .fullName("Ishani Fernando")
                    .email("himayaparami912@gmail.com")
                    .password(passwordEncoder.encode("Cre@123"))
                    .phoneNumber("0773117387")
                    .nic("850000005V")
                    .role(Role.CUSTOMER_RELATIONS_EXECUTIVE)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build(), 6);

            Officer officer = (Officer) saveUser(Officer.builder()
                    .fullName("Dilshan Perera")
                    .email("himayacharuka@gmail.com")
                    .password(passwordEncoder.encode("Officer@123"))
                    .phoneNumber("0773117388")
                    .nic("850000004V")
                    .role(Role.BANK_OFFICER)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .employeeId("EMP1001")
                    .branch("Colombo Main")
                    .build(), 2);

            Customer customer = (Customer) saveUser(Customer.builder()
                    .fullName("Charuka Shehan")
                    .email("charuhima390@gmail.com")
                    .password(passwordEncoder.encode("Customer@123"))
                    .phoneNumber("0773117389")
                    .nic("200212345678")
                    .role(Role.CUSTOMER)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .customerId(generateUniqueCustomerId())
                    .dob(LocalDate.of(2002, 5, 20))
                    .address("123, Galle Road, Colombo 03")
                    .kycStatus("APPROVED")
                    .branch(branch)
                    .build(), 1);

            Account account = Account.builder()
                    .accountNumber(generateAccountNumber())
                    .customer(customer)
                    .accountType("SAVINGS")
                    .balance(new BigDecimal("50000.00"))
                    .initialDeposit(new BigDecimal("50000.00"))
                    .status(AccountStatus.ACTIVE)
                    .branch(branch)
                    .occupation("Software Engineer")
                    .monthlyIncome(new BigDecimal("150000.00"))
                    .nomineeName("Samantha Perera")
                    .nomineeRelationship("Spouse")
                    .submittedBy(officer)
                    .approvedBy(manager)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            account = accountRepository.save(account);

            AtmCard atmCard = AtmCard.builder()
                    .account(account)
                    .cardNumber(generateCardNumber())
                    .cvvHash(passwordEncoder.encode(String.format("%03d", RANDOM.nextInt(1000))))
                    .expiryDate(LocalDate.now().plusYears(5).withDayOfMonth(1))
                    .status("ACTIVE")
                    .issuedAt(LocalDateTime.now())
                    .build();
            atmCardRepository.save(atmCard);
        }

        // Ensure secondary managers exist so that Dual Authorization / Four-Eyes Principle can be fulfilled
        if (userRepository.findByEmail("rscharukashehan@gmail.com").isEmpty()) {
            saveUser(User.builder()
                    .fullName("Kamal Gunaratne")
                    .email("rscharukashehan@gmail.com")
                    .password(passwordEncoder.encode("Manager@123"))
                    .phoneNumber("0773117399")
                    .nic("850000009V")
                    .role(Role.BANK_MANAGER)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build(), 3);
            org.slf4j.LoggerFactory.getLogger(DatabaseSeeder.class)
                    .info("[DATABASE SEEDER] Seeded secondary manager rscharukashehan@gmail.com for dual authorization.");
        }
        if (userRepository.findByEmail("rscharukashehan@gmail.com").isEmpty()) {
            saveUser(User.builder()
                    .fullName("Senior Manager")
                    .email("rscharukashehan@gmail.com")
                    .password(passwordEncoder.encode("Manager@123"))
                    .phoneNumber("0773117398")
                    .nic("850000008V")
                    .role(Role.BANK_MANAGER)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build(), 3);
            org.slf4j.LoggerFactory.getLogger(DatabaseSeeder.class)
                    .info("[DATABASE SEEDER] Seeded senior manager rscharukashehan@gmail.com for dual authorization.");
        }

        if (userRepository.findByEmail("charuhima390@gmail.com").isEmpty()) {
            Customer customer = (Customer) saveUser(Customer.builder()
                    .fullName("John Silva")
                    .email("charuhima390@gmail.com")
                    .password(passwordEncoder.encode("Customer@123"))
                    .phoneNumber("0773117390")
                    .nic("199512345678")
                    .role(Role.CUSTOMER)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .customerId(generateUniqueCustomerId())
                    .dob(LocalDate.of(1995, 3, 15))
                    .address("45, High Level Road, Colombo 05")
                    .kycStatus("APPROVED")
                    .branch(branch)
                    .build(), 1);

            Account account = Account.builder()
                    .accountNumber(generateAccountNumber())
                    .customer(customer)
                    .accountType("SAVINGS")
                    .balance(new BigDecimal("50000.00"))
                    .initialDeposit(new BigDecimal("50000.00"))
                    .status(AccountStatus.ACTIVE)
                    .branch(branch)
                    .occupation("Accountant")
                    .monthlyIncome(new BigDecimal("120000.00"))
                    .nomineeName("Mary Silva")
                    .nomineeRelationship("Spouse")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            account = accountRepository.save(account);

            AtmCard atmCard = AtmCard.builder()
                    .account(account)
                    .cardNumber(generateCardNumber())
                    .cvvHash(passwordEncoder.encode(String.format("%03d", RANDOM.nextInt(1000))))
                    .expiryDate(LocalDate.now().plusYears(5).withDayOfMonth(1))
                    .status("ACTIVE")
                    .issuedAt(LocalDateTime.now())
                    .build();
            atmCardRepository.save(atmCard);
            org.slf4j.LoggerFactory.getLogger(DatabaseSeeder.class)
                    .info("[DATABASE SEEDER] Seeded standard demo customer charuhima390@gmail.com with active account and card.");
        } else {
            // Ensure charuhima390@gmail.com has at least one active account for transfer testing
            userRepository.findByEmail("charuhima390@gmail.com").ifPresent(custUser -> {
                if (custUser instanceof Customer customer) {
                    if (accountRepository.findByCustomerId(customer.getId()).isEmpty()) {
                        Account account = Account.builder()
                                .accountNumber(generateAccountNumber())
                                .customer(customer)
                                .accountType("SAVINGS")
                                .balance(new BigDecimal("50000.00"))
                                .initialDeposit(new BigDecimal("50000.00"))
                                .status(AccountStatus.ACTIVE)
                                .branch(branch)
                                .occupation("Accountant")
                                .monthlyIncome(new BigDecimal("120000.00"))
                                .nomineeName("Mary Silva")
                                .nomineeRelationship("Spouse")
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                        account = accountRepository.save(account);

                        AtmCard atmCard = AtmCard.builder()
                                .account(account)
                                .cardNumber(generateCardNumber())
                                .cvvHash(passwordEncoder.encode(String.format("%03d", RANDOM.nextInt(1000))))
                                .expiryDate(LocalDate.now().plusYears(5).withDayOfMonth(1))
                                .status("ACTIVE")
                                .issuedAt(LocalDateTime.now())
                                .build();
                        atmCardRepository.save(atmCard);
                        org.slf4j.LoggerFactory.getLogger(DatabaseSeeder.class)
                                .info("[DATABASE SEEDER] Generated missing account and ATM card for charuhima390@gmail.com.");
                    }
                }
            });
        }

        if (userRepository.findByEmail("rscharukashehanuni@gmail.com").isEmpty()) {
            saveUser(User.builder()
                    .fullName("Administrator")
                    .email("rscharukashehanuni@gmail.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .phoneNumber("0773117391")
                    .nic("850000010V")
                    .role(Role.SYSTEM_ADMINISTRATOR)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build(), 4);
            org.slf4j.LoggerFactory.getLogger(DatabaseSeeder.class)
                    .info("[DATABASE SEEDER] Seeded rscharukashehanuni@gmail.com.");
        }

        if (userRepository.findByEmail("paramihimaya500@gmail.com").isEmpty()) {
            saveUser(User.builder()
                    .fullName("Compliance Officer")
                    .email("paramihimaya500@gmail.com")
                    .password(passwordEncoder.encode("Compliance@123"))
                    .phoneNumber("0773117392")
                    .nic("850000011V")
                    .role(Role.COMPLIANCE_OFFICER)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build(), 5);
            org.slf4j.LoggerFactory.getLogger(DatabaseSeeder.class)
                    .info("[DATABASE SEEDER] Seeded paramihimaya500@gmail.com.");
        }
        seedInitialApprovalRequests();
    }

    private void seedInitialApprovalRequests() {
        try {
            User officer = userRepository.findByEmail("himayacharuka@gmail.com").orElse(null);
            Long officerId = officer != null ? officer.getId() : 5L;

            User manager = userRepository.findByEmail("rscharukashehan@gmail.com")
                    .or(() -> userRepository.findByEmail("rscharukashehan@gmail.com"))
                    .orElse(null);
            Long managerId = manager != null ? manager.getId() : 1L;

            jdbcTemplate.update("INSERT IGNORE INTO notification_templates (id, template_key, name, subject, body, channel) " +
                    "VALUES (1, 'ACCOUNT_ACTIVATED', 'Account Activated', 'Your Account is Active', " +
                    "'Welcome to LankaTrust SmartBank. Your account is activated.', 'EMAIL')");

            // Request 1: Initiated by Officer Dilshan -> Bank Manager can review and approve immediately
            jdbcTemplate.update("INSERT INTO approval_requests (id, request_type, target_id, target_reference, requester_id, status, execution_status, remarks, requested_at, version) " +
                    "VALUES (1, 'TEMPLATE_DELETE', 1, 'ACCOUNT_ACTIVATED', ?, 'PENDING', 'PENDING', 'Deletion requested by Officer Dilshan Perera.', NOW(), 0) " +
                    "ON DUPLICATE KEY UPDATE requester_id = VALUES(requester_id), remarks = VALUES(remarks), target_id = 1", officerId);

            // Request 2: Initiated by primary Manager -> Demonstrates Four-Eyes Principle (Self-Requested for Manager 1, approvable by Manager 2)
            jdbcTemplate.update("INSERT INTO approval_requests (id, request_type, target_id, target_reference, requester_id, status, execution_status, remarks, requested_at, version) " +
                    "VALUES (2, 'STAFF_ROLE_UPDATE', ?, 'officer@lankatrust.lk', ?, 'PENDING', 'PENDING', 'role=CUSTOMER_RELATIONS_EXECUTIVE', NOW(), 0) " +
                    "ON DUPLICATE KEY UPDATE requester_id = VALUES(requester_id), target_id = VALUES(target_id)", officerId, managerId);

            org.slf4j.LoggerFactory.getLogger(DatabaseSeeder.class)
                    .info("[DATABASE SEEDER] Seeded dual-authorization approval queue (Officer request #1 + Manager request #2).");
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(DatabaseSeeder.class)
                    .warn("[DATABASE SEEDER] Seeding approval request notice: {}", e.getMessage());
        }
    }

    private User saveUser(User user, int roleId) {
        User saved = userRepository.save(user);
        try {
            jdbcTemplate.update("INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (?, ?)", saved.getId(), roleId);
        } catch (Exception ignored) {
        }
        return saved;
    }

    private String generateUniqueCustomerId() {
        int sequence = 1;
        while (customerRepository.existsByCustomerId(String.format("CUST%06d", 100000 + sequence))) {
            sequence++;
        }
        return String.format("CUST%06d", 100000 + sequence);
    }

    private String generateAccountNumber() {
        return "LTB" + String.format("%09d", RANDOM.nextInt(1_000_000_000));
    }

    private String generateCardNumber() {
        return "4" + String.format("%015d", RANDOM.nextLong(1_000_000_000_000_000L));
    }

    private void migrateSchemaIfNecessary() {
        try {
            if (jdbcTemplate.getDataSource() == null) {
                return;
            }
            try (java.sql.Connection conn = jdbcTemplate.getDataSource().getConnection()) {
                ensureColumnExists(conn, "otp_verifications", "operation_type", "VARCHAR(50) NULL DEFAULT 'LOGIN_STEPUP'");
                ensureColumnExists(conn, "approval_requests", "rejected_at", "DATETIME NULL");
                ensureColumnExists(conn, "approval_requests", "rejection_reason", "VARCHAR(500) NULL");
                ensureColumnExists(conn, "approval_requests", "execution_status", "VARCHAR(30) NULL DEFAULT 'PENDING'");
                ensureColumnExists(conn, "approval_requests", "executed_at", "DATETIME NULL");
                ensureColumnExists(conn, "approval_requests", "requested_data", "VARCHAR(1000) NULL");
                ensureColumnExists(conn, "approval_requests", "current_data", "VARCHAR(1000) NULL");
                ensureColumnExists(conn, "approval_requests", "version", "BIGINT NOT NULL DEFAULT 0");
                try {
                    jdbcTemplate.execute("ALTER TABLE loans MODIFY COLUMN status ENUM('SUBMITTED','UNDER_VERIFICATION','COMPLIANCE_CHECK','APPROVED','REJECTED','CANCELLED')");
                } catch (Exception ignored) {}
                try {
                    jdbcTemplate.execute("ALTER TABLE reports MODIFY COLUMN params TEXT NULL");
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(DatabaseSeeder.class)
                    .warn("[DATABASE MIGRATION] Schema check encountered non-fatal notice: {}", e.getMessage());
        }
    }

    private void ensureColumnExists(java.sql.Connection conn, String table, String column, String colDef) {
        try {
            java.sql.DatabaseMetaData meta = conn.getMetaData();
            boolean columnFound = false;
            try (java.sql.ResultSet rs = meta.getColumns(conn.getCatalog(), null, table.toLowerCase(), column.toLowerCase())) {
                if (rs.next()) columnFound = true;
            }
            if (!columnFound) {
                try (java.sql.ResultSet rs = meta.getColumns(conn.getCatalog(), null, table.toUpperCase(), column.toUpperCase())) {
                    if (rs.next()) columnFound = true;
                }
            }
            if (!columnFound) {
                jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + colDef);
                org.slf4j.LoggerFactory.getLogger(DatabaseSeeder.class)
                        .info("[DATABASE MIGRATION] Added missing column '{}.{}'", table, column);
            }
        } catch (Exception alterEx) {
            // Column already exists or table not yet created
        }
    }
}

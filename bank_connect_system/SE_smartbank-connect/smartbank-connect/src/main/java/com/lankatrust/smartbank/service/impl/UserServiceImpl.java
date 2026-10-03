package com.lankatrust.smartbank.service.impl;

import com.lankatrust.smartbank.entity.*;
import com.lankatrust.smartbank.repository.AccountRepository;
import com.lankatrust.smartbank.repository.CustomerRepository;
import com.lankatrust.smartbank.repository.UserRepository;
import com.lankatrust.smartbank.service.AuditLogService;
import com.lankatrust.smartbank.service.NotificationService;
import com.lankatrust.smartbank.service.SettingsService;
import com.lankatrust.smartbank.service.UserService;
import com.lankatrust.smartbank.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final com.lankatrust.smartbank.repository.OtpVerificationRepository otpVerificationRepository;
    private final com.lankatrust.smartbank.repository.PasswordResetTokenRepository passwordResetTokenRepository;
    private final SettingsService settingsService;
    private final JdbcTemplate jdbcTemplate;
    private final com.lankatrust.smartbank.service.OtpService otpService;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public User register(String fullName, String email, String rawPassword, String phone, String nic, String address, java.time.LocalDate dob) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        if (!PasswordValidator.isValid(rawPassword)) {
            throw new IllegalArgumentException(PasswordValidator.getRequirements());
        }
        Customer customer = Customer.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .phoneNumber(phone)
                .nic(nic)
                .role(Role.CUSTOMER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .address(address)
                .dob(dob)
                .kycStatus("PENDING")
                .build();
        User saved = userRepository.save(customer);
        assignRole(saved.getId(), 1);
        auditLogService.log(email, "CUSTOMER_REGISTERED", "User", saved.getId(), "Self-registration via web form");
        notificationService.send(saved, "Welcome to SmartBank Connect",
                "Your account has been created successfully. You can now apply for a bank account.",
                NotificationChannel.IN_APP);
        return saved;
    }

    @Override
    @Transactional
    public User registerCustomer(String customerId, String accountNumber, String email, String phone,
                                 String username, String rawPassword, String confirmPassword) {
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer ID not found in bank records."));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account number not found."));

        if (!account.getCustomer().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("Account number does not match the customer ID.");
        }
        if (!customer.getEmail().equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("Email does not match bank records.");
        }
        if (!customer.getPhoneNumber().equals(phone)) {
            throw new IllegalArgumentException("Mobile number does not match bank records.");
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Account is not yet active. Please wait for manager approval.");
        }
        if (!rawPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        if (!PasswordValidator.isValid(rawPassword)) {
            throw new IllegalArgumentException(PasswordValidator.getRequirements());
        }
        if (customer.getPassword() != null && !customer.getPassword().isBlank()) {
            throw new IllegalArgumentException("This customer is already registered. Please log in.");
        }

        customer.setPassword(passwordEncoder.encode(rawPassword));
        customer.setEnabled(true);
        User saved = userRepository.save(customer);

        auditLogService.log(email, "CUSTOMER_REGISTRATION_COMPLETED", "User", saved.getId(),
                "Customer completed online registration with username: " + username);
        notificationService.send(saved, "Registration Successful",
                "Your online banking access has been activated. Username: " + username,
                NotificationChannel.EMAIL);
        return saved;
    }

    @Override
    @Transactional
    public User createStaffUser(String fullName, String email, String rawPassword, Role role, String nic, String employeeId, String phone, String branch, String createdByEmail) {
        if (!PasswordValidator.isValid(rawPassword)) {
            throw new IllegalArgumentException(PasswordValidator.getRequirements());
        }
        return createStaffUserFromHash(fullName, email, passwordEncoder.encode(rawPassword), role, nic, employeeId, phone, branch, createdByEmail);
    }

    @Override
    @Transactional
    public User createStaffUserFromHash(String fullName, String email, String passwordHash, Role role, String nic, String employeeId, String phone, String branch, String createdByEmail) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        User user;
        if (role == Role.BANK_OFFICER) {
            user = Officer.builder()
                    .fullName(fullName)
                    .email(email)
                    .password(passwordHash)
                    .phoneNumber(phone)
                    .nic(nic)
                    .role(role)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .employeeId(employeeId)
                    .branch(branch)
                    .build();
        } else {
            user = User.builder()
                    .fullName(fullName)
                    .email(email)
                    .password(passwordHash)
                    .phoneNumber(phone)
                    .nic(nic)
                    .role(role)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build();
        }
        User saved = userRepository.save(user);
        assignRole(saved.getId(), roleId(role));
        auditLogService.log(createdByEmail, "STAFF_USER_CREATED", "User", saved.getId(),
                "Role assigned: " + role + " (automatically saved to MySQL)");
        return saved;
    }

    @Override
    @Transactional
    public User updateRole(Long userId, Role newRole, String actorEmail) {
        User user = getById(userId);
        Role oldRole = user.getRole();
        user.setRole(newRole);
        User saved = userRepository.save(user);
        assignRole(saved.getId(), roleId(newRole));
        auditLogService.log(actorEmail, "STAFF_ROLE_UPDATED", "User", saved.getId(),
                "Role changed from " + oldRole + " to " + newRole);
        return saved;
    }

    @Override
    @Transactional
    public String generateAndSendOtp(String email) {
        return otpService.generateAndSendOtp(email, "LOGIN_STEPUP");
    }

    @Override
    @Transactional
    public boolean verifyOtp(String email, String otp) {
        return otpService.verifyOtp(email, otp);
    }

    @Override
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    @Override
    public User getByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        String clean = email.trim();
        return userRepository.findByEmail(clean)
                .or(() -> userRepository.findAll().stream()
                        .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(clean))
                        .findFirst())
                .or(() -> clean.contains("@") ? java.util.Optional.empty()
                        : userRepository.findByEmail(clean + "@lankatrust.lk"))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, String fullName, String phone, String address, java.time.LocalDate dob, String profilePhotoPath) {
        User user = getById(userId);
        user.setFullName(fullName);
        user.setPhoneNumber(phone);
        if (user instanceof Customer customer) {
            customer.setAddress(address);
            customer.setDob(dob);
            if (profilePhotoPath != null) {
                customer.setProfilePhotoPath(profilePhotoPath);
            }
        }
        User saved = userRepository.save(user);
        auditLogService.log(user.getEmail(), "PROFILE_UPDATED", "User", saved.getId(), "Customer profile updated");
        return saved;
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword, String confirmPassword) {
        User user = getById(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirmation do not match.");
        }
        if (!PasswordValidator.isValid(newPassword)) {
            throw new IllegalArgumentException(PasswordValidator.getRequirements());
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        auditLogService.log(user.getEmail(), "PASSWORD_CHANGED", "User", user.getId(), "Customer changed password");
        notificationService.send(user, "Password Changed",
                "Your SmartBank Connect password was changed successfully.", NotificationChannel.EMAIL);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User setEnabled(Long userId, boolean enabled, String actorEmail) {
        User user = getById(userId);
        user.setEnabled(enabled);
        User saved = userRepository.save(user);
        auditLogService.log(actorEmail, enabled ? "USER_ENABLED" : "USER_DISABLED", "User", saved.getId(),
                saved.getEmail());
        return saved;
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account found for that email address."));
        String token = java.util.UUID.randomUUID().toString().replace("-", "");
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .userId(user.getId())
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();
        passwordResetTokenRepository.save(resetToken);
        String link = "http://localhost:8080/reset-password?token=" + token;
        notificationService.sendHtml(user, "Reset your SmartBank Connect password",
                com.lankatrust.smartbank.util.EmailTemplates.generic(user.getFullName(),
                        "<p>We received a request to reset your password.</p>"
                                + "<p><a href=\"" + link + "\">Reset password</a></p>"
                                + "<p>This link expires in 30 minutes. If you did not request a reset, ignore this email.</p>"),
                "Reset your password: " + link);
        auditLogService.log(email, "PASSWORD_RESET_REQUESTED", "User", user.getId(), "Reset token issued");
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token."));
        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This reset link has expired. Please request a new one.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        if (!PasswordValidator.isValid(newPassword)) {
            throw new IllegalArgumentException(PasswordValidator.getRequirements());
        }
        User user = getById(resetToken.getUserId());
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        auditLogService.log(user.getEmail(), "PASSWORD_RESET_COMPLETED", "User", user.getId(), "Password reset via email link");
    }

    @Override
    public List<User> searchCustomers(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        return userRepository.findByRole(Role.CUSTOMER).stream()
                .filter(u -> q.isBlank()
                        || (u.getFullName() != null && u.getFullName().toLowerCase().contains(q))
                        || (u.getEmail() != null && u.getEmail().toLowerCase().contains(q))
                        || (u.getNic() != null && u.getNic().toLowerCase().contains(q))
                        || (u.getPhoneNumber() != null && u.getPhoneNumber().contains(q)))
                .toList();
    }

    private void assignRole(Long userId, int roleId) {
        jdbcTemplate.update("INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (?, ?)", userId, roleId);
    }

    private int roleId(Role role) {
        return switch (role) {
            case CUSTOMER -> 1;
            case BANK_OFFICER -> 2;
            case BANK_MANAGER -> 3;
            case SYSTEM_ADMINISTRATOR -> 4;
            case COMPLIANCE_OFFICER -> 5;
            case CUSTOMER_RELATIONS_EXECUTIVE -> 6;
        };
    }
}

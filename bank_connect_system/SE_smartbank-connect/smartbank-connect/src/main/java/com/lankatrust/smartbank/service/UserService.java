package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.entity.Role;
import com.lankatrust.smartbank.entity.User;

import java.util.List;

public interface UserService {
    User register(String fullName, String email, String rawPassword, String phone, String nic, String address, java.time.LocalDate dob);
    User registerCustomer(String customerId, String accountNumber, String email, String phone,
                          String username, String rawPassword, String confirmPassword);
    User createStaffUser(String fullName, String email, String rawPassword, Role role, String nic, String employeeId, String phone, String branch, String createdByEmail);

    /** Used by ApprovalServiceImpl to finalize an approved StaffCreationRequest — password is already BCrypt-hashed, so it is stored as-is. */
    User createStaffUserFromHash(String fullName, String email, String passwordHash, Role role, String nic, String employeeId, String phone, String branch, String createdByEmail);

    /** Used by ApprovalServiceImpl when a manager approves a STAFF_ROLE_UPDATE request. */
    User updateRole(Long userId, Role newRole, String actorEmail);
    String generateAndSendOtp(String email);
    boolean verifyOtp(String email, String otp);
    List<User> findByRole(Role role);
    User getByEmail(String email);
    User getById(Long id);

    User updateProfile(Long userId, String fullName, String phone, String address, java.time.LocalDate dob, String profilePhotoPath);

    void changePassword(Long userId, String currentPassword, String newPassword, String confirmPassword);

    List<User> findAll();
    User setEnabled(Long userId, boolean enabled, String actorEmail);
    void requestPasswordReset(String email);
    void resetPassword(String token, String newPassword, String confirmPassword);
    List<User> searchCustomers(String query);
}

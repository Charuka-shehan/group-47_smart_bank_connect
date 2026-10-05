package com.lankatrust.smartbank.controller;

import com.lankatrust.smartbank.entity.Role;
import com.lankatrust.smartbank.entity.StaffCreationRequest;
import com.lankatrust.smartbank.repository.StaffCreationRequestRepository;
import com.lankatrust.smartbank.service.ApprovalService;
import com.lankatrust.smartbank.service.UserService;
import com.lankatrust.smartbank.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/staff")
public class StaffController {

    private final UserService userService;
    private final StaffCreationRequestRepository staffCreationRequestRepository;
    private final ApprovalService approvalService;
    private final PasswordEncoder passwordEncoder;

    /** C: Manager/Admin can still create staff directly (unchanged, existing prototype behaviour). */
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('BANK_MANAGER', 'SYSTEM_ADMINISTRATOR')")
    public String createStaff(@RequestParam String fullName,
                              @RequestParam String email,
                              @RequestParam String password,
                              @RequestParam Role role,
                              @RequestParam String nic,
                              @RequestParam String employeeId,
                              @RequestParam String phone,
                              @RequestParam String branch,
                              Authentication authentication,
                              RedirectAttributes ra) {
        try {
            userService.createStaffUser(fullName, email, password, role, nic, employeeId, phone, branch, authentication.getName());
            ra.addFlashAttribute("success", "Staff account created successfully.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/staff";
    }

    /** C (request): Bank Officer submits a new staff creation request — Manager/Admin approval required before the account exists. */
    @PostMapping("/request-create")
    @PreAuthorize("hasRole('BANK_OFFICER')")
    public String requestCreateStaff(@RequestParam String fullName,
                                     @RequestParam String email,
                                     @RequestParam String password,
                                     @RequestParam Role role,
                                     @RequestParam String nic,
                                     @RequestParam String employeeId,
                                     @RequestParam String phone,
                                     @RequestParam String branch,
                                     Authentication authentication,
                                     RedirectAttributes ra) {
        try {
            if (!PasswordValidator.isValid(password)) {
                throw new IllegalArgumentException(PasswordValidator.getRequirements());
            }
            StaffCreationRequest request = StaffCreationRequest.builder()
                    .fullName(fullName)
                    .email(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .role(role)
                    .nic(nic)
                    .employeeId(employeeId)
                    .phone(phone)
                    .branch(branch)
                    .requestedBy(userService.getByEmail(authentication.getName()))
                    .build();
            StaffCreationRequest saved = staffCreationRequestRepository.save(request);
            approvalService.create("STAFF_CREATE", saved.getId(), email, authentication.getName(),
                    "New " + role + " account requested for " + fullName);
            ra.addFlashAttribute("success", "Staff creation request submitted for manager approval.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/staff";
    }

    /** U (request): Officer requests a role change for an existing staff member — Manager/Admin approval required. */
    @PostMapping("/{id}/request-role-update")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String requestRoleUpdate(@PathVariable Long id, @RequestParam Role newRole,
                                    Authentication authentication, RedirectAttributes ra) {
        try {
            var target = userService.getById(id);
            approvalService.create("STAFF_ROLE_UPDATE", id, target.getEmail(), authentication.getName(),
                    "role=" + newRole);
            ra.addFlashAttribute("success", "Role update request submitted for manager approval.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/staff";
    }

    /** D (request): Officer requests deletion (disable) of a staff account — Manager/Admin approval required. */
    @PostMapping("/{id}/request-deletion")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String requestDeletion(@PathVariable Long id, @RequestParam(required = false) String reason,
                                  Authentication authentication, RedirectAttributes ra) {
        try {
            var target = userService.getById(id);
            approvalService.create("STAFF_DELETE", id, target.getEmail(), authentication.getName(),
                    reason != null ? reason : "Deletion requested by officer");
            ra.addFlashAttribute("success", "Staff deletion request submitted for manager approval.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/staff";
    }
}

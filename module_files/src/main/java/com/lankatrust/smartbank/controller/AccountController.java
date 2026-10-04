package com.lankatrust.smartbank.controller;

import com.lankatrust.smartbank.entity.Account;
import com.lankatrust.smartbank.entity.Branch;
import com.lankatrust.smartbank.entity.Role;
import com.lankatrust.smartbank.entity.User;
import com.lankatrust.smartbank.service.AccountService;
import com.lankatrust.smartbank.service.BranchService;
import com.lankatrust.smartbank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import com.lankatrust.smartbank.service.ValidationService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;
    private final BranchService branchService;
    private final ValidationService validationService;

    /** R: Staff can view all accounts at any time. */
    @GetMapping("/manage")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String manage(Model model) {
        model.addAttribute("accounts", accountService.getAll());
        model.addAttribute("customers", userService.findByRole(Role.CUSTOMER));
        model.addAttribute("branches", branchService.getAll());
        return "accounts/manage";
    }

    /** Officer account opening form. */
    @GetMapping("/open")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String openAccountForm(Model model) {
        model.addAttribute("branches", branchService.getAll());
        model.addAttribute("accountTypes", new String[]{"SAVINGS", "CURRENT", "FIXED_DEPOSIT"});
        return "accounts/open";
    }

    /** C: Officer submits a new account request (PENDING_APPROVAL). */
    @PostMapping("/open")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','SYSTEM_ADMINISTRATOR')")
    public String openAccount(@RequestParam String customerId,
                              @RequestParam String fullName,
                              @RequestParam String email,
                              @RequestParam String phone,
                              @RequestParam String nic,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dob,
                              @RequestParam String address,
                              @RequestParam String occupation,
                              @RequestParam BigDecimal monthlyIncome,
                              @RequestParam String accountType,
                              @RequestParam BigDecimal initialDeposit,
                              @RequestParam Integer branchId,
                              @RequestParam(required = false) String nomineeName,
                              @RequestParam(required = false) String nomineeRelationship,
                              @RequestParam(required = false) MultipartFile signature,
                              @RequestParam(required = false) MultipartFile nicFront,
                              @RequestParam(required = false) MultipartFile nicBack,
                              @RequestParam(required = false) MultipartFile proofOfAddress,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            Map<String, MultipartFile> documents = new HashMap<>();
            if (signature != null && !signature.isEmpty()) {
                validationService.validateUploadedFile(signature, "document");
                documents.put("signature", signature);
            }
            if (nicFront != null && !nicFront.isEmpty()) {
                validationService.validateUploadedFile(nicFront, "document");
                documents.put("nicFront", nicFront);
            }
            if (nicBack != null && !nicBack.isEmpty()) {
                validationService.validateUploadedFile(nicBack, "document");
                documents.put("nicBack", nicBack);
            }
            if (proofOfAddress != null && !proofOfAddress.isEmpty()) {
                validationService.validateUploadedFile(proofOfAddress, "document");
                documents.put("proofOfAddress", proofOfAddress);
            }

            Account account = accountService.openAccount(customerId, fullName, email, phone, nic, dob, address,
                    occupation, monthlyIncome, accountType, initialDeposit, branchId,
                    nomineeName, nomineeRelationship, documents, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Account opening request submitted: " + account.getAccountNumber());
            return "redirect:/accounts/manage";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/accounts/open";
        }
    }

    /** C: Customer or staff requests/opens a new account from dashboard. */
    @PostMapping("/request")
    @PreAuthorize("hasAnyRole('CUSTOMER','BANK_MANAGER','BANK_OFFICER','SYSTEM_ADMINISTRATOR')")
    public String requestAccount(@RequestParam String accountType,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getByEmail(authentication.getName());
            Account acc = accountService.createForCustomer(user.getId(), accountType);
            redirectAttributes.addFlashAttribute("success", "New " + accountType + " account created: " + acc.getAccountNumber());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/bankdash/overview";
    }

    /** C: Officer submits a new account request for an existing customer (PENDING_APPROVAL). */
    @PostMapping("/manage/create")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','SYSTEM_ADMINISTRATOR')")
    public String create(@RequestParam Long customerId, @RequestParam String accountType,
                          Authentication authentication) {
        accountService.submitNewAccount(customerId, accountType, authentication.getName());
        return "redirect:/accounts/manage";
    }

    /** U: Officer submits an account update request (PENDING Manager Approval). */
    @PostMapping("/manage/{id}/request-update")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String requestUpdate(@PathVariable Long id,
                                @RequestParam(required = false) String accountType,
                                @RequestParam(required = false) Integer branchId,
                                @RequestParam(required = false) String nomineeName,
                                @RequestParam(required = false) String nomineeRelationship,
                                @RequestParam(required = false) String occupation,
                                @RequestParam(required = false) BigDecimal monthlyIncome,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            accountService.requestAccountUpdate(id, accountType, branchId, nomineeName, nomineeRelationship,
                    occupation, monthlyIncome, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Account update request submitted for manager approval.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/accounts/manage";
    }

    /** U: Manager approves a pending account, activating it. */
    @PostMapping("/manage/{id}/approve")
    @PreAuthorize("hasAnyRole('BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String approve(@PathVariable Long id, Authentication authentication) {
        accountService.approveAccount(id, authentication.getName());
        return "redirect:/accounts/manage";
    }

    /** Manager rejects a pending account. */
    @PostMapping("/manage/{id}/reject")
    @PreAuthorize("hasAnyRole('BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String reject(@PathVariable Long id, @RequestParam String reason, Authentication authentication) {
        accountService.rejectAccount(id, authentication.getName(), reason);
        return "redirect:/accounts/manage";
    }

    /** Officer freeze becomes a manager-approval request; managers freeze immediately. */
    @PostMapping("/manage/{id}/freeze")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String freeze(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        boolean manager = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BANK_MANAGER") || a.getAuthority().equals("ROLE_SYSTEM_ADMINISTRATOR"));
        if (manager) {
            accountService.freezeAccount(id, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Account frozen.");
        } else {
            accountService.requestFreeze(id, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Freeze request submitted for manager approval.");
        }
        return "redirect:/accounts/manage";
    }

    @PostMapping("/manage/{id}/unfreeze")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String unfreeze(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        boolean manager = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BANK_MANAGER") || a.getAuthority().equals("ROLE_SYSTEM_ADMINISTRATOR"));
        if (manager) {
            accountService.unfreezeAccount(id, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Account reactivated.");
        } else {
            accountService.requestUnfreeze(id, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Unfreeze request submitted for manager approval.");
        }
        return "redirect:/accounts/manage";
    }

    @PostMapping("/manage/{id}/request-close")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String requestClose(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        accountService.requestClosure(id, authentication.getName());
        redirectAttributes.addFlashAttribute("success", "Closure request submitted for manager approval.");
        return "redirect:/accounts/manage";
    }

    /** D: Manager approves closure. */
    @PostMapping("/manage/{id}/close")
    @PreAuthorize("hasAnyRole('BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String close(@PathVariable Long id, Authentication authentication) {
        accountService.approveClosure(id, authentication.getName());
        return "redirect:/accounts/manage";
    }

    /** Permanent deletion: Removes account and all dependent records from database. */
    @PostMapping({"/manage/{id}/delete", "/{id}/delete"})
    @PreAuthorize("hasAnyRole('BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String deleteAccount(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            accountService.deleteAccountPermanently(id, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Account has been permanently deleted from the database.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/accounts/manage";
    }

    @DeleteMapping({"/manage/{id}", "/{id}"})
    @PreAuthorize("hasAnyRole('BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> deleteAccountApi(@PathVariable Long id, Authentication authentication) {
        try {
            accountService.deleteAccountPermanently(id, authentication.getName());
            return org.springframework.http.ResponseEntity.ok(Map.of("message", "Account permanently deleted from database."));
        } catch (Exception ex) {
            return org.springframework.http.ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
